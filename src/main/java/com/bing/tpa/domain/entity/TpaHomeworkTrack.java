package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Data
@TableName("tpa_homework_track")
@ApiModel(value = "TpaHomeworkTrack对象", description = "")
public class TpaHomeworkTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("跟踪信息id")
    @TableId(value = "ht_id", type = IdType.AUTO)
    private Integer htId;
//     ！！！这个很重要，之前忘了加
    @ApiModelProperty("作业的id，后续匹配该作业的完成情况")
    @TableField(value = "hid")
    private Integer hid;
    //     ！！！这个很重要，之前忘了加
    @ApiModelProperty("预习任务的id,记录预习题的完成情况")
    @TableField(value = "pt_id")
    private Integer ptId;

    @ApiModelProperty("学生Id")
    @TableField("sid")
    private Integer sid;

    @ApiModelProperty("题目id")
    @TableField("qid")
    private Integer qid;

    @ApiModelProperty("完成情况，0:未开始，1：正在完成中，2：已完成，后端设置一个端口检测学生在题目页面的行为，当停留超过30秒就视为正在完成，并记录开始的时间")
    @TableField("status")
    private Integer status;

    @ApiModelProperty("学生的回答")
    @TableField("answer")
    private String answer;

    @ApiModelProperty("评分或获取的分数")
    @TableField("score")
    private Integer score;

    @ApiModelProperty("学生开始尝试解题的时间")
    @TableField("attempt_time")
    private LocalDateTime attemptTime;

    @ApiModelProperty("学生完成这道题的时间")
    @TableField("completion_time")
    private LocalDateTime completionTime;

    @ApiModelProperty("解题用时")
    @TableField("time_spent")
    private String timeSpent;

    @ApiModelProperty("是否正确")
    @TableField("is_correct")
    private Integer isCorrect;

    @ApiModelProperty("错误的可能原因（由AI根据学生的答案进行分析）")
    @TableField("mistake_case")
    private String mistakeCase;

    @ApiModelProperty("AI针对学生完成该题目的情况对学生进行额外知识的补充说明")
    @TableField("add_explanation")
    private String addExplanation;

//    @ApiModelProperty("学生姓名")
//    @TableField("stu_name") // 映射数据库中的 stu_name 字段
    @TableField(exist = false)
    private String stuName;

    @TableField(exist = false)
    private String point;//题目的知识点


}
