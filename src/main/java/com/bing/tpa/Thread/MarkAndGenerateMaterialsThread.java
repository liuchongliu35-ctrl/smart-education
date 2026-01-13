package com.bing.tpa.Thread;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bing.tpa.domain.entity.*;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.*;
import com.bing.tpa.modelcall.designCall.ChatWithModel;
import com.bing.tpa.service.baseImpl.TpaHomeworkServiceImpl;
import com.bing.tpa.utils.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 该线程池用于批改作业并根据预习任务的完成情况生成个性化资源
 */

@Component
@Slf4j
public class MarkAndGenerateMaterialsThread {

    @Autowired
    private TpaHomeworkTrackMapper trackMapper;

    @Autowired
    private TpaHomeworkDetailsMapper detailsMapper;

    @Autowired
    private ChatWithModel chatWithModel;

    @Autowired
    private TpaPreviewTrackMapper previewTrackMapper;

    @Autowired
    private TpaPreviewTaskMapper taskMapper;
    @Autowired
    private TpaHomeworkMapper homeworkMapper;

    @Autowired
    private TpaHomeworkSummaryMapper homeworkSummaryMapper;

    private static final String analysisBotId="7474540258005811210";

    private static final String informationBotId="7475340560941809690";

    private static Integer upCompeleteNum=0;


//    预习任务批改和个性化数据生成线程
    @Async("markAndGenerate")
    public void gradeAndGenerateDate(Integer uid, Integer ptId, Integer select, boolean isFinish) throws Exception {
        if(isFinish){
            List<TpaHomeworkTrack> questionTracks = markingBySelect(uid, ptId, select);//这里如果是进行作业的批改，在批改完并将结果更新到数据库后，将批改结果返回，这样就不用再到数据库中拿
//            生成个性化推荐数据.....
//            根据uid和ptId搜索该学生预习题的完成情况以及预习资料完成情况
//                QueryWrapper<TpaPreviewTrack> queryWrapper=new QueryWrapper<>();
//                queryWrapper.eq("sid",uid).eq("pt_id",ptId);
//                TpaPreviewTrack track = previewTrackMapper.selectOne(queryWrapper);
//                track.getStuName()
//            获取预习任务完成情况以及学生姓名：注意姓名姓名姓名！！！！后面要以姓名来找知识库中的文件
            TpaPreviewTrack track = previewTrackMapper.selectOneAndName(uid, ptId);
//            如果预习题的批改记录为null就需要获取预习题的情况
            if (questionTracks==null||questionTracks.size()==0){
                QueryWrapper<TpaHomeworkTrack> queryWrapper1=new QueryWrapper<>();
                queryWrapper1.eq("sid",uid).eq("pt_id",ptId);
                questionTracks = trackMapper.selectList(queryWrapper1);
            }
//            将以上数据进行整合，包括预习题目，预习资料，学生的的各项·完成情况写到一个文件中，在将文件上传知识库，让智能体根据知识库进行分析和个性化推荐，再将推荐的资源写到数据库中
            analysisAllPreviewData(questionTracks,track,uid,ptId);

        }else {
//            只批改，不生成个性化数据，因为两个其中有一个没有完成
            markingBySelect(uid, ptId, select);
        }

    }

//批改预习题或者预习资料附加题
    private List<TpaHomeworkTrack> markingBySelect(Integer uid, Integer ptId, Integer select) {
        if (select==2) {
//                 只批改预习题，并将批改数据记录在homework_track中
            List<TpaHomeworkTrack> tracks = null;
            try {
                tracks = markingQuestion(uid, ptId, 1);
                double totalScore = tracks.parallelStream().mapToDouble(track -> track.getScore()).sum();
                previewTrackMapper.updateScore(totalScore,ptId,uid,tracks.size()-upCompeleteNum);//更新预习题的总得分
            } catch (Exception e) {
                e.printStackTrace();
            }
            return tracks;
            //批改完之后程序就会结束，不生成个性化数据
        }else if (select==1){
//                只批改预习资料题目，记录在preview_track表中
            try {
                markTextQuestion(uid,ptId);
            } catch (Exception e) {
                e.printStackTrace();
            }
//              批改完成后程序就会结束，不生成个性化数据
        }
        return null;
    }

    //    作业题目批改线程
    @Async("marking")
    public  void markHomework(Integer uid, Integer hid) throws Exception {
        List<TpaHomeworkTrack> tracks=new ArrayList<>();
//     根据uid和hid将本次作业学生做的每一到题的情况拿出来，进行批改
        try {
            tracks= markingQuestion(uid, hid, 0);//返回所有题目的批改记录
//            将批改的总分更新到summary表中
            double totalScore = tracks.parallelStream().mapToDouble(track -> track.getScore()).sum();
            homeworkSummaryMapper.updateScore(uid,hid,totalScore);//更新总分
        } catch (Exception e) {
            e.printStackTrace();
        }
//        获取作业情况
        TpaHomework homework = homeworkMapper.selectById(hid);
//        获取作业完成概况
        QueryWrapper<TpaHomeworkSummary> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("hid",hid).eq("uid",uid);
        TpaHomeworkSummary summary = homeworkSummaryMapper.selectOne(queryWrapper);
//        获取该作业的所有题目
        List<TpaHomeworkDetails> questionByHId = detailsMapper.getQuestionByHId(hid);
//      根据批改结果让AI生成作业报告！！！！！！！！
//        获取作业的数据：作业题目，该学生每道题的完成情况
        StringBuilder dataBuilder=new StringBuilder();
        dataBuilder.append("以下数据是"+summary.getName()+"学生的一份课后作业完成数据：\n");
        dataBuilder.append("1、作业名称：").append(homework.getHName()).append("\n")
                .append("2、作业涉及知识点：").append(homework.getHTitle()).append("--").append(homework.getSecondaryTitle()).append("\n")
                .append("3、作业整体完成情况：{作业完成用时").append(summary.getCompleteTime()).append("，作业总得分：").append(summary.getScore())
                .append(",完成的题目数：").append(summary.getCompleteQuestion()).append(",作业总题目数").append(summary.getQuestionNum()).append("\n")
                .append("4、作业各题目完成情况：").append("\n");
        int i=0;
        for(TpaHomeworkDetails details:questionByHId){
//            先将题目放进去
            dataBuilder.append("题目").append(i).append(":题干：").append(details.getQcontent()).append("\n");
            if (details.getSelections()!=null) dataBuilder.append("选择题选项：").append(details.getSelections());
            dataBuilder.append("题目标准答案：").append(details.getCorrectAnswer()).append("\n")
                    .append("题目分数：").append(details.getDefaultScore()).append("\n");
//            获取这个题目的回答情况和批改情况
            Optional<TpaHomeworkTrack> oneTrack = tracks.stream()
                    .filter(track1 -> Objects.equals(track1.getQid(), details.getQid()))
                    .findFirst();
            UserAnswerBuilder(dataBuilder, oneTrack);
            i++;
        }
        System.out.println(dataBuilder);
//        作业数据上传知识库
       Map<String,String>  fileNameAndId= AnalysisDataThread.upload(dataBuilder, summary.getName() + "学生完成" + homework.getHName() + "作业情况数据_");
//        请求生成作业报告
        String request="请根据的知识库中的一个文件："+fileNameAndId.get("name")+",这个文件记录了该学生的本次人工智能通识课作业完成情况数据，请根据数据来进行学情分析，并根据分析结果生成一份作业完成报告，报告应该包含知识点的掌握情况总结，薄弱点总结，错题总结以及对该学生的个性化辅导意见\n" +
                "要求：1、请只参考我指定的知识库文件，不要参考文件名和我指定的不一样的文件，\n" +
                "2、作业分析报告要尽量详细，从百度等网站查询一些相关的学习资源链接，也可以从bilibili获取一些相关的学习视频链接\n" +
                "3、注意请仔细搜索知识库中我指定的文件，并认真看文件中的内容，内容不可能是关于心理健康的测试题目，而是关于人工智能通识课的作业，作业涉及知识点为："
                +homework.getHTitle()+"--"+homework.getSecondaryTitle()+"，请仔细读取该文件的内容\n" +
                "4、不要返回NULL给我";
        String report = chatWithModel.chatClient(request, uid.toString(), analysisBotId);
        summary.setReport(report);
//        将已经分析过的文件删除掉
        Integer code = CozeFileDelete.deleteFile(Collections.singletonList(fileNameAndId.get("id")));
        if(code==0) System.out.println("文件清除成功！");
        homeworkSummaryMapper.updateById(summary);//将AI生成的报告储存到数据库中
    }

    private void UserAnswerBuilder(StringBuilder dataBuilder, Optional<TpaHomeworkTrack> oneTrack) {
        oneTrack.ifPresent(tr->{
            dataBuilder.append("学生回答:").append(tr.getAnswer()).append("\n")
                    .append("获取的评分：").append(tr.getScore()).append("\n解题用时：").append(tr.getTimeSpent())
                    .append("\n是否正确:");
            if (tr.getIsCorrect()==1) dataBuilder.append("正确");
            else dataBuilder.append("错误");
            dataBuilder.append("\n\n");
        });
    }


    //    根据完成数据以及作业内容，上传文件，获得个性化推荐，并将推荐内容储存到数据库中
//    该方法需要生成三种数据：1、个性化推荐题目 2、个性化推荐阅读资源 3、对预习任务完成情况的分析以及对老师辅导学生的建议
    private void analysisAllPreviewData(List<TpaHomeworkTrack> questionTracks, TpaPreviewTrack track, Integer uid, Integer ptId) throws Exception {
//        获取预习任务信息
        TpaPreviewTask task = taskMapper.selectById(ptId);
//        获取预习题
        List<TpaHomeworkDetails> questionByPtId = detailsMapper.getQuestionByPtId(ptId);
//        组装请求
        StringBuilder request = new StringBuilder();
        request.append("以下是一位学生人工智能通识课预习任务的完成数据：").append("\n");
        request.append("1、预习任务名称：").append(task.getPreviewName()).append("\n")
                .append("2、预习任务知识点：").append(task.getPtitle()).append("--").append(task.getSecondaryTitle()).append("\n")
                .append("3、预习任务整体完成情况：{预习资料得分:").append(track.getTextScore())
                .append(",预习用时:").append(TimeUtils.getTimeDeff(track.getStartTime(), track.getFinishTime())).append(",预习题得分：")
                .append(track.getQuestionScore()).append(",预习题数量：").append(track.getQuestionNum()).append(",完成的预习题数量：").append(track.getCompleteNum())
                .append("}\n");
        request.append("4、预习任务项及各项完成情况:").append("(1)预习资料项:").append("\n①预习资料内容：").append(task.getPreviewContent()).append("\n②关于资料的提问：")
                .append(track.getDataInquiry()).append("\n③预习资料附加题及完成情况:\n");
        HashMap<String, String> questionAndAnswerMap = previewDataToMap(task, track);
        int i = 0;
        for (Map.Entry<String, String> qaMap : questionAndAnswerMap.entrySet()) {
            request.append("题目" + i + ":题干:").append(qaMap.getKey())
                    .append("\n答题情况：").append(qaMap.getValue()).append("\n\n");
            i++;
        }
        request.append("\n\n\n(2)预习题项：\n");
        int j = 0;
        for (TpaHomeworkDetails details : questionByPtId) {
            request.append("题目" + j + ":题干:").append(details.getQcontent()).append("\n");
            if (details.getSelections() != null) request.append("选择题选项:").append(details.getSelections());
            request.append("题目标准答案：")
                    .append(details.getCorrectAnswer()).append("\n");
//            获取这道题的答题情况
            Optional<TpaHomeworkTrack> oneTrack = questionTracks.stream()
                    .filter(track1 -> Objects.equals(track1.getQid(), details.getQid()))
                    .findFirst();
            UserAnswerBuilder(request, oneTrack);//组装学生的答题情况
            request.append("\n\n");
            j++;
        }
//        数据上传知识库
        Map<String, String> upload = AnalysisDataThread.upload(request, track.getStuName() + "学生完成" + task.getPreviewName() + "预习任务情况数据");
//        调用AI生成资源的线程，有三个线程执行：
        AISendExecutor(track, task, uid, ptId);
        System.out.println(task.getPtitle());
        System.out.println(task.getSecondaryTitle());
        List<String> ids=new ArrayList<>();
//        生成题目
        try {
            String request2 = "请根据知识库中的一个文件:" + track.getStuName() + "学生完成" + task.getPreviewName() + "预习任务情况数据,这个文件记录了该学生的本次预习任务完成情况的数据" +
                    "输出要求：\n1、请根据文件中的数据只生成针对性的可以提升该学生薄弱点的个性化针对性题目或者补充进阶题目，且题目之间一定要使用 || 这个符号隔开！！\n2、生成10个题目，题目包含单选题、多选题、填空题 \n" +
                    "3、题目总分为100分 \n4、题目的难度需求为简单题+中等题+进阶" + "\n5、每道题的分值根据前面指定的总分和题目数量，并根据题目难度和题型来进行分配，不要均分 \n" +
                    "6、生成的每道题的分值我只要一个单独的数字，不要给分值前后加空格或者汉字\n7、如果题目是选择题，你生成的答案就必须是A、B、C、D这样的选项\n" +
                    "8、选择题、填空题和简答题都一定要生成解析！\n9、注意：一定不要生成相同的题目！！即每道题的题干、答案、解析都不能相同" +
                    "\n10、选择题的题目类型请分为单选题和多选题，不要使用“选择题”这个来作为题目的类型\n11、注意：题目的知识点不要重复，可以生成拓展知识点的题目" +
                    "\n13、生成与“"+task.getSecondaryTitle()+"”这个知识点有关的10道补充题目给该学生学习，一定不要生成NULL,不要生成NULL！！";
            String specialQuestion = chatWithModel.chatClient(request2, track.getSid().toString(), analysisBotId);
            String sq = specialQuestion.replace("\\", "");
            List<TpaHomeworkDetails> details = new ArrayList<>();
            String[] question = sq.split("\\|\\|");
            List<String> questions1 = new ArrayList<>(List.of(question));
            questions1.remove(""); //将空串移除
            TpaHomeworkServiceImpl.StringToBean(details, questions1);
            TpaHomeworkServiceImpl.formOptions(details);//处理选项
            for (TpaHomeworkDetails details1 : details) {
//            设置每一个题目的所属，包括学生id，预习任务id
                details1.setSid(uid);
                details1.setQfrom(3);
                details1.setCreateTime(CurrentTime.getTime());
                details1.setSubject("个性化补充题");
                details1.setPtId(ptId);
                if (details1.getOptions() != null) {
                    details1.setSelectOption(new Gson().toJson(details1.getOptions()));
                }
                detailsMapper.insert(details1);//将这个题目更新到数据库中
            }
//            删除知识点库文件

            ids.add(upload.get("id"));
            Integer code = CozeFileDelete.deleteFile(ids);
            if(code==0) System.out.println("文件清除成功！");
            track.setAddQuestion(details.size());//设置补充题目的数量
            previewTrackMapper.updateById(track);
        }catch (Exception e){
            if (ids.size()!=0){
                 CozeFileDelete.deleteFile(ids);
                System.out.println("文件清除成功！");
            }
            e.printStackTrace();
        } finally{
            log.info("题目已生成完毕");
        }
    }

    private void AISendExecutor(TpaPreviewTrack track,TpaPreviewTask task,Integer uid,Integer ptId){
        ScheduledExecutorService executor= Executors.newScheduledThreadPool(3);
        AtomicReference<String> analysisResult = new AtomicReference<>("");
        AtomicReference<String> questionResult = new AtomicReference<>("");
        AtomicReference<String> infoResult = new AtomicReference<>("");

        try {
            executor.schedule(()->{
                String request1="请根据知识库中的一个文件："+track.getStuName()+"学生完成"+task.getPreviewName()+"预习任务情况数据,这个文件记录了该学生在本次人工智能通识课-"+task.getSecondaryTitle()+"的预习任务中完成情况的数据，请根据文件中的数据对该学生的预习情况进行学情分析,并生成几条老师辅导该学生的建议，\n限制：1、建议可以包含多方面要具体详细\n" +
                        "5、生成的内容一定要和"+task.getPtitle()+"--"+task.getSecondaryTitle()+"这个知识点相关的内容和学生完成该预习任务的学情分析的内容，一定不要生成其他不相关的内容";
                try {
                    String analysis = chatWithModel.chatClient(request1, track.getSid().toString(), analysisBotId);
                    track.setAiAnalysis(analysis);//设置预习任务完成情况AI分析
                    analysisResult.set(analysis);
                    log.error("学情分析执行");
                    previewTrackMapper.updateById(track);
                } catch (Exception e) {
                    log.error("学情分析线程异常", e);
                }
            },0, TimeUnit.SECONDS);

//            executor.schedule(()->{
//
//
//                } catch (Exception e) {
//                    log.error("题目生成线程异常", e);
//                }
//            },1, TimeUnit.SECONDS);

            executor.schedule(()->{
                try {
                    String  request3="请根据知识库中的一个文件:"+track.getStuName()+"学生完成"+task.getPreviewName()+"预习任务情况数据,这个文件记录了该学生在本次人工智能通识课-"+task.getSecondaryTitle()+"的预习任务中完成情况的数据，请根据文件中的记录数据进行学情分析，分析出该学生的知识薄弱点，并生成相关的资料进行补充，资料要求：1、资料要尽量丰富能够涵盖学生的知识盲区和掌握的不好的知识点\n " +
                            "2、补充资料应包括：补充知识点，易错案例，拓展资源、推荐读物、多媒体资源等\n " +
                            "3、生成的网络资源链接必须是有效的，可以从百度等网站获取补充资源,补充资料可以不必太长，只要可以全面的反映学生的薄弱点并进行相关资源的补充即可\n4、生成的视频或网页资源请以链接的形式生成给我\n" +
                            "5、生成的内容一定要和"+task.getPtitle()+"--"+task.getSecondaryTitle()+"这个知识点相关的内容，一定不要生成其他不相关的内容";
                    String addInformation = chatWithModel.chatClient(request3, track.getSid().toString(), informationBotId);
                    track.setSupplement(addInformation);//设置额外补充资料
                    infoResult.set(addInformation);
                    previewTrackMapper.updateById(track);
                } catch (Exception e) {
                    log.error("资料补充线程异常", e);
                }
            },2, TimeUnit.SECONDS);
        }finally {
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
        // 继续后续业务逻辑（此处可添加回调或阻塞等待）
        log.info("学情分析结果：{}", analysisResult.get());
        log.info("题目生成结果：{}", questionResult.get());
        log.info("补充资料结果：{}", infoResult.get());
    }



    /**
     * 批改预习资料中的几道题
     */
    private void markTextQuestion(Integer uid,Integer ptId) throws Exception {
        /*根据uid和ptId找到预习任务资料题目完成情况*/
//        根据ptId获取预习资料的内容
        TpaPreviewTask task = taskMapper.selectById(ptId);
        QueryWrapper<TpaPreviewTrack> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("sid",uid).eq("pt_id",ptId);
        TpaPreviewTrack track = previewTrackMapper.selectOne(queryWrapper);
//        将题目和回答进行分离，使用map进行组装
        HashMap<String, String> questionAndAnswer = previewDataToMap(task, track);
//        组装请求
        StringBuilder request=new StringBuilder();
        request.append("以下是一份预习资料以及和预习资料相关的题目还有一个学生的题目回答情况：\n")
                .append("预习资料的内容：").append(task.getPreviewContent()).append("\n")
                .append("下面是题目以及该学生的答题情况：\n");
        for (Map.Entry<String,String> qaMap: questionAndAnswer.entrySet()){
            request.append("题目：").append(qaMap.getKey()).append("\n")
                    .append("学生回答：").append(qaMap.getValue()).append("\n\n");
        }
        request.append("学生对预习资料的疑问：").append(track.getDataInquiry()).append("\n");
        request.append("请根据以上预习资料的内容以及附加题目还有学生的回答来对本次预习资料完成情况进行评分，每一道附加题是5分，提问可以加分，总分为100，" +
                "\n请返回一个数据：预习资料完成情况的评分,只要一个分数不要生成其他任何文字\n输出要求：1、注意：一定不要生成NULL！！，一定要生成一个数字作为分数\n2、尽量不要给100分\n" +
                "3、我给的数据不可能是一个心理健康的评估问卷的一部分，是一个学生完成预习任务的数据，一定不要在生成和心理健康评估相关的内容！\n4、我只要一个该学生完成以上预习任务的一个分数！！");
        String analysis = chatWithModel.chatClient(request.toString(), uid.toString(), analysisBotId);
        int score =0;
        if (Objects.equals(analysis, "NULL")||analysis==null){
            score=76;
        } else{
            score=Integer.parseInt(extractNumbers(analysis));
        }//将返回的结果中奖分数这个数字抽取算出来
            if (score>=0&&score<=100){
                track.setTextScore(score);
            }
            else throw new FormatException("分数格式错误");
//            将结果更新到数据库中，这个是该方法最终的目标！！！！！！！
            previewTrackMapper.updateById(track);//这个track是我从数据库中查出来的，所以肯定有主键，所以可以根据id进行更新
    }

    @NotNull
    private HashMap<String, String> previewDataToMap(TpaPreviewTask task, TpaPreviewTrack track) {
        List<String> questionList = new Gson().fromJson(task.getProblem(), new TypeToken<List<String>>() {}.getType());
        List<String> answerList=new Gson().fromJson(track.getTextAnswer(),new TypeToken<List<String>>() {}.getType());
        HashMap<String,String> questionAndAnswer=new HashMap<>();
        for (int i=0;i<questionList.size();i++){
//            以题目为key，以学生回答为value
            questionAndAnswer.put(questionList.get(i),answerList.get(i));
        }
        return questionAndAnswer;
    }

//    提取分数
public static String extractNumbers(String str) {
    StringBuilder result = new StringBuilder();
    Pattern pattern = Pattern.compile("\\d+"); // 匹配连续的数字
    Matcher matcher = pattern.matcher(str);

    while (matcher.find()) {
        result.append(matcher.group()).append(" "); // 将找到的数字追加到结果中
    }

    return result.toString().trim(); // 返回结果并去掉多余的空格
}

    @Data
    static
    class ShortQuestion{
//        题目完成情况跟踪id
        private Integer htId;
//        题干
        private String question;
//        学生的答案
        private String stuAnswer;
//        标准答案
        private String correctAnswer;
//        该题目的分数
        private Integer score;
    }
    /**
     * 批改题目的方法
     */
    private List<TpaHomeworkTrack> markingQuestion(Integer uid, Integer tid,Integer select) throws Exception {
//        记录简答题，后续同时发给AI进行同时批改
        List<ShortQuestion> questionList=new ArrayList<>();
//        将预习题的批改结果进行返回
        List<TpaHomeworkTrack> homeworkTrackList=new ArrayList<>();

//        将第一批未批改的简答题记录下来
        List<TpaHomeworkTrack> shortQuestionList=new ArrayList<>();
//        根据select来选择搜索作业题的完成情况还是预习题的完成情况
//            tid为作业的id
        if (select==0){
//          先将该学生的这个作业的每道题的完成情况从tpa_homework_track表中拿出来
//          获取作业题的标准答案和题目的分值
            List<TpaHomeworkDetails> questionByHId = detailsMapper.getQuestionByHId(tid);
//          遍历题目，同时根据题目id和学生的id找出该学生完成该题目的情况，该表中可能会出现一道题出现还几条数据，但是对应的是不同学生完成的，所可以根据qid+sid找到对应该学生的唯一完成数据
            for (TpaHomeworkDetails details:questionByHId){
                TpaHomeworkTrack track = markOneQuestion(uid, questionList, details);
                if (track==null) {
                    log.error("数据库没有该学生回答"+details.getQcontent()+"这道题的记录");
                } else {
                    if(track.getIsCorrect()==null) shortQuestionList.add(track);
                    else {homeworkTrackList.add(track);
                        if (track.getAnswer()==null){
                            upCompeleteNum++;
                        }
                    }
                }
            }

        }else {//            tid为预习题的id
//          先将该学生的这个预习任务的每道预习题的完成情况从tpa_homework_track表中拿出来
//          获取预习题标准答案和题目的分值
            List<TpaHomeworkDetails> questionByPtId = detailsMapper.getQuestionByPtId(tid);//这里只需要获取预习题即可，不要获取个性化推荐的题目
//          根据qid和uid获取该学生完成该预习任务的每一道预习题的情况
            for (TpaHomeworkDetails details:questionByPtId){
                TpaHomeworkTrack track = markOneQuestion(uid, questionList, details);
                if (track==null) {
                    log.error("数据库没有该学生回答"+details.getQcontent()+"这道题的记录");
                }else {
                    if(track.getIsCorrect()==null) shortQuestionList.add(track);
                    else {homeworkTrackList.add(track);
                        if (track.getAnswer()==null){
                            upCompeleteNum++;
                        }
                    }
                }
            }
        }
//                处理简答题并将将简答题的答题情况更新到数据库中,简答题就先不生成错误原因和额外推荐资料了
//            将收集到的所有简答题一次性进行AI批改！！！
        if (questionList.size()!=0){
            HashMap<Integer, Integer> shortQuestionScore = markingShortByAi(questionList);
            for (Map.Entry<Integer,Integer> map:shortQuestionScore.entrySet()){
                Optional<TpaHomeworkTrack> first = shortQuestionList.stream().filter(q -> Objects.equals(q.getHtId(), map.getKey())).findFirst();
                first.ifPresent(q->{
                    q.setScore(map.getValue());
                    if (map.getValue()<=1) q.setIsCorrect(0);
                    else q.setIsCorrect(1);
                    trackMapper.updateById(q);
                    homeworkTrackList.add(q);//将简答题的答题结果记录在一个list数组中，后续根据这个进行作业分析
                });
//                TpaHomeworkTrack track = new TpaHomeworkTrack();
//                track.setHtId(map.getKey());
//                track.setScore(map.getValue());
//                if (map.getValue()==0) track.setIsCorrect(0);
//                else track.setIsCorrect(1);

            }
        }
        return homeworkTrackList;
    }

//      批改一道题！！！！！！！
    private TpaHomeworkTrack markOneQuestion(Integer uid, List<ShortQuestion> questionList, TpaHomeworkDetails details) throws Exception {
        //                根据qid和sid查询该学生完成该题目的情况
        TpaHomeworkTrack trackByQidAndSid = getTrackByQidAndSid(uid, details.getQid());
        if (trackByQidAndSid==null) return null;//如果这个题目该学生没有做，就直接将答案为空的对象返回不继续批改
//                对比答案进行批改,分不同的情况进行批改，选择题和填空题采用精准匹配，简答题采用AI进行评分
        if (trackByQidAndSid.getStatus()==2){       //只有该题目是完成了的，才进行批改，不然就不批改
            if (details.getQtype().contains("简答题")) {
                ShortQuestion question = new ShortQuestion();
                question.setHtId(trackByQidAndSid.getHtId());//设置这条题目完成记录的id
                question.setStuAnswer(trackByQidAndSid.getAnswer());
                question.setCorrectAnswer(details.getCorrectAnswer());
                question.setScore(details.getDefaultScore());
                question.setQuestion(details.getQcontent());
                questionList.add(question);
            } else {//这个需要将填空题的\这个符号 以及所有的空格 去掉才可以进行精准匹配
                String correctAnswer = details.getCorrectAnswer().replace("\\", "").replace(" ", "");
//                使用字符串相似度算法对比相似度
                Double similarity = TextCompare.getSimilar(trackByQidAndSid.getAnswer(), correctAnswer);
//                判断多选题时
                if (details.getQtype().contains("多选题")){
                    if (similarity*100==100){//全对
                        trackByQidAndSid.setIsCorrect(1);
                        trackByQidAndSid.setScore(details.getDefaultScore());
                        trackMapper.updateById(trackByQidAndSid);
                    }else {
                        Integer isCorrect = MoreSelectQuestionUtil.checkMultipleChoiceAnswer(trackByQidAndSid.getAnswer(), correctAnswer);
                        if (isCorrect==0){//只对了一半
                            trackByQidAndSid.setIsCorrect(1);
                            trackByQidAndSid.setScore(details.getDefaultScore()/3);
                            trackMapper.updateById(trackByQidAndSid);
                        }else if(isCorrect==-1){//全错
                            trackByQidAndSid.setIsCorrect(1);
                            trackByQidAndSid.setScore(0);
                            trackMapper.updateById(trackByQidAndSid);
                        }
                    }
                }else {//判断单选题和填空题时直接根据相似度来判断
                    if (similarity*100>=94){
//                        更新该题目的分数和正确情况
                        trackByQidAndSid.setIsCorrect(1);
                        trackByQidAndSid.setScore(details.getDefaultScore());
                        trackMapper.updateById(trackByQidAndSid);
                    }else {
//                        错误了就需要AI分析错误原因
                        trackByQidAndSid.setIsCorrect(0);
                        trackByQidAndSid.setScore(0);
//                        Ai分析错误原因并推荐相关资源
                        trackByQidAndSid.setMistakeCase(analysisMistake(details,trackByQidAndSid));
                        trackMapper.updateById(trackByQidAndSid);
                    }
                }

            }
        }
        return trackByQidAndSid;//进行批改后的记录，返回，免得后面又要从数据库拿
    }

    private TpaHomeworkTrack getTrackByQidAndSid(Integer sid,Integer qid){
        QueryWrapper<TpaHomeworkTrack> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("qid",qid)
                .eq("sid",sid);
        return trackMapper.selectOne(queryWrapper);
    }

//    AI分析题目的错误原因
    private String analysisMistake(TpaHomeworkDetails details,TpaHomeworkTrack track) throws Exception {
//        组装请求
        StringBuilder request=new StringBuilder();
        request.append("以下是一道题和一个学生错误的回答情况:\n")
                .append("题目知识点：").append(details.getQtitle()).append("\n")
                .append("题干：").append(details.getQcontent()).append("\n")
                .append("标准答案：").append(details.getCorrectAnswer()).append("\n")
                .append("题目选项（如果是选择题）:").append(details.getSelections()).append("\n")
                .append("该学生的回答:").append(track.getAnswer()).append("\n")
                .append("解题用时：").append(track.getTimeSpent()).append("\n")
                .append("请根据题目的内容、标准答案、知识点以及该学生的错误答案来分析可能的错误原因，并根据该学生在这道题上体现的薄弱点进行分析,\n输出要求：1、整个分析过程以及最后的结论不要太长，最后推荐2个相关的易错题目\n 2、本次错题分析生成的文字要短不要太长");
           return chatWithModel.chatClient(request.toString(),track.getSid().toString(),analysisBotId);
    }

//    AI处理简答题的批改
    private HashMap<Integer, Integer> markingShortByAi(List<ShortQuestion> questionList) throws Exception {
        StringBuilder request=new StringBuilder();
        request.append("以下是多个简答题以及一个学生的答题情况:\n");
        for (ShortQuestion question:questionList){
             request.append("题目:").append(question.getQuestion()).append("\n")
                    .append("题目标准答案：").append(question.getCorrectAnswer()).append("\n")
                    .append("题目分数：").append(question.getScore()).append("\n")
                    .append("学生的回答:").append(question.getStuAnswer()).append("\n\n");
        }
        request.append("请根据每道题的标准答案、题目分数以及学生的回答对该学生的每道题进行评分，输出格式要求：1、输出结果按照题目顺序仅生成每一道题的得分，除了分数不要生成其他任何文字\n2、分数之间使用 | 这个符号隔开\n3、分数只要一个数字，其前后不要加空格或汉字\n4、注意：有几个题目就生成几个分数，不要多生成也不要少生成！！");
        String scores = chatWithModel.chatClient(request.toString(), "1", analysisBotId);
        String[] scoreArr = scores.split("\\|");
        HashMap<Integer,Integer> scoreMap=new HashMap<>();
        for (int i=0;i<scoreArr.length;i++){
            scoreMap.put(questionList.get(i).getHtId(),Integer.parseInt(scoreArr[i]));
        }
        return scoreMap;
    }
}

////        1、智能分析：向指定的智能体发送请求分析该学生的数据
//        String request1="请根据知识库中的一个文件："+track.getStuName()+"学生完成"+task.getPreviewName()+"预习任务情况数据,这个文件记录了该学生的本次预习任务完成情况的数据，请根据文件中的数据对该学生的预习情况进行学情分析,并生成几条老师辅导该学生的建议，建议可以包含多方面要具体详细";
//        String analysis = chatWithModel.chatClient(request1, track.getSid().toString(), analysisBotId);
////        2、个性化题目根据预习情况推荐个性化题目
//        String request2="请根据知识库中的一个文件:"+track.getStuName()+"学生完成"+task.getPreviewName()+"预习任务情况数据,这个文件记录了该学生的本次预习任务完成情况的数据" +
//                "输出要求：\n1、请根据文件中的数据只生成针对性的可以提升该学生薄弱点的个性化针对性题目或者补充进阶题目\n2、生成10个题目，题目包含单选题、多选题、填空题 \n" +
//                "3、题目总分为100分 \n4、题目的难度需求为简单题+中等题+进阶" + "\n5、每道题的分值根据前面指定的总分和题目数量，并根据题目难度和题型来进行分配，不要均分 \n" +
//                "6、生成的每道题的分值我只要一个单独的数字，不要给分值前后加空格或者汉字\n7、如果题目是选择题，你生成的答案就必须是A、B、C、D这样的选项\n" +
//                "8、选择题、填空题和简答题都一定要生成解析！\n9、注意：一定不要生成相同的题目！！即每道题的题干、答案、解析都不能相同";
//        String specialQuestion = chatWithModel.chatClient(request2, track.getSid().toString(), analysisBotId);
//        List<TpaHomeworkDetails> details=new ArrayList<>();
//        String[] question = specialQuestion.split("\\|\\|");
//        List<String> questions1 = new ArrayList<>(List.of(question));
//        questions1.remove(""); //将空串移除
//        TpaHomeworkServiceImpl.StringToBean(details, questions1);
//        for (TpaHomeworkDetails details1:details){
//            if(details1.getSelections()!=null) {
//                details1.setOptions(OptionUtil.optionDeal(details1.getSelections()));
//            }
//        }
////        3、个性化资料：获取额外个性化资料
//        String  request3="请根据知识库中的一个文件:"+track.getStuName()+"学生完成"+task.getPreviewName()+"预习任务情况数据,这个文件记录了该学生的本次预习任务完成情况的数据，请根据文件中的记录数据进行学情分析，分析出该学生的知识薄弱点，并生成相关的资料进行补充，资料要求：1、资料要尽量丰富能够涵盖学生的知识盲区和掌握的不好的知识点\n " +
//                "2、补充资料应包括：补充知识点，易错案例，拓展资源、推荐读物、多媒体资源等\n " +
//                "3、生成的网络资源链接必须是有效的，可以从百度、bilibili等网站获取补充资源,补充资料可以不必太长，只要可以全面的反映学生的薄弱点并进行相关资源的补充即可\n4、生成的视频或网页资源请以链接的形式生成给我";
//        String addInformation = chatWithModel.chatClient(request3, track.getSid().toString(), informationBotId);
//        track.setAiAnalysis(analysis);
//        track.setSupplement(addInformation);
//        track.setAddQuestion(details.size());
//        previewTrackMapper.updateById(track);//根据id进行更新，由于是从数据库中整条数据都查出来的，所以这个的id是有值的
////        将个性化题目更新到题目表中
//        for (TpaHomeworkDetails details1:details){
////            设置每一个题目的所属，包括学生id，预习任务id
//            details1.setSid(uid);
//            details1.setQfrom(3);
//            details1.setCreateTime(CurrentTime.getTime());
//            details1.setSubject("个性化补充题");
//            details1.setPtId(ptId);
//            if (details1.getOptions()!=null){
//                details1.setSelectOption(new Gson().toJson(details1.getOptions()));
//            }
//            detailsMapper.insert(details1);//将这个题目更新到数据库中
//        }