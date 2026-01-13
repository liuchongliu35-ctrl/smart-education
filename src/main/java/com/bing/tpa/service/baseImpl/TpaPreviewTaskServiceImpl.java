package com.bing.tpa.service.baseImpl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bing.tpa.domain.VO.PreviewTaskReleaseVo;
import com.bing.tpa.domain.VO.PreviewTaskVo;
import com.bing.tpa.domain.VO.TaskResourceVo;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.domain.entity.TpaPreviewTask;
import com.bing.tpa.domain.entity.TpaPreviewTrack;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.TpaClassMapper;
import com.bing.tpa.mapper.TpaPreviewTaskMapper;
import com.bing.tpa.mapper.TpaPreviewTrackMapper;
import com.bing.tpa.modelcall.homeworkCall.TaskGenerate;
import com.bing.tpa.service.baseService.TpaPreviewTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.utils.CurrentTime;
import com.bing.tpa.utils.OptionUtil;
import com.bing.tpa.utils.word.ImageHandler;
import com.bing.tpa.utils.word.LinkHandler;
import com.bing.tpa.utils.word.RichTextParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaPreviewTaskServiceImpl extends ServiceImpl<TpaPreviewTaskMapper, TpaPreviewTask> implements TpaPreviewTaskService {
    private static final String botId="7473479507333464115";

    private static final String botId2="7475340560941809690";

    @Autowired
    private TaskGenerate taskGenerate;

    @Autowired
    private TpaPreviewTaskMapper taskMapper;

    @Autowired
    private TpaHomeworkDetailsServiceImpl detailsService;

    @Autowired
    private TpaPreviewTrackMapper trackMapper;

    @Autowired
    private TpaClassMapper classMapper;

    ScheduledExecutorService taskGenerateExecutor= Executors.newScheduledThreadPool(5);
    Logger logger = LoggerFactory.getLogger(TpaPreviewTaskServiceImpl.class);
//author_id  preview_name p_title secondary_title questions_num questions_grade problem_type
//  "authorId": 0,
//  "previewName": 0,
//  "problemType": "",
//  "ptitle": "",
//  "questionsGrade": 0,
//  "questionsNum": 0,
//  "secondaryTitle": ""

    @Override
    @Transactional
    public PreviewTaskVo generateTaskResources(TpaPreviewTask previewTask, Integer tid) {
//        创建一条预习任务数据
        previewTask.setAuthorId(tid);//预习任务一定要设置老师的id，为后续的各种操作提供方便
        boolean save = save(previewTask);
        PreviewTaskVo previewTaskVo = new PreviewTaskVo();
        if(save){
        AtomicReference<TaskResourceVo> readResourcesRef = new AtomicReference<>(new TaskResourceVo());
        AtomicReference<List<TpaHomeworkDetails>> questionResourcesRef = new AtomicReference<>(new ArrayList<>());
        ScheduledFuture<?> resourceFuture = null;
        ScheduledFuture<?> questionFuture = null;
//        todo 创建两个线程，一个线程获取预习资料，一个线程获取题目
        try{
//        todo 获取预习资料的线程
            resourceFuture=taskGenerateExecutor.schedule(()->{
                try {
                    logger.info("开始获取预习资料");
                    TaskResourceVo readResources = gainReadingResources(previewTask);
                    readResourcesRef.set(readResources);
                    logger.info("预习资料生成完成");
                } catch (Exception e) {
                    throw new RuntimeException("预习资料生成失败，请重试");
                }
            },0, TimeUnit.SECONDS);

//        todo 获取题目的线程
            questionFuture=taskGenerateExecutor.schedule(()->{
                try {
                    logger.info("开始生产预习题目");
                    previewTaskVo.setPtId(previewTask.getPtId());
                    List<TpaHomeworkDetails> taskQuestions = gainQuestion(previewTask);
                    AtomicInteger num= new AtomicInteger(1);
                    taskQuestions.forEach(tq->{
                        tq.setQid(num.get());//暂时给qid进行赋值，为了前端好进行区分渲染。后续题目保存到数据库中时，需要将qid置为null；
                        num.getAndIncrement();
                    });
                    questionResourcesRef.set(taskQuestions);
                    logger.info("预习题目生成完成");
                } catch (Exception e) {
                    throw new RuntimeException("预习题生成失败，请重试");
                }
            },1, TimeUnit.SECONDS);

            resourceFuture.get();
            questionFuture.get();
        } catch (InterruptedException |NullPointerException| ExecutionException e  ) {
            String message = e.getMessage();
            removeById(previewTask.getPtId());//如果数据获取失败就不要创建新的预习任务了
            System.out.println(message);
//            throw new RuntimeException(message);
        } finally {
            previewTaskVo.setTaskList(questionResourcesRef.get());
            previewTaskVo.setPreviewText(readResourcesRef.get());
            previewTaskVo.setPreviewName(previewTask.getPreviewName());
            previewTaskVo.setTotalScore(previewTask.getQuestionsGrade());
            previewTaskVo.setPtitle(previewTask.getPtitle());
            previewTaskVo.setSecondaryTitle(previewTask.getSecondaryTitle());
        }
//            previewTaskVo.setPtId(previewTask.getPtId());
////            调用coze接口获取题目和资料，获取资料和获取题目的分开
////            todo 再获取题目
//            List<TpaHomeworkDetails> taskQuestions = gainQuestion(previewTask);
//            AtomicInteger num= new AtomicInteger(1);
//            taskQuestions.forEach(tq->{
//                tq.setQid(num.get());//暂时给qid进行赋值，为了前端好进行区分渲染。后续题目保存到数据库中时，需要将qid置为null；
//                num.getAndIncrement();
//            });
        }else return null;
        return previewTaskVo;
    }

    /**
     * 预习题我们不搞和作业一样的引用机制，我们的预习题只能AI生成和AI再次添加以及自己添加题目，
     * 不设置从题库获取题目，这样到这里的预习题一定是数据库中没有的，所以不需要检查是来自题库，直接设置完标志性属性就可以添加到数据库中了
     * 预习题不可以从题库中引用题目，但可以将其加入到题库中，为作业引用题目提供方便
     */
    @Override
    public Integer saveTaskResource(PreviewTaskVo previewTaskVo) {
//        1、先将预习题的一些标志属于预习任务添加到题目表中
        List<TpaHomeworkDetails> questionList = previewTaskVo.getTaskList();
//        根据预习任务id找到题目所属的课程
        String subject = taskMapper.selectSubjectByPtId(previewTaskVo.getPtId());
//        重新记录预习题的数量
        AtomicReference<Integer> totalScore= new AtomicReference<>(0);
        questionList.forEach(item->{
            if (item.getDefaultScore()!=null)
             totalScore.updateAndGet(v -> v + item.getDefaultScore());
//            这个ptid标志这个题目是属于预习任务的，是预习题,不设置作业Id就默认是null，后续查找作业的题目就不会查到这个，但是作业可以搜索题库时可以根据ptId搜索到数据
//            将qid置为null，为了将新题目插入到数据库中!!!!!
            item.setQid(null);
            item.setPtId(previewTaskVo.getPtId());
            item.setSubject(subject);
//            2表示这个题目是属于预习任务的
            item.setQfrom(2);
            item.setCreateTime(CurrentTime.getTime());
//            设置预习题答案的json串
            if (item.getOptions()!=null){
                item.setSelectOption(new Gson().toJson(item.getOptions()));
            }
        });
//        2、将预习资料和预习资料的题更新到预习任务中
//        更新预习题的分数，设置预习任务的其他数据
        TpaPreviewTask previewTask = new TpaPreviewTask();
        previewTask.setPtId(previewTaskVo.getPtId());
        previewTask.setQuestionsNum(questionList.size());
        previewTask.setQuestionsGrade(totalScore.get());
        /*设置预习任务的预习资料以及预习资料的题目数据*/
        previewTask.setPreviewContent(previewTaskVo.getPreviewText().getReadResource());
//        将题目按照一个json串储存在数据库中，方便后续直接拿出json串就可以解析出List出来
        previewTask.setProblem(new Gson().toJson(previewTaskVo.getPreviewText().getQuestion()));
        int update = taskMapper.updateById(previewTask);
        boolean b = detailsService.saveBatch(questionList);
        if(!b) return 0;
        return update;
    }

//    一个班级一个作业，一个班级的作业是独一无二的，一个作业不可以发到多个班级，所以这里直接根据班级id找到该班级独一的作业
    @Override
    public List<TpaPreviewTask> getTaskList(Integer cid) {
        List<TpaPreviewTask> taskList = lambdaQuery()
                .eq(TpaPreviewTask::getCid, cid)
                .list();
        Integer personNum = classMapper.getClassPersonNum(cid, null);
        taskList.forEach(t->{
            t.setUnComplete(personNum-t.getComplete());
        });
        return taskList;
    }

//    走这个接口说明用户开始预习了，需要将一条记录预习情况的数据到preview_track表中
    @Override
    public PreviewTaskVo taskByPtId(Integer ptId, Integer id) throws FormatException {
//        先根据id查询预习任务的基本内容
        TpaPreviewTask previewTask = getById(ptId);
//      将这个转化到PreviewTaskVo对象中，因为PreviewTaskVo继承了TpaPreviewTask，但是除了预习资料和预习资料题目不转
        PreviewTaskVo previewTaskVo = new PreviewTaskVo();
        TaskResourceVo taskResourceVo = new TaskResourceVo();
//        1、将预习任务的资料赋给PreviewTaskVo
        taskResourceVo.setReadResource(previewTask.getPreviewContent().replace("\\",""));
//        2、将预习资料的附加题赋给PreviewTaskVo
        taskResourceVo.setQuestion(new Gson().fromJson(previewTask.getProblem().replace("\\",""),new TypeToken<List<String>>() {}.getType()));//这里将json形式的字符串转为list集合
        previewTaskVo.setPreviewText(taskResourceVo);
//        查询该预习任务的预习题列表，因为获取预习题是不可以从题库中拉取题目，所以这个不需要检查other_hid这个记录该题目被其他作业引用的字段
        List<TpaHomeworkDetails> questionList = detailsService.lambdaQuery()
                .eq(TpaHomeworkDetails::getPtId, ptId)
                .and(wrapper -> wrapper.isNull(TpaHomeworkDetails::getSid))
//                .eq(TpaHomeworkDetails::getSid,null)//预习题不包括学生的个性化题目，个性化题目也是属于预习任务的，所以有预习任务的id，即ptId
                .list();
//        将选项转为数组
        for (TpaHomeworkDetails details:questionList){
//            将题目的答案和解析置空
        if (details.getSelectOption()!=null){
            dealFormation(details);
        }
        }
//        3、将预习任务的题目赋给PreviewTaskVo
        previewTaskVo.setTaskList(questionList);
//        将其他预习任务的信息给previewTaskVo，除了预习资料和预习资料的题目
        BeanUtil.copyProperties(previewTask,previewTaskVo,"previewContent","problem");

//        添加一条记录预习完成情况的数据到preview_track表中
        //       3. 新建一条预习任务的记录数据插入到preview_track表中,当id为null是表示用户知识查看，不开始做
        if (id!=null&&id!=0){
            QueryWrapper<TpaPreviewTrack> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("pt_id",ptId)
                    .eq("sid",id);
            boolean exists = trackMapper.exists(queryWrapper);//先判断是否有该数据，在进行组装对象的操作。不然就会每次都要走组装的步骤但不一定会继续插入
            if (!exists){
                TpaPreviewTrack track = new TpaPreviewTrack();
                track.setPtId(ptId);
                track.setSid(id);//设置用户的id，表示这个预习任务的完成情况记录数据属于该用户
                track.setTextFinish(0);
                track.setQuestionFinish(0);
                track.setStartTime(CurrentTime.getTime());//这里和作业题目的开始时间算法不一样，这个直接在点击开始预习任务就设置，那个是将点击开始答题的时间当做作业开始时间和第一道题的答题时间
                track.setQuestionNum(questionList.size());
                trackMapper.insert(track);//记录预习过程
            }
        }
        previewTaskVo.setPtitle(previewTask.getPtitle());
        previewTaskVo.setSecondaryTitle(previewTask.getSecondaryTitle());
        previewTaskVo.setTotalScore(previewTask.getQuestionsGrade());
        return previewTaskVo;
    }

    static void dealFormation(TpaHomeworkDetails details) {
        details.setQcontent(details.getQcontent().replace("\\",""));
        if (details.getSelectOption()!=null){
            List<String> list = new Gson().fromJson(details.getSelectOption(), new TypeToken<List<String>>() {}.getType());
            List<String> newOption=new ArrayList<>();
            list.forEach(l-> newOption.add(l.replace("\\", "")));
            details.setOptions(newOption);
            details.setSelections(details.getSelections().replace("\\",""));
        }
    }

    @Override
    @Transactional
    public boolean releaseTask(PreviewTaskReleaseVo taskReleaseVo) {
//        将截止时间转为LocalDataTime
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime deadline = LocalDateTime.parse(taskReleaseVo.getDeadline(), df);
//        查找该班级需要和哪一个预习任务关联
        assert taskReleaseVo.getPtId()!=null;
        TpaPreviewTask task = new TpaPreviewTask();
        task.setPtId(taskReleaseVo.getPtId());
        task.setCid(taskReleaseVo.getCid());
        /*设置创建作业创建时间*/
        task.setDeadline(deadline);
        task.setActive(1);
        boolean update = updateById(task);
//        更新预习任务的预习题的审核时间
        boolean update1 = detailsService.lambdaUpdate()
                .eq(TpaHomeworkDetails::getPtId, taskReleaseVo.getPtId())
                .set(TpaHomeworkDetails::getReviewTime, CurrentTime.getTime())
                .update();
        return update&&update1;
    }
    //根据用户的需求新添加指定数量的预习题
    @Override
    public PreviewTaskVo addQuestions(Integer ptId, Integer num) {
        TpaPreviewTask task = getById(ptId);
        PreviewTaskVo previewTaskVo = new PreviewTaskVo();
        previewTaskVo.setPtId(ptId);
        task.setQuestionsNum(num);
        task.setQuestionsGrade(0);
        List<TpaHomeworkDetails> details = gainQuestion(task);
        previewTaskVo.setTaskList(details);
        return previewTaskVo;
    }

//    TODO 根据用户id和知识点，查找属于该知识点的预习任务列表
    @Override
    public List<TpaPreviewTask> taskByTidAndTitle(String title, Integer tid) {
        return  taskMapper.getTaskByTitleAndTid(title,tid);
    }
//todo 解析预习资料为word格式
    @Override
    public XWPFDocument generatePreviewWord(Integer ptId) throws IOException, InvalidFormatException {
        // 1. 查询数据库：获取富文本内容
        TpaPreviewTask task = taskMapper.selectById(ptId);
        if (task == null || StringUtils.isEmpty(task.getPreviewContent())) {
            throw new RuntimeException("未找到对应预习任务或内容为空");
        }
        String richText = task.getPreviewContent();

        // 2. 创建Word文档
        XWPFDocument doc = new XWPFDocument();

        // 3. 解析富文本（处理标题、段落、列表）
        List<RichTextParser.ContentBlock> contentBlocks = RichTextParser.parse(richText);
        for (RichTextParser.ContentBlock block : contentBlocks) {
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();

            // 按类型设置样式（标题加粗、列表缩进）
            if (block.getType() == RichTextParser.ContentType.TITLE) {
                run.setText(block.getContent());
                run.setBold(true); // 标题加粗
                run.setFontSize(14); // 标题字号
            } else if (block.getType() == RichTextParser.ContentType.LIST_ITEM) {
                // 列表项：左缩进1000emu
                paragraph.setIndentationLeft(1000);
                run.setText("• " + block.getContent()); // 列表前缀（•）
            } else { // 普通段落
                // 先处理段落中的链接（替换为超链接）
                Map<String, String> links = LinkHandler.extractLinks(block.getContent());
                if (links.isEmpty()) {
                    run.setText(block.getContent()); // 无链接，直接写文本
                } else {
                    // 有链接：拆分文本，插入超链接
                    String text = block.getContent();
                    for (Map.Entry<String, String> link : links.entrySet()) {
                        String linkText = link.getKey();
                        String linkUrl = link.getValue();
                        // 插入链接前的文本
                        int index = text.indexOf("[" + linkText + "]");
                        if (index > 0) {
                            run.setText(text.substring(0, index));
                        }
                        // 插入超链接
//                        LinkHandler.insertLinkToParagraph(paragraph, linkText, linkUrl);
                        // 更新剩余文本
                        text = text.substring(index + ("[" + linkText + "](" + linkUrl + ")").length());
                    }
                    // 插入链接后的文本
                    if (!text.isEmpty()) {
                        run.setText(text);
                    }
                }
            }
        }

        // 4. 插入图片（从富文本中提取图片并插入）
        List<String> imageUrls = ImageHandler.extractImageUrls(richText);
        for (String imageUrl : imageUrls) {
            ImageHandler.insertImageToWord(doc, imageUrl);
        }

        return doc;
    }

    private  TaskResourceVo gainReadingResources(TpaPreviewTask previewTask) {
        StringBuilder request = new StringBuilder();
        request.append("请帮我生成一份人工智能通识课的预习资料，预习资料涉及的知识点为：“")
                .append(previewTask.getPtitle())
                .append("--").append(previewTask.getSecondaryTitle()).
                append("“，预习资料应包括：问题引导、知识框架、重点内容、基础概念、案例分析、拓展资源、推荐读物、思维导图展示与多媒体资源等能够让学生对本次课堂要讲的知识点有一个初步的了解和掌握").
                append("\n同时在资料的最后面在加3到4个和预习资料内容相关的简答题或选择题，题目不需要生成答案和解析。" +
                        "注意，预习资料和题目之间使用||号来隔开，题目与题目之间使用 | 这符号隔开，且推荐的文章或视频资源以及思维导图使用链接的形式输出，一定要输出题目");
        TaskResourceVo taskResourceVo = null;
        try {
            String resource = taskGenerate.chatClient(request.toString(), previewTask.getPtId().toString(), botId2);
            taskResourceVo = new TaskResourceVo();
//            String[] resourceAndQuestion = resource.split("\\|\\|");
//            if (resourceAndQuestion.length != 2) throw new FormatException("预习阅读资源格式错误，请重新请求");
//            else {
                taskResourceVo.setReadResource(resource);//这里一次只生成资料
//            在生成两道相关题目
            StringBuilder re=new StringBuilder();
            re.append("有如下预习资料：\n")
                    .append(resource).append("\n\n")
                    .append("请根据以上预习资料生成2-3到和资料相关的测试题目，题型任意，每题5分\n输出格式要求：1、题目与题目之间使用 | 这个符号隔开\n2、不要生成题目的答案和解析");
            String question = taskGenerate.chatClient(re.toString(), previewTask.getPtId().toString(), botId2);
                String[] questions = question.split("\\|");
//                if (questions.length <= 1) throw new FormatException("资源习题分解错误，请重新请求");
                List<String> questionList = new ArrayList<>(Arrays.asList(questions));
                taskResourceVo.setQuestion(questionList);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskResourceVo;
    }

    private List<TpaHomeworkDetails> gainQuestion(TpaPreviewTask previewTask) {
        StringBuilder request=new StringBuilder();
        List<TpaHomeworkDetails> details=new ArrayList<>();
        request.append("请帮我生成一份人工智能通识课的预习题，预习题设计知识点为：").append(previewTask.getPtitle()).
                append("--").append(previewTask.getSecondaryTitle()).
                append(",题目数量为：").append(previewTask.getQuestionsNum()).
                append("个，题目类型为：").append(previewTask.getProblemType()).
                append("\n题目的总分为：");
                if(previewTask.getQuestionsGrade()==0) request.append("根据生成的题目数以及各个题目的分值来确定");
                else request.append(previewTask.getQuestionsGrade());

                request.append("\n题目的难度需求为：基础简单题占70%，中等题占30%").
                append("\n生成要求：1、每道题的分值根据前面指定的总分和题目数量，并根据题目难度和题型来进行分配，不要均分\n2、我要的题目数量是：")
                .append(previewTask.getQuestionsNum()).append("个,请不要多生成，也不要少生成\n3、生成的每道题的分值我只要一个单独的数字，不要给分值前后加空格或者汉字" +
                                "\n4、如果题目是选择题，你生成的答案就必须是A、B、C、D这样的选项" +
                                "\n5、选择题、填空题和简答题都一定要生成解析！\n" +
                                "6、选择题的题目类型请分为单选题和多选题，不要使用“选择题”这个来作为题目的类型" +
                                "\n7、注意：题目的知识点不要重复，可以生成拓展知识点的题目");

        try {
            String questions = taskGenerate.chatClient(request.toString(), previewTask.getPtId().toString(), botId);
            //            将题目转为对象，对字符串进行分割
            String[] questionsList =questions.split("\\|\\|");
            List<String> questions1 = new ArrayList<>(List.of(questionsList));
            questions1.remove(""); //将空串移除
            if(questionsList.length<previewTask.getQuestionsNum()) throw new FormatException("题目数量错误，生成失败请重新尝试");
            TpaHomeworkServiceImpl.StringToBean(details, questions1);
//            将题目选项转为数组!!!!!!!!!!!
            TpaHomeworkServiceImpl.formOptions(details);//处理选项
        } catch (Exception e) {
            e.printStackTrace();
        }
        return details;
    }
}
