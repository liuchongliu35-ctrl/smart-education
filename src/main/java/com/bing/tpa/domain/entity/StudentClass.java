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
@TableName("student_class")
@ApiModel(value = "StudentClass对象", description = "")
public class StudentClass implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("学生-班级关系id")
    @TableId(value = "sc_id", type = IdType.AUTO)
    private Integer scId;

    @ApiModelProperty("学生的id")
    @TableField("sid")
    private Integer sid;

    @ApiModelProperty("班级id")
    @TableField("cid")
    private Integer cid;

    @ApiModelProperty("该学生是否被移除班级")
    @TableField("is_exit")
    private Integer isExit;

    @ApiModelProperty("该学生在该班级中的排名")
    @TableField("ranking")
    private Integer ranking;

    @ApiModelProperty("AI对学生的综合评价")
    @TableField("evaluation")
    private String evaluation;

    @TableField(exist = false)
    private String stuName;


}
