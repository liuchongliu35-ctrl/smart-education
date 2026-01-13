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
@TableName("tpa_class")
@ApiModel(value = "TpaClass对象", description = "")
public class TpaClass implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("班级的id")
    @TableId(value = "cid", type = IdType.AUTO)
    private Integer cid;

    @ApiModelProperty(value = "该班级的老师id",required = true)
    @TableField("tid")
    private Integer tid;

    @ApiModelProperty("班级人数")
    @TableField("person")
    private Integer person;

    @ApiModelProperty(value = "班级名称",required = true)
    @TableField("c_name")
    private String cName;

    @ApiModelProperty(value = "班级教学科目",required = true)
    @TableField("c_subject")
    private String cSubject;

    @ApiModelProperty("班级编码")
    @TableField("class_code")
    private String classCode;

    @ApiModelProperty("班级短编码，加课码")
    @TableField("short_code")
    private String shortCode;



}
