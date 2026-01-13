package com.bing.tpa.service.baseImpl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.VO.HomeworkReleaseVo;
import com.bing.tpa.domain.VO.HomeworkTotalSituation;
import com.bing.tpa.domain.VO.HomeworkVo;
import com.bing.tpa.domain.entity.*;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.*;
import com.bing.tpa.modelcall.homeworkCall.TaskGenerate;
import com.bing.tpa.service.baseService.TpaHomeworkService;
import com.bing.tpa.utils.CurrentTime;
import com.bing.tpa.utils.OptionUtil;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
public class TpaHomeworkServiceImpl extends ServiceImpl<TpaHomeworkMapper, TpaHomework> implements TpaHomeworkService {

    @Autowired
    private TaskGenerate taskGenerate;

    @Autowired
    private TpaHomeworkDetailsMapper detailsMapper;

    @Autowired
    private TpaClassMapper classMapper;

    @Autowired
    private TpaHomeworkSummaryMapper summaryMapper;

    @Autowired
    private TpaHomeworkTrackMapper trackMapper;

    @Autowired
    private TpaHomeworkMapper homeworkMapper;

    private static final String botId="7473479507333464115";

    /**
     * 从AI获取题目
     * @param homework
     * @param tid
     * @return
     */
    @Override
    @Transactional
    public HomeworkVo generateQuestions(TpaHomework homework, Integer tid) throws FormatException {
//        先创建一条作业数据
        homework.setAuthorId(tid);//老师的id一定不可以为null
        boolean save = save(homework);
        HomeworkVo homeworkVo = new HomeworkVo();
        if (save){
            homeworkVo.setHid(homework.getHid());
//            调用coze接口获取题目
//            调用发送信息和处理格式的方法
            List<TpaHomeworkDetails> homeworkDetails = null;
            try {
                homeworkDetails = sendAndParse(homework);
            } catch (Exception e) {
               throw new FormatException("题目格式不正确，请重新点击生成");
            }
            homeworkVo.setDetails(homeworkDetails);
        }else return null;
        homeworkVo.setHomeworkName(homework.getHName());
        homeworkVo.setHtitle(homework.getHTitle());
        homeworkVo.setSecondaryTitle(homework.getSecondaryTitle());
        homeworkVo.setTotalScore(homework.getScore());
        return homeworkVo;
    }

    /**
     * 发布作业，需改字段
     * 需要修改的字段： cid、create_time、deadline、state
     * 一个作业只发布到一个班级，这样可以实现不同班级的作业不同，根据班级的具体情况来定制作业，而不只是一个作业发到所有班级，这样就不能根据实际班级情况来指定作业
     */
    @Override
    public boolean releaseHomework(HomeworkReleaseVo releaseVo) {
//        将截止时间转为LocalDataTime
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime deadline = LocalDateTime.parse(releaseVo.getDeadline(),df);
//        根据hid查找作业
        TpaHomework homework = new TpaHomework();
        homework.setHid(releaseVo.getHid());
//        设置作业要绑定的班级，这里只能绑定一个班级，这个作业是该班级独一份的，题目和作业一一对应，作业和班级一一对应，题目和班级也是一一对应的
//        这里绑定了一个班级发布后就不可以改题目了，这样就可以根据班级id查询到唯一的作业，也可以根据作业id查询到唯一的题目，实现不同班级作业个性化
        homework.setCid(releaseVo.getCid());
        //        设置作业未完成人数!!!!初始化为班级全部人数,这个只有在确定了发布班级后才可以确定未完成人数
        homework.setUncomplate( classMapper.getClassPersonNum(releaseVo.getCid(),null));
        /*设置创建作业创建时间*/
        homework.setCreateTime(CurrentTime.getTime());
//        设置截止时间
        homework.setDeadline(deadline);
//        设置作业状态
        homework.setState(1);
        boolean update = updateById(homework);
//        更新题目的审核时间
        Integer updateTime= detailsMapper.updateReviewTimeInteger(releaseVo.getHid(), CurrentTime.getTime());
        return update;
    }

    @Override
    public HomeworkVo addExtraQuestion(Integer hid, Integer num) {
//        根据作业id查找到这些题目即将要关联的作业
        TpaHomework homework = getById(hid);
        HomeworkVo homeworkVo = new HomeworkVo();
        homeworkVo.setHid(hid);
        homework.setScore(0);
//        调用sendAndParse方法发送请求
        homework.setQuantity(num);//这里暂时将用户要求的题目数量赋给homework记录题目数量的字段
        List<TpaHomeworkDetails> homeworkDetails = sendAndParse(homework);
        homeworkVo.setDetails(homeworkDetails);
        return homeworkVo;
    }

    @Override
    public HomeworkTotalSituation totalSituation(Integer hid, Integer cid) {
//        先获取与这个作业相关的所有数据
//        班级人数
        HomeworkTotalSituation totalSituation = new HomeworkTotalSituation();
        Integer personNum = classMapper.getClassPersonNum(cid, null);
        totalSituation.setTotalPerson(personNum);
//      获取作业的基本数据
        TpaHomework homework = getById(hid);
        log.error("aaa"+homework.getScore());
        totalSituation.setHtitle(homework.getHTitle());
        totalSituation.setSecondaryTitle(homework.getSecondaryTitle());
        totalSituation.setHName(homework.getHName());
        totalSituation.setComplete(homework.getComplate());
        totalSituation.setUncomplete(homework.getUncomplate());
        double v = Double.parseDouble(String.valueOf(homework.getComplate()));
        BigDecimal bd = new BigDecimal(v / personNum).setScale(2, RoundingMode.HALF_UP);
        totalSituation.setCompleteRate(bd.doubleValue());

//        获取一个作业所有学生的整体完成数据
        QueryWrapper<TpaHomeworkSummary> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("hid",hid);
        List<TpaHomeworkSummary> summaries = summaryMapper.selectList(queryWrapper);
//        计算平均分
        AtomicInteger totalScore= new AtomicInteger();
        AtomicInteger good= new AtomicInteger();
        AtomicInteger middle= new AtomicInteger();
        AtomicInteger poor= new AtomicInteger();
        summaries.forEach(s->{
            if (s.getScore()!=null){
                totalScore.addAndGet(s.getScore());
                if (s.getScore()>homework.getScore()*0.8){
                    good.getAndIncrement();
                }else if(s.getScore()>=homework.getScore()*0.6){
                    middle.getAndIncrement();
                }else {
                    poor.getAndIncrement();
                }
            }
        });
        double v1 = Double.parseDouble(String.valueOf(totalScore.get()));
        totalSituation.setAvgScore(new BigDecimal(v1 / homework.getComplate()).setScale(2, RoundingMode.HALF_UP).doubleValue());
        totalSituation.setGood(good.get());
        totalSituation.setMiddle(middle.get());
        totalSituation.setPoor(poor.get());
        double vgood = Double.parseDouble(String.valueOf(good.get()));
        double vmiddle = Double.parseDouble(String.valueOf(middle.get()));
        double vpoor = Double.parseDouble(String.valueOf(poor.get()));
        totalSituation.setGoodRate(new BigDecimal(vgood / personNum).setScale(2, RoundingMode.HALF_UP).doubleValue());
        totalSituation.setPassingRate(new BigDecimal(vmiddle / personNum).setScale(2, RoundingMode.HALF_UP).doubleValue());
        totalSituation.setUnPassingRate(new BigDecimal(vpoor / personNum).setScale(2, RoundingMode.HALF_UP).doubleValue());

//        获取每个人，每道题的完成数据
        QueryWrapper<TpaHomeworkTrack> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.eq("hid",hid);
        List<TpaHomeworkTrack> tracks = trackMapper.selectList(queryWrapper1);
        //根据题目id进行分类，后续遍历题目就将这个题目的所有学生回答互数据拿出来
        Map<Integer, List<TpaHomeworkTrack>> trackMap = tracks.stream().collect(Collectors.groupingBy(TpaHomeworkTrack::getQid));
//        获取这个作业的题目
        List<TpaHomeworkDetails> questionByHId = detailsMapper.getQuestionByHId(hid);
        Map<String, Integer> pointMap=new HashMap<>();
        questionByHId.forEach(q->{
//            将这个题目的学生回答数据拿出来
            List<TpaHomeworkTrack> questionTracks = trackMap.getOrDefault(q.getQid(), new ArrayList<>());
            AtomicInteger pointMistake= new AtomicInteger();
            questionTracks.forEach(qt->{
                if (qt.getIsCorrect()!=null){
                    if (qt.getIsCorrect()==0)
                     pointMistake.getAndIncrement();
                }
            });
            if (pointMap.get(q.getQtitle())==null){
                pointMap.put(q.getQtitle(),pointMistake.get());
            }else {
                pointMap.put(q.getQtitle(),pointMap.get(q.getQtitle())+pointMistake.get());
            }
        });
        // 将Map转换为List，以便排序
        List<Map.Entry<String, Integer>> sortedKnowledgePoints = new ArrayList<>(pointMap.entrySet());
        // 按错误次数从高到低排序
        sortedKnowledgePoints.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
//    private List<Map<String,Integer>> mistakePoint;//每一个知识点错误的人数，人数最多的就是错的最多的知识点
        totalSituation.setMistakePoint(sortedKnowledgePoints);
        return totalSituation;
    }

//    TODO 根据作业知识点和班级id
    @Override
    public List<TpaHomework> selectByPointAndCid(String title, Integer uid) {
        return  homeworkMapper.selectByAuthorIdAndTitle(title, uid);
    }

    //    处理题目生成请求发送和结构处理的方法
    private List<TpaHomeworkDetails> sendAndParse(TpaHomework homework) {
        StringBuilder request=new StringBuilder();
        List<TpaHomeworkDetails> details = new ArrayList<>();
        request.append("请帮我生成一份人工智能通识课作业，作业涉及知识点为: “").append(homework.getHTitle()).
                append("--").append(homework.getSecondaryTitle()).
                append("“,作业题目个数为: ").append(homework.getQuantity()).
                append("个,作业题目类型为: ").append(homework.getProblemType());
        if (homework.getScore()==0) request.append("\n题目的总分为：根据生成的题目数以及各个题目的分值来确定");
           else  request.append("\n题目的总分为：").append(homework.getScore());
        request.append("\n题目的难度需求为：").append(homework.getDifficulty()).
                append("\n生成要求：1、每道题的分值根据前面指定的总分和题目数量，并根据题目难度和题型来进行分配，不要均分\n2、我要的题目数量是：")
                        .append(homework.getQuantity()).append("个,请不要多生成，也不要少生成\n3、生成的每道题的分值我只要一个单独的数字，不要给分值前后加空格或者汉字\n4、如果题目是选择题，你生成的答案就必须是A、B、C、D这样的选项" +
                         "\n5、选择题、填空题和简答题都一定要生成解析！\n6、选择题的题目类型请分为单选题和多选题，不要使用“选择题”这个来作为题目的类型\n7、注意：题目的知识点不要重复，可以生成拓展知识点的题目");
        try {
            String questionString = taskGenerate.chatClient(request.toString(), homework.getHid().toString(), botId);
//            将题目转为对象，对字符串进行分割
            String[] questions = questionString.split("\\|\\|");
            System.out.println(Arrays.toString(questions));
            List<String> questions1 = new ArrayList<>(List.of(questions));
            questions1.remove(""); //将空串移除
            if(questions.length< homework.getQuantity()) throw new FormatException("题目格式错误，生成失败请重新尝试");
            StringToBean(details, questions1);
//            将选择题的选项改为数组形式,方便前端渲染!!!!!!!!!!!!!!!!!,这里只是将字符串选项转为集合格式的，赋给另外一个option变量
//            并没与改变Selections字符串格式选项的值，后续前端检查完后仍然可以将这个字符串返回然后插入到数据库中
            formOptions(details);//处理选项
        } catch (Exception e) {
            e.printStackTrace();
        }
        return details;
    }

//    这里处理过后，前端审核完成再插入数据库猴子那个就没有空格
    public static void formOptions(List<TpaHomeworkDetails> details) throws FormatException {
        for (TpaHomeworkDetails details1:details){
            if(details1.getSelections()!=null) {
                details1.setSelections(details1.getSelections().replace("\\",""));
                details1.setOptions(OptionUtil.optionDeal(details1.getSelections()));
                details1.setOptions(details1.getOptions().
                        stream().filter(s -> !s.isEmpty()).
                        collect(Collectors.toList()));
            }
        }
    }

    public static List<TpaHomeworkDetails> StringToBean(List<TpaHomeworkDetails> details, List<String> questions) throws FormatException {
        for(String question:questions){
            TpaHomeworkDetails details1 = new TpaHomeworkDetails();
            if (!question.contains("|")) continue;//将不包含题目的数据去掉
            String[] columns = question.split("\\|");
            if(columns.length>=8){
                details1.setQtype(columns[1].replace(" ", ""));//类型
                details1.setQtitle(columns[2]);//知识点
                details1.setQcontent(columns[3]);//题干
                details1.setCorrectAnswer(columns[4]); //答案
                details1.setAnswerAnalysis(columns[5]); //解析
                details1.setQdefficult(columns[6]);//难度
                String score=columns[7];
                if (score.contains("分")||score.contains(" ")) score=score.replace("分","").replaceAll("\\s+", "");
                if (!score.isEmpty())details1.setDefaultScore(Integer.parseInt(score));//分值
                if(columns.length>=9){
                    if(!columns[8].contains("无")&&columns[8].length()>3)
                     details1.setSelections(columns[8]);//选项
                }
            }else throw new FormatException("题目格式错误，生成失败请重新尝试");
            details.add(details1);
        }
        return details;
    }
}
//        这是弃用的一个作业绑多个班级的方案
//        if(homework.getCid()==null){
////          如果这个作业是第一次和作业关联，就将,cid,这个字符串插入，方便后续根据,cid,匹配到完整的班级id
//            homework.setCid(","+releaseVo.getCid()+",");
//        }else if(!homework.getCid().contains(","+releaseVo.getCid()+",")){
//            homework.setCid(homework.getCid()+releaseVo.getCid()+",");
//        }else return false;
