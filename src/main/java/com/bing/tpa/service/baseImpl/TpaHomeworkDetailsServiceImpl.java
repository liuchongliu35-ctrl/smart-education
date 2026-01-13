package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.entity.TpaHomework;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.mapper.TpaHomeworkDetailsMapper;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.mapper.TpaHomeworkMapper;
import com.bing.tpa.mapper.TpaPreviewTaskMapper;
import com.bing.tpa.mapper.TpaTeacherMapper;
import com.bing.tpa.service.baseService.TpaHomeworkDetailsService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.CurrentTime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaHomeworkDetailsServiceImpl extends ServiceImpl<TpaHomeworkDetailsMapper, TpaHomeworkDetails> implements TpaHomeworkDetailsService {

    @Autowired
    private TpaHomeworkMapper homeworkMapper;

    @Autowired
    private TpaTeacherMapper teacherMapper;

    @Autowired
    private TpaTeacherService  teacherService;

    @Autowired
    private TpaHomeworkDetailsMapper detailsMapper;

    @Autowired
    private TpaPreviewTaskMapper taskMapper;
//      q_from
//      create_time
//      hid
    @Override
    public boolean addNewQuestion(List<TpaHomeworkDetails> homeworkDetails, Integer hid) {
//        每一个Ai生成的新题目还需要添加一个课程信息的字段
//        根据作业id连表查询该作业属于的课程
        String subject = homeworkMapper.selectSubjectByHid(hid);
//        还需要重新设置一下作业的总分
//        同时也要将新的题目数量替换到作业中记录题目数量的字段！！！
        AtomicReference<Integer> totalScore= new AtomicReference<>(0);
//        处理可能引用其他作业题目的情况，这时这个题目就是有qid和hid的，和AI生成的不一样，AI现场生成的是没有qid也没有和本次作业相连的
        List<TpaHomeworkDetails> repeatQuestions=new ArrayList<>();
        homeworkDetails.forEach(item->{
            if (item.getDefaultScore()!=null)
                totalScore.updateAndGet(v -> v + item.getDefaultScore());
            if(item.getHid()==null&&item.getQid()==null) { //这两个必须都等于null才表示该题目不是从题库搜索出来的，是本次创建作业从AI生成的新题目
//              表示这个题是在创建这个作业时现场创建的，还没有qid和hid，这里将本题和这次作业相连
                item.setQfrom(1);//这个只对AI生成的新题目进行设置，已经存在的题目就不需要设置了
                item.setCreateTime(CurrentTime.getTime());//这个只对AI生成的新题目进行设置，已经存在的题目就不需要设置了
                item.setHid(hid);//设置每一个题目所属作业的id，这样就可以根据作业id查找到属于他的题目了
//                设置新题属于的课程,后续根据这个字段进行相关题目自动匹配！！！！！！
                item.setSubject(subject);
            }else { //不等于null表示这个题目是从其他作业弄过来的，就不能再将这道题添加进去了,也表示这道题的Qid也是存在的
                repeatQuestions.add(item);
//                将这个从需要新添加的题目集合中去掉，后续在另外一个字段记录这个已经被其他作业使用的题目被本次作业拉取的情况
                homeworkDetails.remove(item);
            }
//           如果options不为空就需要将这个转为json串，这样后面就不用总是对Selections进行处理了
//            后续只用将option转为数组就可以了
            if (item.getOptions()!=null){
//                这里可以直接将options直接写入数据库，因为在生成的时候就已经将空串去掉后再返回给用户审核了，所以这里的字符串集合没有串
                item.setSelectOption(new Gson().toJson(item.getOptions()));
            }
        });
//        更新一下作业的总分
        TpaHomework homework = new TpaHomework();
//        重新设置作业题目的数量
        homework.setQuantity(homeworkDetails.size());
        homework.setHid(hid);
        homework.setScore(totalScore.get());
        homeworkMapper.updateById(homework);

/*修改已存在的题目被该题目应用的情况，同时增加记录被引用次数的字段,由于这个是从题库拿到的，所以一些字段是有值的，比如：题目Qid、被应用的次数UsageCount，属于的作业的Hid*/
        boolean flag=true;
        if (!repeatQuestions.isEmpty()){
            for (TpaHomeworkDetails details:repeatQuestions){
                assert details.getQid()!=null;
                assert details.getHid()!=null;
                LambdaUpdateChainWrapper<TpaHomeworkDetails> set = lambdaUpdate()
                        .eq(TpaHomeworkDetails::getQid, details.getQid())
                        .set(TpaHomeworkDetails::getUsageCount, details.getUsageCount() + 1);
                if(details.getOtherHid()==null){
//                    ,1,2,3,这样所有的id都是在两个逗号之间的，就可以实现不同id的间隔，方便后续的模糊查询不会导致一个id包含在另一个id中
//                    ,1,这样才是一个完整的id，可以和,15,这个分开，如果不这样查询 1 的时候 15也包含1就业会被认为包含1
//                    不使用逗号分割的方法，使用list转json的方法
                    List<String> otherHidList=new ArrayList<>();
                    otherHidList.add(hid.toString());
                    set.set(TpaHomeworkDetails::getOtherHid,new Gson().toJson(otherHidList));
//                    set.set(TpaHomeworkDetails::getOtherHid,","+hid+",");//这是第一个id
                }else if(!details.getOtherHid().contains("\""+hid.toString()+"\"")){
                    List<String> otherHids = new Gson().fromJson(details.getOtherHid(), new TypeToken<List<String>>() {
                    }.getType());
                    otherHids.add(hid.toString());//将新的hid加入到otherHid字段中
                    set.set(TpaHomeworkDetails::getOtherHid,otherHids);
                }
//                执行这个题目的修改
                boolean update = set.update();
                if(!update) flag=false;
            }
        }
        return saveBatch(homeworkDetails)&&flag;
    }

    @Override
    public List<TpaHomeworkDetails> automaticMatchByTid(Integer tid) {
//        先查找教师教授的课程
        TpaTeacher teacher = teacherService.getCurrentUser();
//        根据老师教授的课程来进行匹配,查找作业题目
        List<TpaHomeworkDetails> questionsFromHomework = detailsMapper.selectQuestionBySubject(teacher.getTeachLesson(),teacher.getTeachStage());
//        查找预习题
        List<TpaHomeworkDetails> questionsFromTask = detailsMapper.getQuestionBySubject(teacher.getTeachLesson(),teacher.getTeachStage());
        questionsFromHomework.addAll(questionsFromTask);
        return questionsFromHomework;
    }

//    将题目都收集起来写入word中
    @Override
    public XWPFDocument exportToWord(Integer hid, Integer ptId) {
        List<TpaHomeworkDetails> questions = detailsMapper.selectQuestions(hid, ptId);
        if (questions.isEmpty()) {
            throw new RuntimeException("未找到相关习题");
        }

        XWPFDocument doc = new XWPFDocument();
        addTitle(doc, questions.get(0));
        addQuestions(doc, questions);
        return doc;
    }


    private void addTitle(XWPFDocument doc, TpaHomeworkDetails firstQuestion) {
        XWPFParagraph titlePara = doc.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText(cleanContent("习题集"));
        titleRun.setFontSize(18);
        titleRun.setBold(true);
        titleRun.addBreak();

        XWPFRun infoRun = titlePara.createRun();
        String subject = firstQuestion.getSubject();
        infoRun.setText(cleanContent("科目: " + (subject != null ? subject : "")));
        infoRun.setFontSize(12);
        infoRun.addBreak();
        setChineseFont(titleRun);  // 添加字体设置
        setChineseFont(infoRun);   // 添加字体设置
    }

    private void addQuestions(XWPFDocument doc, List<TpaHomeworkDetails> questions) {
        int questionNum = 1;
        for (TpaHomeworkDetails question : questions) {
            addQuestion(doc, question, questionNum++);
            addAnswerAnalysis(doc, question);
            addSpaceAfterQuestion(doc);
        }
    }

    private void addQuestion(XWPFDocument doc, TpaHomeworkDetails question, int num) {
        // 题号
        XWPFParagraph numPara = doc.createParagraph();
        XWPFRun numRun = numPara.createRun();
        numRun.setText(cleanContent(num + ". "));
        numRun.setBold(true);

        // 题干
        XWPFRun contentRun = numPara.createRun();
        contentRun.setText(cleanContent(question.getQcontent()));

        // 添加选择题选项
        if (Arrays.asList("单选题", "多选题", "选择题").contains(question.getQtype())) {
            addOptions(doc, question);
        }
        setChineseFont(numRun);      // 添加字体设置
        setChineseFont(contentRun);  // 添加字体设置
    }

    private void addOptions(XWPFDocument doc, TpaHomeworkDetails question) {
        String options = question.getSelections();
        if (options != null && !options.isEmpty()) {
            // 按选项分割
            String[] optionArray = options.split("\\s*\\n\\s*");

            for (String option : optionArray) {
                if (!option.trim().isEmpty()) {
                    XWPFParagraph optionPara = doc.createParagraph();
                    optionPara.setIndentationLeft(400); // 缩进

                    XWPFRun optionRun = optionPara.createRun();
                    optionRun.setText(cleanContent(option.trim()));
                    setChineseFont(optionRun);  // 添加字体设置
                }
            }
        }

    }

    private void addAnswerAnalysis(XWPFDocument doc, TpaHomeworkDetails question) {
        XWPFParagraph analysisPara = doc.createParagraph();
        analysisPara.setSpacingAfter(200); // 段后间距

        XWPFRun analysisRun = analysisPara.createRun();
        analysisRun.setText(cleanContent("【答案】" + question.getCorrectAnswer()));
        analysisRun.addBreak();

        analysisRun.setText(cleanContent("【解析】" + question.getAnswerAnalysis()));
        analysisRun.setColor("808080"); // 灰色
        analysisRun.setFontSize(10);
        setChineseFont(analysisRun);  // 添加字体设置
    }

    private void addSpaceAfterQuestion(XWPFDocument doc) {
        XWPFParagraph spacePara = doc.createParagraph();
        spacePara.setSpacingAfter(400); // 增加间距
    }

    // 创建字体提供程序（解决中文乱码核心）
    private void setChineseFont(XWPFRun run) {
        run.setFontFamily("宋体");  // 或 "SimSun", "Microsoft YaHei"
        run.setFontSize(12);
    }

    private String cleanContent(String content) {
        if (content == null) return "";
        String original = content;
        // 替换HTML标签
        content = content.replaceAll("<br\\s*/?>", "\n")
                .replaceAll("<[^>]+>", "");

        System.out.println("原始内容: " + original);
        System.out.println("清理后: " + content);
        // 处理特殊空白字符
        return content.replaceAll("\\u00A0", " ") // 替换不间断空格
                .replaceAll("\\s+", " ")     // 合并多余空格
                .trim();
    }
    //    检查该题目关联的作业是否已公开
    private boolean checkOpen(Integer hid){
        TpaHomework homework = homeworkMapper.selectById(hid);
        return homework.getIsOpen()==1;
    }
}
