package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@Getter
@Setter
@TableName("tpa_student")
@ApiModel(value = "TpaStudent对象", description = "")
@Data
public class TpaStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("学生id")
    @TableId(value = "sid", type = IdType.AUTO)
    private Integer sid;

    @ApiModelProperty(value = "学生姓名",required = true)
    @TableField("stu_name")
    private String stuName;

    @ApiModelProperty(value = "学生的年级",required = true)
    @TableField("stu_stage")
    private String stuStage;

    @ApiModelProperty(value = "学生学号",required = true)
    @TableField("stu_num")
    private String stuNum;


}
