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

import javax.validation.constraints.NotNull;

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
@TableName("tpa_teacher")
@ApiModel(value = "TpaTeacher对象", description = "")
/**
 * `uid` int NOT NULL AUTO_INCREMENT COMMENT '教师id',
 *   `ts_id` int DEFAULT NULL COMMENT '教授课程的id',
 *   `account` varchar(255) NOT NULL COMMENT '教师账户名',
 *   `password` varchar(255) NOT NULL COMMENT '账号密码',
 *   `teach_stage` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教学的阶段默认是大学',
 *   `stage_num` varchar(255) NOT NULL COMMENT '年级，默认大一',
 *   `teach_lesson` varchar(255) NOT NULL COMMENT '教学课程的名称',
 *   `phone` varchar(255) NOT NULL COMMENT '老师手机号',
 *   `sex` varchar(255) NOT NULL COMMENT '老师性别',
 *   `name` varchar(255) NOT NULL COMMENT '老师姓名',
 */
public class TpaTeacher implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("教师id")
    @TableId(value = "uid", type = IdType.AUTO)
    private Integer uid;

    @ApiModelProperty("教授课程id")
    @TableField(value = "ts_id")
    private Integer tsId;

    @NotNull
    @ApiModelProperty(value = "教师用户名，相当于username",required = true)
    @TableField("account")
    private String account;

    @NotNull
    @ApiModelProperty(value = "账号密码",required = true)
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "教学的阶段默认是大学",required = true)
    @TableField("teach_stage")
    private String teachStage;

    @ApiModelProperty(value = "年级，默认是大一",required = true)
    @TableField("stage_num")
    private Integer stageNum;

    @ApiModelProperty(value = "教学课程，默认为人工智能通识课",required = true)
    @TableField("teach_lesson")
    private String teachLesson;

    @NotNull
    @ApiModelProperty(value = "老师手机号",required = true)
    @TableField("phone")
    private String phone;

    @ApiModelProperty(value = "老师性别",required = true)
    @TableField("sex")
    private String sex;

    @NotNull
    @ApiModelProperty(value = "老师姓名",required = true)
    @TableField("name")
    private String name;

    @ApiModelProperty(value = "老师邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "老师状态")
    @TableField("is_active")
    private Integer isActive;

    @ApiModelProperty(value = "老师所属学校id",required  = false)
    @TableField("school_id")
    private Integer schoolId;

    @TableField(exist = false)
    private String schoolName;

    @ApiModelProperty("用户角色")
    @TableField("role")
    private String role;


}
