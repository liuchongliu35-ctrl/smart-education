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
@TableName("tpa_subject")
@ApiModel(value = "TpaSubject对象", description = "")
public class TpaSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("学科id")
    @TableId(value = "ts_id", type = IdType.AUTO)
    private Integer tsId;

    @ApiModelProperty(value = "课程所属的学校的id")
    @TableField("school_id")
    private Integer schoolId;

    @ApiModelProperty("课程名称")
    @TableField("subject_name")
    private String subjectName;

    @ApiModelProperty("课程要求，如教学范围，教学难度等")
    @TableField("subject_info")
    private String subjectInfo;

//    这个字段前端需要限制为下拉框，不可以由用户自定义
    @ApiModelProperty("学科所属的教学阶段，默认大学")
    @TableField("subject_stage")
    private String subjectStage;

    @ApiModelProperty("默认大学1年级")
    @TableField("grade")
    private Integer grade;

    @ApiModelProperty("课程使用的教材的信息")
    @TableField("volume")
    private String volume;

    @ApiModelProperty("课程类型，如，基础课程，核心课程，交叉课程，非计算机专业课程，系列课程")
    @TableField("subtitle")
    private String subtitle;

    @ApiModelProperty("课程定制声明，如学校教学计划，适用阶段等")
    @TableField("customize")
    private String customize;

    @ApiModelProperty("课程编号")
    @TableField("subcode")
    private String subcode;

    @ApiModelProperty("课程是否公开")
    @TableField("is_open")
    private Integer isOpen;

    @ApiModelProperty("课程目标")
    @TableField("purpose")
    private String purpose;





}
