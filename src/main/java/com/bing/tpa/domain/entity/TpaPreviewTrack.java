package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@Getter
@Setter
@TableName("tpa_preview_track")
@ApiModel(value = "TpaPreviewTrack对象", description = "")
public class TpaPreviewTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("学生预习任务完成情况记录id，该表一条数据就是一个学生完成一个预习任务的情况")
    @TableId(value = "pid", type = IdType.AUTO)
    private Integer pid;

    @ApiModelProperty("该条记录对应的预习任务的id")
    @TableField("pt_id")
    private Integer ptId;

    @ApiModelProperty("该条记录对应的学生的id")
    @TableField("sid")
    private Integer sid;

    @ApiModelProperty("预习资料完成情况（-1：还未查看，0：还未完成，1：已完成）")
    @TableField("text_finish")
    private Integer textFinish;

    @ApiModelProperty("预习题是否完成（0：未完成，1：已完成）这两个都是1才是完成了预习任务")
    @TableField("question_finish")
    private Integer questionFinish;

    @ApiModelProperty("预习资料题目得分")
    @TableField("text_score")
    private Integer textScore;


    @ApiModelProperty("对预习资料的不懂的地方提出疑问")
    @TableField(" data_inquiry")
    private String dataInquiry;

    @ApiModelProperty("开始预习时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @ApiModelProperty("结束预习时间")
    @TableField("finish_time")
    private LocalDateTime finishTime;

    @ApiModelProperty("预习题得分")
    @TableField("question_score")
    private Integer questionScore;


    @ApiModelProperty("预习题完成数量")
    @TableField("complete_num")
    private Integer completeNum;

    @ApiModelProperty("预习资料题目回答，使用json串形式储存")
    @TableField("text_answer")
    private String textAnswer;

    @ApiModelProperty("预习题数量")
    @TableField("question_num")
    private Integer questionNum;

    @ApiModelProperty("补充资料，AI智能补充，该学生在这一个预习任务的情况个性化推荐资料")
    @TableField("supplement")
    private String supplement;

    @ApiModelProperty("补充题目的个数，题目详情放到tpa_homework_details")
    @TableField("add_question")
    private Integer addQuestion;

    @ApiModelProperty("AI对该学生预习情况的分析，包括对老师辅导该学生的建议")
    @TableField("ai_analysis")
    private String aiAnalysis;

//    @ApiModelProperty("学生姓名")
//    @TableField("stu_name") // 映射数据库中的 stu_name 字段
    @TableField(exist = false)
    private String stuName;

    @TableField(exist = false)
    private String stuCode;

    @TableField(exist = false)
    private String completeTime;

}
