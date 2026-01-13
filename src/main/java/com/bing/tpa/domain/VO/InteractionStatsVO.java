package com.bing.tpa.domain.VO;

import lombok.Data;

import java.util.Map;

@Data
public class InteractionStatsVO {
    private Integer hdId; // 互动ID
    private String title; // 互动标题（从problem字段提取）
    private String answer;//如果是互动题，就需要记录题目的答案
    // 基础数据
    private Integer participation; // 参与人数
    private Integer correctCount; // 正确人数
    private Double accuracyRate; // 正确率
    private Integer viewpointCount; // 有效观点数

    // 衍生数据
    private Double viewpointDensity; // 观点密度（观点数/参与人数）
    private Double join;//参与率
    private Double effectiveInteraction;
    private Double interactionScore; // 互动得分（公式见下文）
    private Long durationHours; // 互动持续时间（小时）
    private String timePeriod; // 时间段标签（上午/下午/晚间）
    private Map<String, Integer> questionTypeDistribution; // 问题类型分布
}
