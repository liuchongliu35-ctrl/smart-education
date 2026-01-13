package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@TableName("tpa_design_before")
@ApiModel(value = "TpaDesignBefore对象", description = "")
public class TpaDesignBefore implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("教学设计前置数据的id")
    @TableId(value = "dc_id", type = IdType.AUTO)
    private Integer dcId;

    @ApiModelProperty("关联的教学设计")
    @TableField("td_id")
    private Integer tdId;

    @ApiModelProperty("学期分析依据数据来源（verchar）作业或者预习任务的id，使用逗号隔开")
    @TableField("analysis_from")
    private String analysisFrom;

    @ApiModelProperty("AI根据学生完成学习任务产生的数据来进行分析，并生成结论告诉老师该如何进行教学")
    @TableField("analysis_result")
    private String analysisResult;

    @ApiModelProperty("Ai根据教学设计的两个主题来预测该教学设计可能涉及到的内容")
    @TableField("knowledge_analysis")
    private String knowledgeAnalysis;

    @ApiModelProperty("该教学设计可能使用的图片")
    @TableField("photo")
    private String photo;

    @ApiModelProperty("该教学设计可能使用的视频资源")
    @TableField("video")
    private String video;

    @ApiModelProperty("上节课的互动数据分析结果和建议，需要包含上节课的主题")
    @TableField("interaction_analysis")
    private String interactionAnalysis;

    @ApiModelProperty("预习任务数据分析结果")
    @TableField("preview_result")
    private String previewResult;


}
