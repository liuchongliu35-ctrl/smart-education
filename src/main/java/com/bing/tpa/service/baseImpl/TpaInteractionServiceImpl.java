package com.bing.tpa.service.baseImpl;

import cn.hutool.core.lang.hash.Hash;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.VO.InteractionRequireVo;
import com.bing.tpa.domain.VO.InteractionStatsVO;
import com.bing.tpa.domain.entity.TpaClass;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.bing.tpa.mapper.TpaClassMapper;
import com.bing.tpa.mapper.TpaInteractionMapper;
import com.bing.tpa.domain.entity.TpaInteraction;
import com.bing.tpa.mapper.TpaTeachDesignMapper;
import com.bing.tpa.modelcall.designCall.ChatWithModel;
import com.bing.tpa.service.baseService.TpaInteractionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class TpaInteractionServiceImpl extends ServiceImpl<TpaInteractionMapper, TpaInteraction> implements TpaInteractionService {

    @Autowired
    private TpaInteractionMapper interactionMapper;

    @Autowired
    private TpaClassMapper classMapper;

    @Autowired
    private TpaTeachDesignMapper designMapper;

    @Autowired
    private  ChatWithModel chatWithModel;

    private final static String interactionBotId="7486408334200045568";

    // 获取单个互动详细统计
    @Override
    public InteractionStatsVO getInteractionStats(Integer hdId) {
        List<TpaInteraction> records = interactionMapper.selectList(
                new QueryWrapper<TpaInteraction>().eq("hd_id", hdId)
        );

        return calculateStats(records);
    }

    // 获取某个教学设计下的所有互动统计
    @Override
    public List<InteractionStatsVO> getInteractionsByTeachingDesign(Integer tdId) {
        List<TpaInteraction> records = interactionMapper.selectList(
                new QueryWrapper<TpaInteraction>().eq("td_id", tdId)
        );

        return groupByInteractionId(records);
    }

//    获取新地互动环节
    @Override
    public List<TpaInteraction> getInteractions(InteractionRequireVo requireVo) {
//        调用智能体获取互动环节
        return sendAndParse(requireVo);
    }

    @Override
    public List<TpaInteraction> interactionList(Integer tdId) {
//        根据教学设计id获取互动环节
        List<TpaInteraction> interactions = lambdaQuery()
                .eq(TpaInteraction::getTdId, tdId)
                .list();
        interactions.forEach(i->{
            if (i.getMethod().contains("问答式互动")){
//                将问答式互动的题目转为正常格式
                Gson gson = new Gson();
                List<TpaHomeworkDetails> details = gson.fromJson(i.getContent(), new com.google.gson.reflect.TypeToken<List<TpaHomeworkDetails>>() {}.getType());
                i.setDetails(details);
                i.setContent(null);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    HashMap<String, String> answerAndAnalysis = objectMapper.readValue(i.getAnswer(), HashMap.class);
                    List<String> answer = new ArrayList<>(answerAndAnalysis.keySet());
                    List<String> analysis = new ArrayList<>(answerAndAnalysis.values());
                    i.setAnswerList(answer);
                    i.setAnalysisList(analysis);
                    i.setAnswer(null);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
        return interactions;
    }

    public  List<TpaInteraction>  sendAndParse(InteractionRequireVo requireVo) {
//        组装请求
//        获取教学设计的知识点范围
        TpaTeachDesign design = designMapper.selectById(requireVo.getTdId());
        List<TpaInteraction> interactionList=new ArrayList<>();
//        TpaTeachDesign design=new TpaTeachDesign();
//        design.setDesignTitle("大数定律及中心极限定理");
//        design.setSecondaryTitle("中心极限定理");
//        生成与人工智能通识课“....--.....”这个知识点相关的
        StringBuilder request=new StringBuilder();
        request.append("生成与人工智能通识课程“").append(design.getDesignTitle()).append("--").append(design.getSecondaryTitle()).append("”")
                .append("这个知识点相关的3到4个互动环节\n输出格式有如下要求：1、不同的环节之间一定要使用 == 这个符号分割开\n2、问答式互动需要生成4-5个题目，题目类型可以多样\n3、问答式互动的题目之间使用 || 这个符号分隔开\n4、讨论式互动需要生成2-3个讨论话题\n5、讨论式互动需要生成进行讨论的方式、每一个话题的参考答案、分组方式").
                append("\n6、注意：至少生成第三个及以上的互动环节，参考知识库中有关互动形式的资料老设计不同种类的互动环节\n7、每一种互动环节都需要紧扣本次教学的主题\n8、除了问答式互动的题目之间需要 || 这个符号外，其他任何地方都不要出现这个符号\n9、注意：请将互动方式的文字使用[]号括起来，并将markdown的符号去掉");
        try {
            String interaction = chatWithModel.chatClient(request.toString(), requireVo.getTdId().toString(), interactionBotId);
            System.out.println(interaction);
//            切割interaction,根据规定，不同的互动环节使用====分隔开，问答式互动中题目之间使用 || 隔开，每道题的答案放在一个{}中
            String[] inter = interaction.split("==");
            for (String in:inter){
                String regex = "\\[([^\\]]+)\\]";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(in);
                TpaInteraction interaction1 = new TpaInteraction();
                if (matcher.find()) {
                    interaction1.setMethod(matcher.group(1));
                }
//                interaction1.setMethod();//设置该互动的方式
//                包装问答式互动的题目
                if(interaction1.getMethod().contains("问答式互动")){
//                    将问答式互动中的题目先转为对象，在转为json串储存到数据库中，后续从数据库中获取的时候再转为对象
                    String[] question = in.split("\\|\\|");
                    List<String> question1 = new ArrayList<>(List.of(question));
                    question1.remove(" ");
                    List<TpaHomeworkDetails> details = new ArrayList<>();
                    TpaHomeworkServiceImpl.StringToBean(details, question1);
                    interaction1.setDetails(details);
                    ObjectMapper objectMapper = new ObjectMapper();
                    interaction1.setContent(objectMapper.writeValueAsString(details));//将题目List转为JSON对象储存到互数据库中
                    /*Gson gson = new Gson();
                      List<TpaHomeworkDetails> details = gson.fromJson(json, new com.google.gson.reflect.TypeToken<List<TpaHomeworkDetails>>() {}.getType());*/
                    HashMap<String,String> answerAndAnalysis=new HashMap<>();
                    details.forEach(d-> answerAndAnalysis.put(d.getCorrectAnswer(),d.getAnswerAnalysis()));
                    /* HashMap<String, String> answerAndAnalysis = objectMapper.readValue(json, HashMap.class);*/
                    interaction1.setAnswer(objectMapper.writeValueAsString(answerAndAnalysis));
                    interaction1.setQuestionType(1);//客观题

                }else {
                    interaction1.setContent(in);
                    interaction1.setQuestionType(2);//主观题
                }
                interactionList.add(interaction1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return interactionList;
    }


    private List<InteractionStatsVO> groupByInteractionId(List<TpaInteraction> records) {
        return records.stream()
                .collect(Collectors.groupingBy(TpaInteraction::getHdId))
                .values().stream()
                .map(this::calculateStats)
                .collect(Collectors.toList());
    }

    private InteractionStatsVO calculateStats(List<TpaInteraction> records) {
        if (records.isEmpty()) return new InteractionStatsVO();

        InteractionStatsVO vo = new InteractionStatsVO();
        TpaInteraction firstRecord = records.get(0);

        // 基础数据映射
        vo.setHdId(firstRecord.getHdId());
        vo.setTitle(extractTitle(firstRecord.getMethod()));
        vo.setParticipation(firstRecord.getParticipates());
        vo.setCorrectCount(firstRecord.getCorrect());
        vo.setViewpointCount(firstRecord.getViewpointNum());
        if (firstRecord.getAnswer()!=null)
            vo.setAnswer(firstRecord.getAnswer());
        // 计算衍生数据
        // 1. 正确率
        if (vo.getParticipation() > 0) {
            vo.setAccuracyRate(vo.getCorrectCount() * 100.0 / vo.getParticipation());
        }

        // 2. 观点密度
        vo.setViewpointDensity(vo.getViewpointCount() * 1.0 / vo.getParticipation());

        // 3. 互动得分（示例公式可调整）
        vo.setInteractionScore(calculateInteractionScore(vo));

        // 4. 时间维度分析
        if (firstRecord.getDeadline() != null) {
            LocalDateTime createTime = firstRecord.getCreateTime(); // 假设有创建时间字段
            vo.setDurationHours(ChronoUnit.HOURS.between(createTime, firstRecord.getDeadline()));

            // 时间段分析
            int hour = createTime.getHour();
            vo.setTimePeriod(hour < 12 ? "上午" : (hour < 18 ? "下午" : "晚间"));
        }

        // 5. 问题类型分布
        vo.setQuestionTypeDistribution(records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getQuestionType() == 1 ? "客观题" : "主观题",
                        Collectors.summingInt(r -> 1))
                ));

//        参与率
        TpaTeachDesign design = designMapper.selectById(records.get(0).getTdId());
        TpaClass cla = classMapper.selectOne(new QueryWrapper<TpaClass>().eq("tid", design.getAuthorId()));
        vo.setJoin((double) (firstRecord.getParticipates()/cla.getPerson()));
//        有效互动率
        vo.setEffectiveInteraction(firstRecord.getCorrect() * 100.0 / firstRecord.getParticipates());
        return vo;
    }

    // 其他辅助方法
    private String extractTitle(String problem) {
        try {
            JSONObject json = JSON.parseObject(problem);
            return json.getString("title");
        } catch (Exception e) {
            return "未命名互动";
        }
    }

    private Double calculateInteractionScore(InteractionStatsVO vo) {
        // 示例计算公式（可根据业务需求调整权重）
        return vo.getAccuracyRate() * 0.6
                + vo.getViewpointDensity() * 30 * 0.3
                + Math.log(vo.getParticipation()) * 0.1;
    }
}