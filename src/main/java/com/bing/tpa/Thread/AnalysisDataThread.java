package com.bing.tpa.Thread;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bing.tpa.domain.dto.AnalysisResult;
import com.bing.tpa.domain.entity.*;
import com.bing.tpa.mapper.*;
import com.bing.tpa.modelcall.designCall.AnalysisByModel;
import com.bing.tpa.modelcall.designCall.ChatWithModel;
import com.bing.tpa.service.baseImpl.TpaHomeworkServiceImpl;
import com.bing.tpa.utils.*;
import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.client.exception.CozeApiException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.sql.rowset.spi.SyncResolver;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AnalysisDataThread {

    @Autowired
    private TpaDesignBeforeMapper designBeforeMapper;

    @Autowired
    private TpaHomeworkMapper homeworkMapper;

    @Autowired
    private TpaHomeworkTrackMapper homeworkTrackMapper;

    @Autowired
    private TpaHomeworkDetailsMapper detailsMapper;

    @Autowired
    private TpaPreviewTaskMapper previewTaskMapper;

    @Autowired
    private TpaPreviewTrackMapper previewTrackMapper;

    @Autowired
    private TpaTeachDesignMapper designMapper;

    @Autowired
    private TpaInteractionMapper interactionMapper;

    @Autowired
    private TpaClassMapper classMapper;

    @Autowired
    private ChatWithModel chatWithModel;


    private static List<String> ids=new ArrayList<>();

    private static final Long datasetId=7476419813767741494L;
    private static final String botId="7474540258005811210";

    long minDuration = Long.MAX_VALUE; // 初始化最小时间差为最大值

    private static List<String> documentIds=new ArrayList<>();

//    cid班级的id，后续根据班级的id找到该班级的所有作业和预习任务，至于互动互数据就使用上一次最新的教学设计的id找互动数据
//    @Async("analysisData")
    public void analysisDataAndSave(TpaTeachDesign teachDesign){
        List<Message> messageHistory = new ArrayList<>();
//        1.首先根据两个主题来匹配相应的作业和任务，如果匹配不到完全一样或相似度超过80%的，就使用最新作业或者预习任务的数据
           try {

//               设置四个线程
               ScheduledExecutorService executor= Executors.newScheduledThreadPool(4);
               AtomicReference<String> homeworkExecutorResult = new AtomicReference<>("");
               AtomicReference<String> previewExecutorResult = new AtomicReference<>("");
               AtomicReference<String> interactionExecutorResult = new AtomicReference<>("");
               AtomicReference<String> predictionExecutorResult = new AtomicReference<>("");
               TpaDesignBefore tpaDesignBefore = new TpaDesignBefore();
               try {
//                   作业分析线程
                   executor.schedule(()->{
                       // 2. 作业数据分析
                       String homeworkResult = null;
                       try {
                           homeworkResult = analyzeHomeworkData(teachDesign, messageHistory);
                           assert homeworkResult != null;
                           tpaDesignBefore.setAnalysisResult(homeworkResult);
                           homeworkExecutorResult.set(homeworkResult);
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   },0, TimeUnit.SECONDS);

//                   预习任务分析线程
                   executor.schedule(()->{
                       // 3. 预习任务分析
                       String previewResult = null;
                       try {
                           previewResult = analyzePreviewTask(teachDesign,  messageHistory);
                           assert previewResult != null;
                           tpaDesignBefore.setPreviewResult(previewResult);
                           previewExecutorResult.set(previewResult);
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   },1, TimeUnit.SECONDS);
//
//                   互动数据分析线程
                   executor.schedule(()->{
                       // 4. 互动数据分析
                       String interactionResult = null;
                       try {
                           interactionResult = analyzeInteractionData(teachDesign, messageHistory);
                           assert interactionResult != null;
                           tpaDesignBefore.setInteractionAnalysis(interactionResult);
                           interactionExecutorResult.set(interactionResult);
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   },2, TimeUnit.SECONDS);
//                  预测知识点线程
                   executor.schedule(()->{
                       String  prediction = null;
                       try {
                           prediction = generatePrediction(teachDesign,messageHistory);
                           tpaDesignBefore.setKnowledgeAnalysis(prediction);
                           predictionExecutorResult.set(prediction);
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   },3, TimeUnit.SECONDS);
               }finally {
//               将整个tpaDesignBefore对象进行最后处理，在插入到数据库中
                   tpaDesignBefore.setTdId(teachDesign.getTdId());
                   List<Integer> form=new ArrayList<>();
                   form.add(teachDesign.getTdId());
                   tpaDesignBefore.setAnalysisFrom(new Gson().toJson(form));
                   // 保存最终结果
                   int insert = designBeforeMapper.insert(tpaDesignBefore);
                   if(insert!=1) throw new RuntimeException("教学设计参考数据获取失败");
//               删除此次知识库文件
                   // 优雅关闭线程池（等待所有任务完成）
                   executor.shutdown();
                   try {
                       if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                           executor.shutdownNow();
                       }
                   } catch (InterruptedException e) {
                       executor.shutdownNow();
                       Thread.currentThread().interrupt();
                   }
               }
               log.info("作业分析生成的数据:\n"+homeworkExecutorResult.get());
               log.info("预习任务分析报告：\n"+predictionExecutorResult.get());
               log.info("互动数据分析报告:\n"+interactionExecutorResult.get());
               log.info("知识点预测：\n"+predictionExecutorResult.get());
               deleteKnowledgeBase();//将知识库的文件删除！！
           } catch (CozeApiException e) {
               e.printStackTrace();
           }
    }

    /**
     * 上传作业数据再进行分析
     * @param teachDesign
     * @param history
     * @throws Exception
     * @return
     */
//组织作业数据，先需要根据两级主题匹配到相应的数据，然后将数据上传到知识库中
    private String analyzeHomeworkData(TpaTeachDesign teachDesign, List<Message> history) throws Exception {
//       这个数组用来收集匹配到的作业id，后续到学生完成详情表中查找
        List<TpaHomework> homeworks=new ArrayList<>();
        TpaHomework nearestHomework = null; // 用于记录最近的 homework

        Map<Integer, String>  cidsAndName = getClasses(teachDesign);
        Set<Integer> cids = cidsAndName.keySet();
//        1.从数据库中匹配数据
//            1.1先将该班级的所有作业拿出来，如果没有，就退出
        QueryWrapper<TpaHomework> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("cid",cids);
//        根据该老师教授的所有班级的id获取所有班级的作业
        List<TpaHomework> tpaHomeworks = homeworkMapper.selectList(queryWrapper);
        if(tpaHomeworks==null) return null;
        for (TpaHomework homework:tpaHomeworks){
//            调研相似度匹配函数对两个主题进行匹配，两个主题只要有一个匹配上了，该就算是相关作业
//            满足第一主题相似度大于等于95%就是相同的，先暂定95%，后续再调整
//            第二主题相似度大于等于90%，先暂定90%，后续再调整
            double firstTitle = TextCompare.getCosineSimilarity(homework.getHTitle(), teachDesign.getDesignTitle()) * 100;
            double secondaryTitle = TextCompare.calculateNormalizedLevenshteinDistance(homework.getSecondaryTitle(), teachDesign.getSecondaryTitle()) * 100;
            if (firstTitle >= 95 || secondaryTitle >= 80) {//firstTitle相似度高表示这是一个单元的，所以第二标题可以放宽松
                // 只要满足一个，该作业的知识点就和本次教学设计相关，就将这次作业的 id 记录下来
//                homeworkIds.add(homework.getHid());
//                homeTitle.add(homework.getHTitle()+"--"+homework.getSecondaryTitle());
//                homeName.add(homework.getHName());
//                homeworkScore.add(homework.getScore());
                homeworks.add(homework);
                }
            // 比较 createTime 和当前时间，找出最近的 homework
            LocalDateTime createTime = homework.getCreateTime();
            if (createTime != null) {
                Duration duration = Duration.between(createTime, LocalDateTime.now());
                long durationInSeconds = duration.toSeconds(); // 获取时间差（秒）

                if (durationInSeconds < minDuration) {
                    minDuration = durationInSeconds;
                    nearestHomework = homework; // 更新最近的 homework
                }
            }
//        记录下距离当前时间最近的一次作业
        }
//            1.2遍历作业进行主题匹配，如果一个都匹配不到，就选取最新一次的作业
                if(homeworks.size()==0){
                    assert nearestHomework != null;
                    homeworks.add(nearestHomework);
//                    homeworkIds.add(nearestHomework.getHid());
//                    homeTitle.add(nearestHomework.getHTitle()+"--"+nearestHomework.getSecondaryTitle());
//                    homeName.add(nearestHomework.getHName());
                }
//       将家庭作业的id集合拿出来
        List<Integer> homeworkIds = homeworks.stream().map(TpaHomework::getHid).collect(Collectors.toList());
//       1.3拿出匹配到的作业对应的学生完成数据,同时联表查询到每个题对应的学生的名字
        List<TpaHomeworkTrack> trackData = homeworkTrackMapper.findBath(homeworkIds);
//       1.4获取筛选到的作业中的所有题目
        List<TpaHomeworkDetails> questionBath = detailsMapper.findQuestionBath(homeworkIds);

//        2.遍历作业id列表，再根据id找出questionBath对应的题目，再将题目的id和trackData中对应的数据一起写到txt文件中
//        2.1根据作业的id给题目进行分组,同时给学生完成数据分组（学生完成数据中不同作业的跟踪数据是混到一起的，所以要根据作业进行区分）
        Map<Integer, List<TpaHomeworkDetails>> questionMap = questionBath.stream()
                .collect(Collectors.groupingBy(TpaHomeworkDetails::getHid));

        Map<Integer, List<TpaHomeworkTrack>> trackDataMap = trackData.stream()
                .collect(Collectors.groupingBy(TpaHomeworkTrack::getHid));

//        将作业根据班级进行分类
        Map<Integer, List<TpaHomework>> homeworkByCid = homeworks.stream().collect(Collectors.groupingBy(TpaHomework::getCid));
//        准备string接受班级作业数据
        StringBuilder totalTxt=new StringBuilder();
        totalTxt.append("作业数据：");
//        2.2遍历班级，根据班级，在将作业进行分组，在遍历作业猴子那个的所有题目和跟踪数据
        for (Integer cid:cids){
//            根据班级的id来获取该班级的所有作业
            List<TpaHomework> oneClassHomework = homeworkByCid.getOrDefault(cid, new ArrayList<>());
            totalTxt.append("***"+cidsAndName.get(cid)+"班级的相关作业完成情况***\n");
            int i=0;
//            遍历作业
            for (TpaHomework oneClassWork:oneClassHomework){
                List<TpaHomeworkDetails> questionsByHid = questionMap.getOrDefault(oneClassWork.getHid(), new ArrayList<TpaHomeworkDetails>());
                List<TpaHomeworkTrack> trackByHid = trackDataMap.getOrDefault(oneClassWork.getHid(), new ArrayList<TpaHomeworkTrack>());
//            2.3组装这次作业的数据
                String oneHomeworkDataTxt = processQuestions(oneClassWork.getHName(), oneClassWork.getHTitle()+"--"+oneClassWork.getSecondaryTitle(),
                        questionsByHid,trackByHid,oneClassWork.getScore());
                if(i==0)//第一次的时候开头不能有分隔符
                    totalTxt.append(oneHomeworkDataTxt);
                else totalTxt.append("\n\n-------------------------------------------------------------------------------\n" +
                        "-------------------------------------------------------------------------------\n"+oneHomeworkDataTxt);
                i++;
            }
            totalTxt.append("\n\n");
        }
        System.out.println(totalTxt);
//          2.4将这个作业的数据放入 作业数据.txt 文件中,并进行上传
       Map<String,String> nameAndId = upload(totalTxt, teachDesign.getDesignName()+"教学设计的参考作业数据");
//        文件上传完毕后就可以进行数据分析了
        String question="请根据知识库中的一个文件:"+nameAndId.get("name")+",这个文件中记录着一个老师教学的所有班级下的所有作业，以及每一位学生每一道题的完成情况，请根据文件记录的作业完成情况进行数据分析\n" +
                "要求：1、请严格只参考我指定的知识库文件，不要参考文件名和我指定的不一样的文件\n2、生成的分析报告不要太长，并将所有的markdown的符号去掉\n" +
                "3、要对每一个班级的每一次作业的每一道题对应的所有学生的做题情况进行分析，并以班级为单位，再以每次作业题目的完成数据为最小单位进行分析和总结\n" +
                "4、需要分析出每次作业学生对知识点的掌握情况、错题情况以及后续的教学设计编写、课堂教学方式和侧重点的建议\n5、文件中过的数据比较多，但是你生成的分析数据和结果需要简洁凝练直击要点。注意：不要生成太多内容";
//        5.调用数据分析接口，进行数据分析;将分析数据进行包装返回
        return chatWithModel.chatClient(question, String.valueOf(teachDesign.getAuthorId()),botId);
    }

    /**
     * 根据教师id查找他教的所有班级，找出这些班级中符合条件的预习任务和作业
     * @param teachDesign
     * @return
     */
    @NotNull
    private Map<Integer, String>  getClasses(TpaTeachDesign teachDesign) {
        QueryWrapper<TpaClass> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.eq("tid", teachDesign.getAuthorId())
                .like("c_subject", teachDesign.getSubject())
                .select("cid","c_name");
        List<TpaClass> classes = classMapper.selectList(queryWrapper1);
        Map<Integer, String> cidAndName = classes.stream().collect(Collectors.toMap(TpaClass::getCid, TpaClass::getCName));
        return cidAndName;
    }

    private String processQuestions(String homeName, String homeTitle, List<TpaHomeworkDetails> questionsByHid, List<TpaHomeworkTrack> trackData, Integer homeworkScore) {
        StringBuilder dataBuilder=new StringBuilder();
        dataBuilder.append("作业").append(homeName).append("\n").append("作业涉及知识点:").append(homeTitle).append("\n").append("作业总分：").append(homeworkScore).append("\n本次作业的题目数据和每道题学生完成情况：").append("\n");
//      前面已经将学生完成数据根据作业来进行分组了，所以这里就只需要根据题目进行分组就可以了
        Map<Integer, List<TpaHomeworkTrack>> trackByQid = trackData.stream().collect(Collectors.groupingBy(TpaHomeworkTrack::getQid));
//        遍历题目，根据题目id和根据题目进行筛选的
        for(int i=0;i<questionsByHid.size();i++){
//            通过正在遍历的问题id来匹配该问题的所有学生回答情况
            List<TpaHomeworkTrack> oneQuestionTrack = trackByQid.getOrDefault(questionsByHid.get(i).getQid(), new ArrayList<TpaHomeworkTrack>());
//            先将题目放进去
            dataBuilder.append("题目").append(i+1).append(":").append(questionsByHid.get(i).getQcontent()).append("\n");
//            每一个问题的开头信息（题目的题干，答案，解析等）
            ProblemHeader(questionsByHid, dataBuilder, i);
            dataBuilder.append("题目").append(i+1).append("的学生答题情况：\n");
//            在将该题目学生的track数据放进去
            if (oneQuestionTrack.size()==0) {
                dataBuilder.append("该班暂时无人回答这个题目\n\n");
                continue;}
            for (TpaHomeworkTrack oneTrack:oneQuestionTrack){
                builderStuAnswer(dataBuilder, oneTrack);
                dataBuilder.append(",错误原因：").append(oneTrack.getMistakeCase()).append("}\n\n");
            }
            dataBuilder.append("############\n\n");
        }
        return dataBuilder.toString();
    }

    public void builderStuAnswer(StringBuilder dataBuilder, TpaHomeworkTrack oneTrack) {
        dataBuilder.append("学生").append(oneTrack.getStuName()).append(":{").append("完成情况：");
        if(oneTrack.getStatus()==0) dataBuilder.append("未开始");
        else dataBuilder.append("已完成");
        dataBuilder.append(",该学生的回答：").append(oneTrack.getAnswer());
        dataBuilder.append(",获得的分数：").append(oneTrack.getScore());
        dataBuilder.append(",解题用时：").append(oneTrack.getTimeSpent());
        dataBuilder.append(",是否正确：").append(oneTrack.getIsCorrect());
    }

    private void ProblemHeader(@NotNull List<TpaHomeworkDetails> questionsByHid, StringBuilder dataBuilder, int i) {
        if(questionsByHid.get(i).getQtype().contains("选择题")){
            dataBuilder.append("题目选项：").append(questionsByHid.get(i).getSelections()).append("\n");
        }
        dataBuilder.append("题目解析：").append(questionsByHid.get(i).getAnswerAnalysis()).append("\n");
        dataBuilder.append("题目分数：").append(questionsByHid.get(i).getDefaultScore()).append("\n");
    }

    /**
     * 上传预习任务数据再继续分析
     * @param teachDesign
     * @param history
     * @return
     */
    private String  analyzePreviewTask(TpaTeachDesign teachDesign, List<Message> history) throws Exception {
//        记录符合字符串匹配条件的预习任务
        List<TpaPreviewTask> previewTaskList=new ArrayList<>();
        TpaPreviewTask nearestPreviewTask=new TpaPreviewTask();
        List<Integer> taskIds=new ArrayList<>();
//        根据老师的id和教学的科目匹配相应的班级的id
        Map<Integer, String>  cidsAndName = getClasses(teachDesign);
        Set<Integer> cids = cidsAndName.keySet();
//        1.从数据库中匹配数据
//        根据班级id获取该班级的所有预习任务(已发布的)
            QueryWrapper<TpaPreviewTask> queryWrapper=new QueryWrapper<>();
            queryWrapper.in("cid",cids);
        List<TpaPreviewTask> previewTasks = previewTaskMapper.selectList(queryWrapper);
        if (previewTasks==null) return null;
        for (TpaPreviewTask task:previewTasks){
//            调研相似度匹配函数对两个主题进行匹配，两个主题只要有一个匹配上了，该就算是相关作业
//            满足第一主题相似度大于等于95%就是相同的，先暂定95%，后续再调整
//            第二主题相似度大于等于90%，先暂定90%，后续再调整
            double firstTitle = TextCompare.getCosineSimilarity(task.getPtitle(), teachDesign.getSecondaryTitle()) * 100;
            double secondaryTitle = TextCompare.calculateNormalizedLevenshteinDistance(task.getSecondaryTitle(), teachDesign.getSecondaryTitle()) * 100;
            if (firstTitle >= 95 || secondaryTitle >= 90) {
                // 只要满足一个，该作业的知识点就和本次教学设计相关，就将这次作业的 id 记录下来
                previewTaskList.add(task);//不要和作业的那样只记录作业的id，这里直接将符合条件对象的所有属性都记录下来
            }
            // 比较 createTime 和当前时间，找出最近的 homework
            LocalDateTime createTime = task.getCreateTime();
            if (createTime != null) {
                Duration duration = Duration.between(createTime, LocalDateTime.now());
                long durationInSeconds = duration.toSeconds(); // 获取时间差（秒）

                if (durationInSeconds < minDuration) {
                    minDuration = durationInSeconds;
                    nearestPreviewTask = task; // 更新最近的 homework
                }
            }
//        记录下距离当前时间最近的一次作业
        }

        if(previewTaskList.size()==0) previewTaskList.add(nearestPreviewTask);
//      将previewTaskList中每一个对象的id抽取出来
        taskIds=previewTaskList.stream()
                .map(TpaPreviewTask::getPtId)
                .collect(Collectors.toList());
//       根据这个预习任务的id集合将符合条件的学生学习情况跟踪数据和预习题数据都拿出来
//        查找预习情况数据
        List<TpaPreviewTrack> taskBach = previewTrackMapper.findByIds(taskIds);
//        根据预习任务的id连表查找 预习题 以及 预习题完成情况 数据(这里面也包含推给各个学生的个性化题目和答题情况)
//        List<PreviewQuestionAndStudentAnswer> questionAndAnswer = detailsMapper.selectPreviewQuestionsAndStudentAnswers(taskIds);
//        先将该预习任务的所有题目获取出来（预习题、补充题后续再分开）
        List<TpaHomeworkDetails> tpaHomeworkDetails = detailsMapper.selectPreviewQuestions(taskIds);
//       将这些题目的id抽取出来
//        List<Integer> questionIds = tpaHomeworkDetails.stream()
//                .map(TpaHomeworkDetails::getQid)
//                .collect(Collectors.toList());
//        直接根据预习任务的ptId查找预习题的数据！！！！！！！！！！！！！！！！而不是根据题目的id，这里和作业一样，都根据预习任务id来匹配每一个预习题的完成数据，后面根据qid进行分组和这里使用什么将答题情况拿出来没有关系
        List<TpaHomeworkTrack> previewQuestionTracks = detailsMapper.selectPreviewQuestionsAnswer(taskIds);

//      先包装预习任务的数据，后面再包装个性化推荐的数据
//        1.预习任务的数据
//        对taskBach预习任务追踪数据根据任务的id进行分组
        Map<Integer, List<TpaPreviewTrack>> tasksMap = taskBach.
                stream().collect(Collectors.groupingBy(TpaPreviewTrack::getPtId));
//        对预习题进行分组进行分组，根据预习任务id进行分组
        Map<Integer, List<TpaHomeworkDetails>> taskQuestionMap = tpaHomeworkDetails.stream()
                .collect(Collectors.groupingBy(TpaHomeworkDetails::getPtId));
//       根据班级对预习任务进行分组
        Map<Integer, List<TpaPreviewTask>> previewTaskByCid = previewTaskList.stream().collect(Collectors.groupingBy(TpaPreviewTask::getCid));
//        遍历预习任务
        StringBuilder totalTxt=new StringBuilder();
        StringBuilder specialResources=new StringBuilder();
        totalTxt.append("预习任务--预习资料完成数据：\n");
        specialResources.append("预习任务个性化推荐资源完成数据：");
        int i=0;
//        遍历班级
        for (Integer cid:cids){
//            根据班级id1，将该班级的预习任务拿出来
            List<TpaPreviewTask> oneClassTasks = previewTaskByCid.getOrDefault(cid, new ArrayList<>());
            totalTxt.append("***").append(cidsAndName.get(cid)).append("班级的相关预习任务完成情况***\n");
            specialResources.append("***").append(cidsAndName.get(cid)).append("班级的相关个性化资源完成情况***\n");
            for (TpaPreviewTask previewTask:oneClassTasks){
//            拿出预习任务数据
                List<TpaPreviewTrack> tracks = tasksMap.getOrDefault(previewTask.getPtId(), new ArrayList<>());
                List<TpaHomeworkDetails> taskQuestions = taskQuestionMap.getOrDefault(previewTask.getPtId(), new ArrayList<>());
//            调用组装函数
                String taskTitle=previewTask.getPtitle()+"--"+previewTask.getSecondaryTitle();
                Map<Integer, List<TpaHomeworkDetails>> byQFrom = taskQuestions.stream()
                        .collect(Collectors.groupingBy(TpaHomeworkDetails::getQfrom));//将题目分为个性化题目和预习题区分出来
                List<TpaHomeworkDetails> previewQuestion = byQFrom.getOrDefault(2, new ArrayList<>());
                List<TpaHomeworkDetails> specialQuestion = byQFrom.getOrDefault(3, new ArrayList<>());
                String oneTaskDataTxt = processTasks(taskTitle,previewTask, tracks, previewQuestion,previewQuestionTracks,1);
                String oneTaskOtherDataTxt = processTasks(taskTitle, previewTask, tracks, specialQuestion, previewQuestionTracks, 2);
//            同时组装个性化资源数据
                if(i==0) {//第一次的时候开头不能有分隔符
                    totalTxt.append(oneTaskDataTxt);
                    specialResources.append(oneTaskOtherDataTxt);
                }
                else {
                    totalTxt.append("\n\n-------------------------------------------------------------------------------\n" + "-------------------------------------------------------------------------------\n").append(oneTaskDataTxt);

                    specialResources.append("\n\n-------------------------------------------------------------------------------\n" + "-------------------------------------------------------------------------------\n").append(oneTaskOtherDataTxt);
                }
                i++;
            }
            totalTxt.append("\n\n");
            specialResources.append("\n\n");
        }

        System.out.println(totalTxt);
        System.out.println(specialResources);
//          2.4将这个作业的数据放入 作业数据.txt 文件中
        File txtFile = NewTxtFile.newFile(totalTxt.toString(), teachDesign.getDesignName()+"教学设计的参考预习任务完成数据" );
        File txtFile1 = NewTxtFile.newFile(specialResources.toString(), teachDesign.getDesignName()+"教学设计的参考个性化推荐完成情况数据");
//        同时将另外一个个性化推荐数据的文件也组装上传
//        3.调用知识库新增文档的接口，获得文档的id，写入id的集合中
        String documentId = CozeKnowledgeUploader.uploadFileToKnowledgeBase(datasetId, txtFile);
        String documentId1 = CozeKnowledgeUploader.uploadFileToKnowledgeBase(datasetId, txtFile1);
//        将这两个文件的id记录下来，后续根据id进行删除
        documentIds.add(documentId);
        documentIds.add(documentId1);
//        4.获取两个文件上传进度，只有文件上传完成才可以进行下一步数据分析
        if(documentId!=null&&documentId1!=null){
            Integer progress = CozeUploadProgress.getProgress(datasetId, documentId);
            Integer progress1 = CozeUploadProgress.getProgress(datasetId,documentId1);
            while (progress!=null&&progress<100&&progress1!=null&&progress1<100){
                Thread.sleep(6000);
                progress= CozeUploadProgress.getProgress(datasetId, documentId);
                System.out.println("当前上传进度："+progress+"%");
            }
            System.out.println("文件上传完毕!");
        }
//        文件上传完毕后就可以进行数据分析了
        String question="请根据知识库中的两个文件：1、"+txtFile.getName()+" 2、"+txtFile1.getName()+",这两个文件中记录了一个老师教学的所有班级下的所有预习任务以及每一个学生对个性化任务的完成情况，请根据文件记录的预习任务完成情况进行数据分析\n" +
                "要求：1、请严格只参考我指定的知识库文件，不要参考文件名和我指定的不一样的文件\n2、预习任务完成数据的文件有资料完成情况和预习题完成情况，请分开进行分析\n" +
                "3、个性化推荐资源完成情况文件中如果没有学生的相关做题数据就不进行分析\n 4、生成的分析报告不要太长，并将所有的markdown的符号去掉\n" +
                "5、要对每一个班级的每一次预习任务的每一道预习题对应的所有学生的做题情况进行分析，并以班级为单位，再以每次预习任务的预习题目的完成数据为最小单位进行分析和总结\n" +
                "6、需要分析出每次预习任务学生对知识点的掌握情况、错题情况以及后续的教学设计编写、课堂教学方式和侧重点的建议\n7、文件中过的数据比较多，但是你生成的分析数据和结果需要简洁凝练直击要点。注意：不要生成太多内容";
//        5.调用数据分析接口，进行数据分析;将分析数据进行包装返回
        return chatWithModel.chatClient(question,teachDesign.getAuthorId().toString(),botId);
    }



    private String processTasks(String taskTitle, TpaPreviewTask task, List<TpaPreviewTrack> tracks, List<TpaHomeworkDetails> taskQuestion, List<TpaHomeworkTrack> previewQuestionTracks, int which){
        StringBuilder dataBuilder=new StringBuilder();
   //1、对预习资料情况进行包装
        dataBuilder.append(TimeUtils.TimeToString(task.getCreateTime())).append("  预习任务--").append(task.getPreviewName()).append("\n").append("预习任务涉及知识点：").append(taskTitle).append("'\n\n######\n预习任务完成情况：");
        Map<Integer, List<TpaHomeworkTrack>> trackByQid = previewQuestionTracks.stream().collect(Collectors.groupingBy(TpaHomeworkTrack::getQid));
        if(which==1){
            dataBuilder.append("\n1、预习资料完成情况：\n①预习资料原文：").append(task.getPreviewContent()).append("\n");
//            String[] additionalQuestion = task.getProblem().split("####");
            List<String> additionalQuestion = new Gson().fromJson(task.getProblem(), new TypeToken<List<String>>() {
            }.getType());
            dataBuilder.append("②预习资料附加题总分：").append(additionalQuestion.size() * 5).append("\n");
            dataBuilder.append("③预习资料附加题题干：（附加题他来分析预习资料掌握情况！！）\n");
            for (int i=0;i<additionalQuestion.size();i++){
                dataBuilder.append("附加题").append(i).append(":").append(additionalQuestion.get(i)).append("\n");
            }
            dataBuilder.append("\n");
            dataBuilder.append("- 每个学生附加题完成情况：(附加题答题情况中每道题的回答以“####”这个符号分隔)\n");
            for (TpaPreviewTrack track:tracks){
                if (track.getTextFinish()!=0||track.getQuestionFinish()!=0){//只有完成了预习任务的数据才可以被写在文件中
                    String isComplete=null;
                    List<String> textAnswer = new Gson().fromJson(track.getTextAnswer(), new TypeToken<List<String>>() {
                    }.getType());
                    if (textAnswer.size()==additionalQuestion.size()) isComplete="已完成";
                    else isComplete="未完成";
                    dataBuilder.append(track.getStuName()).append(":{是否完成附加题:").append(isComplete).append(",附加题答题情况：").append(track.getTextAnswer()).append(",预习资料附加题得分：").append(track.getTextScore()).append("}\n");
                }
            }
//            2、对预习任务的题目进行包装
            dataBuilder.append("######\n\n2、预习题完成情况：\n");
            dataBuilder.append("- 预习题总分：").append(task.getQuestionsGrade()).append("\n");
//        对所有的预习题根据qid进行分组
            dataBuilder.append("- 预习题以及每一位学生的完成情况：\n");
//        遍历预习题
            for (int i=0;i<taskQuestion.size()&&taskQuestion.get(i).getQfrom()==2;i++){
//            通过正在遍历的问题id来匹配该问题的所有学生回答情况
                List<TpaHomeworkTrack> oneQuestionTracks = AssembleFunction(taskQuestion, dataBuilder, trackByQid, i);
                if (oneQuestionTracks.size()==0){ dataBuilder.append("该班级暂时没有学生完成该题目");
                continue;}
                for (TpaHomeworkTrack track:oneQuestionTracks){
                    builderStuAnswer(dataBuilder, track);
                    dataBuilder.append(",错误原因：").append(track.getMistakeCase());
                    dataBuilder.append(",额外知识点补充：").append(track.getAddExplanation()).append("}\n\n");
                }
                dataBuilder.append("############\n\n");
            }
//           否则就是封装个性化数据
        }else {
//            外层遍历该任务的所有学生的完成记录
            Map<Integer, List<TpaHomeworkDetails>> bySid = taskQuestion.stream().collect(Collectors.groupingBy(TpaHomeworkDetails::getSid));
            for (TpaPreviewTrack track:tracks){
                dataBuilder.append("- 个性化资源所属：").append(track.getStuName()).append("\n");
                dataBuilder.append("- 个性化资源补充资源原文：\n[").append(track.getSupplement()).append("]\n\n");
                dataBuilder.append("- 个性化补充题目：").append(track.getAddQuestion()).append("个\n");
//               List<TpaHomeworkDetails> taskQuestion这个是根据预习任务的id获取的所有题目，既包括预习题，也包括补充题，所以可以根据这个将个性化题找出来
//               将个性化题目提取出来
                dataBuilder.append("- 个性化题目回答情况：\n");
                List<TpaHomeworkDetails> question = bySid.getOrDefault(track.getSid(), new ArrayList<>());
//                内层遍历该学生在该预习任务中获得的个性化题目
                for (int i=0;i<question.size();i++){
//                    这个其实只有一条数据，因为这个补充题是独属于该学生的，只有该学生做了该个性化题目
//                    List<TpaHomeworkTrack> oneQuestionTracks = AssembleFunction(taskQuestion, dataBuilder, trackByQid, i);
                    List<TpaHomeworkTrack> oneQuestionTracks = trackByQid.getOrDefault(question.get(i).getQid(), new ArrayList<>());
                    dataBuilder.append("- 个性化题目").append(i+1).append("：题干：").append(question.get(i).getQcontent()).append("\n");
//                    ProblemHeader(taskQuestion, dataBuilder, i);
                    if(question.get(i).getQtype().contains("选择题")){
                        dataBuilder.append("- 题目选项：").append(question.get(i).getSelections()).append("\n");
                    }
                    dataBuilder.append("- 题目解析：").append(question.get(i).getAnswerAnalysis()).append("\n");
                    dataBuilder.append("- 题目分数：").append(question.get(i).getDefaultScore()).append("\n");
                    dataBuilder.append("- 该个性化题目学生答题情况：\n");
                    assert oneQuestionTracks != null;
                    if (oneQuestionTracks.size()==0) {dataBuilder.append("- 该学生未完成个性化题目\n\n");continue;}
                    dataBuilder.append("学生").append(track.getStuName()).append(":{").append("完成情况：");
                    if(oneQuestionTracks.get(0).getStatus()==0) dataBuilder.append("未开始");
                    else dataBuilder.append("已完成");
                    dataBuilder.append(",该学生的回答：").append(oneQuestionTracks.get(0).getAnswer());
                    dataBuilder.append(",获得的分数：").append(oneQuestionTracks.get(0).getScore());
                    dataBuilder.append(",解题用时：").append(oneQuestionTracks.get(0).getTimeSpent());
                    dataBuilder.append(",是否正确：").append(oneQuestionTracks.get(0).getIsCorrect());
                    dataBuilder.append(",错误原因：").append(oneQuestionTracks.get(0).getMistakeCase());
                    dataBuilder.append(",额外知识点补充：").append(oneQuestionTracks.get(0).getAddExplanation()).append("}\n");
                    dataBuilder.append("############\n\n");
                }
                dataBuilder.append("===========\n\n");
            }
        }
        return dataBuilder.toString();
    }

    private List<TpaHomeworkTrack> AssembleFunction(List<TpaHomeworkDetails> taskQuestion, StringBuilder dataBuilder, Map<Integer, List<TpaHomeworkTrack>> trackByQid, int i) {
        List<TpaHomeworkTrack> oneQuestionTracks = trackByQid.getOrDefault(taskQuestion.get(i).getQid(), new ArrayList<>());
        if (oneQuestionTracks.size()==0) return null;
        dataBuilder.append("预习题").append(i+1).append("：题干：").append(taskQuestion.get(i).getQcontent()).append("\n");
        ProblemHeader(taskQuestion, dataBuilder, i);
        dataBuilder.append("预习题").append(i+1).append("的学生答题情况：\n");
        return oneQuestionTracks;
    }


    /**
     * 上传互动互数据在进行分析（使用最近三次教学的互动数据进行分析）
     * @param teachDesign
     * @param history
     * @return
     */
    private String  analyzeInteractionData(TpaTeachDesign teachDesign, List<Message> history) throws Exception {
//        1.从数据库中匹配数据
//        先根据教师的id找出他最近写的三次教学设计的id
            QueryWrapper<TpaTeachDesign> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("author_id",teachDesign.getAuthorId());
            queryWrapper.orderByDesc("create_time").last("LIMIT 3");
        List<TpaTeachDesign> tpaTeachDesigns = designMapper.selectList(queryWrapper);
        List<Integer> ids = tpaTeachDesigns.stream().map(TpaTeachDesign::getTdId)
                .collect(Collectors.toList());
//        根据教学设计的id查找该教学设计的互动数据
//        List<TpaInteraction> interactions = interactionMapper.selectBatchIds(ids);
        List<TpaInteraction> interactions = interactionMapper.selectByDesignIds(ids);
//        对这个互动数据根据教学设计的id进行分组
        Map<Integer, List<TpaInteraction>> interactionMap = interactions.stream().collect(Collectors.groupingBy(TpaInteraction::getTdId));
//        对三个教学设计
        StringBuilder dataTxt=new StringBuilder();
        dataTxt.append("最近三堂课的互动数据：\n");
        for (TpaTeachDesign design:tpaTeachDesigns){
//            先将本堂课的所有互动题目及相应的互动数据抽取出来
            List<TpaInteraction> oneInteraction = interactionMap.getOrDefault(design.getTdId(), new ArrayList<>());
            dataTxt.append("学科：").append(design.getSubject()).append("\n");
            dataTxt.append("人工智能通识课课堂授课章节：").append(design.getDesignTitle()).append("\n")
                    .append("课程小节知识点：").append(design.getSecondaryTitle()).append("\n");
            dataTxt.append("课堂题目及每一题的互动数据：\n");
//            遍历互动题目
            for (TpaInteraction interaction:oneInteraction){
                dataTxt.append("互动方式：").append(interaction.getMethod()).append("\n");
                if (interaction.getMethod().contains("问答式互动")){
                    dataTxt.append("问答式互动的互动题目：").append(interaction.getContent()).append("\n");
                    dataTxt.append("互动题目的答案：").append(interaction.getAnswer()).append("\n");
                }else {
                    dataTxt.append("互动内容：").append(interaction.getContent()).append("\n");
                }
                dataTxt.append("互动数据：{").append("学生回答(主观题就记录学生回答的文字,使用#隔离；客观题就记录对错比例格式为：12/2/1，最右边是没有回答的人数)")
                        .append(interaction.getReplay()).append(",有效观点数:").append(interaction.getViewpointNum())
                        .append("，参与人数：").append(interaction.getParticipates()).append(",答对人数：").append(interaction.getCorrect())
                        .append(",学生提问：").append(interaction.getAskContent()).append(",讨论时长：").append(interaction.getDeadline()).append("}\n");
            }
            dataTxt.append("\n#######");
        }

//        2.将数据变为对象字符串的形式，写入text文件中，并将文件进行上传
        Map<String,String> fileName = upload(dataTxt, teachDesign.getDesignName()+"教学设计的参考课堂互动数据");
//        4.获取文件上传进度，只有文件上传完成才可以进行下一步数据分析
//        5.调用数据分析接口，进行数据分析
//        6.将分析数据进行包装返回
        String question="请根据知识库中的一个文件："+fileName.get("name")+"的文件中记录的最近几次课堂教学记录的教师-学生互动数据,请对数据进行分析\n" +
                "要求：1、请严格只参考我指定的知识库文件，不要参考文件名和我指定的不一样的文件\n2、如果还文件中没有相关的互动数据，就不分析，如果有请生成互动分析报告\n" +
                "3、分析需要包含互动效果分析、互动活跃度分析、互动改进方式和建议、本次教学设计的详细互动方案推荐\n4、生成的整体文字不要太多";

//        5.调用数据分析接口，进行数据分析;将分析数据进行包装返回
        return chatWithModel.chatClient(question,teachDesign.getAuthorId().toString(),botId);
    }

    public static Map<String,String> upload(StringBuilder dataTxt,String fileName) throws Exception {
        File file = NewTxtFile.newFile(dataTxt.toString(), fileName);
//        3.调用知识库新增文档的接口，获得文档的id，写入id的集合中
        String documentId = CozeKnowledgeUploader.uploadFileToKnowledgeBase(datasetId,file);
//        将这个文件的id记录下来，后续根据id进行删除
        documentIds.add(documentId);
//        4.获取文件上传进度，只有文件上传完成才可以进行下一步数据分析
        if(documentId!=null){
            Integer progress = CozeUploadProgress.getProgress(datasetId,documentId);
            while (progress!=null&&progress<100){
                Thread.sleep(6000);
                progress= CozeUploadProgress.getProgress(datasetId, documentId);
                System.out.println("当前上传进度："+progress+"%");
            }
            System.out.println("文件上传完毕!");
        }
        HashMap<String,String> nameAndId=new HashMap<>();
        nameAndId.put("name",file.getName());
        nameAndId.put("id",documentId);
        return nameAndId;
    }

    //根据两级主题来预测教学设计包含的内容
    private String generatePrediction(TpaTeachDesign teachDesign, List<Message> history) throws Exception {
        String question="本次教学设计的知识点是："+teachDesign.getDesignTitle()+"--"+teachDesign.getSecondaryTitle()+",请根据对知识库中关于作业完成情况数据、预习任务完成情况数据的分析以及本次教学设计教授的知识点来对本次教学设计的编写提一些意见和教学侧重点，" +
                "同时生成知识点分析、学生以往知识点储备情况分析以及本次教学可能遇到的困难和解决措施";
        return chatWithModel.chatClient(question,teachDesign.getAuthorId().toString(),botId);
    }
//删除本次数据分析的知识点库，防止下次分析使用旧的数据
    private void deleteKnowledgeBase() {
//         根据ids中记录的文档id，调用删除知识库文件的接口，将本次分析使用到的数据进行删除
        if (documentIds.size()!=0){
            Integer code = CozeFileDelete.deleteFile(documentIds);
            if(code==0) System.out.println("文件清除成功！");
        }
    }

    private void updateSessionHistory(List<Message> messageHistory, AnalysisResult result) {
        if (result.getMessageHistory() != null) {
            messageHistory.addAll(result.getMessageHistory());
        }
    }

}

