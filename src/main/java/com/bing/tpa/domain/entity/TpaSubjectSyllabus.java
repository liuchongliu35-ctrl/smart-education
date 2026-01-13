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
@TableName("tpa_subject_syllabus")
@ApiModel(value = "TpaSubjectSyllabus对象", description = "")
public class TpaSubjectSyllabus implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("知识点id")
    @TableId(value = "tss_id", type = IdType.AUTO)
    private Integer tssId;

    @ApiModelProperty("学科id")
    @TableField("ts_id")
    private Integer tsId;

    @ApiModelProperty("一级主题（Ai根据学科来自动生成）")
    @TableField("top_title")
    private String topTitle;

    @ApiModelProperty("二级主题（Ai根据学科和单元/章节内容来自动生成）")
    @TableField("secondary_title")
    private String secondaryTitle;

    @ApiModelProperty("老师自定义的二级主题，但是一级主题不可变，单元/章节不变")
    @TableField("defined_title")
    private String definedTitle;


}
