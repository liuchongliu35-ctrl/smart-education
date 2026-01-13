package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
@TableName("tpa_preview_task")
@ApiModel(value = "TpaPreviewTask对象", description = "")
public class TpaPreviewTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("预习任务表")
    @TableId(value = "pt_id", type = IdType.AUTO)
    private Integer ptId;

    @ApiModelProperty("班级id")
    @TableField("cid")
    private Integer cid;

    @ApiModelProperty(value = "预习任务发布人",required = true)
    @TableField("author_id")
    private Integer authorId;


    @ApiModelProperty(value = "预习任务名称",required = true)
    @TableField("preview_name")
    private String previewName;

    @ApiModelProperty(value = "预习任务一级主题",required = true)
    @TableField("p_title")
    private String ptitle;

    @ApiModelProperty(value = "预习任务的二级主题",required = true)
    @TableField("secondary_title")
    private String secondaryTitle;

    @ApiModelProperty("预习任务额外说明")
    @TableField("p_explanation")
    private String pexplanation;

    @ApiModelProperty("预习任务资料内容（富文本）")
    @TableField("preview_content")
    private String previewContent;

    @ApiModelProperty("预习任务完成人数")
    @TableField("complete")
    private Integer complete;

    @TableField(exist = false)
    private Integer unComplete;

    @ApiModelProperty(value = "预习任务资源是否公开",required = true)
    @TableField("is_open")
    private Integer isOpen;

    @ApiModelProperty("预习任务发布时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("截止时间")
    @TableField("deadline")
    private LocalDateTime deadline;

    @ApiModelProperty("预习任务状态（0：未发布，已发布：1，已结束：-1）")
    @TableField("active")
    private Integer active;

    @ApiModelProperty("预习任务附加题，每题5分，只有完成才算完成了预习资料,使用####隔开，每道题需要有“1.“这样的编号")
    @TableField("problem")
    private String problem;

    @ApiModelProperty("预习任务反馈")
    @TableField("feedback")
    private String feedback;

    @ApiModelProperty("老师给预习资料完成加一个限时，原则上学生完成预习资料不能超过这个时间")
    @TableField("by_time")
    private Integer byTime;

    @ApiModelProperty(value = "预习题的数量",required = true)
    @TableField("questions_num")
    private Integer questionsNum;

    @ApiModelProperty(value = "预习题总分",required = true)
    @TableField("questions_grade")
    private Integer questionsGrade;

    @ApiModelProperty(value = "题目类型,选择，填空、简答等",required = true)
    @TableField("problem_type")
    private String problemType;

    @TableField(exist = false)
    private Integer isSend;
    @TableField(exist = false)
    private String className;



}
