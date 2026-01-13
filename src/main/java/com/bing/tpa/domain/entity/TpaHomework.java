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
import org.jetbrains.annotations.NotNull;

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
@TableName("tpa_homework")
@ApiModel(value = "TpaHomework对象", description = "")
public class TpaHomework implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("作业id")
    @TableId(value = "hid", type = IdType.AUTO)
    private Integer hid;

    @ApiModelProperty("发布的时候再绑定班级的id，和班级进行绑定，以班级为单位对作业进行区分")
    @TableField("cid")
    private Integer cid;

    @ApiModelProperty("老师的id")
    @TableField("author_id")
    private Integer authorId;

    @ApiModelProperty(value = "作业名称，可以随意取",required = true)
    @TableField("h_name")
    private String hName;

    @ApiModelProperty(value = "作业一级主题（主题可以限制知识点的范围）",required = true)
    @TableField("h_title")
    private String hTitle;

    @ApiModelProperty(value = "二级主题，和教学设计的那两个字段作用一样",required = true)
    @TableField("secondary_title")
    private String secondaryTitle;

    @ApiModelProperty("作业说明")
    @TableField("explanation")
    private String explanation;

    @ApiModelProperty(value = "作业总分",required = true)
    @TableField("score")
    private Integer score;

    @ApiModelProperty("完成人数")
    @TableField("complate")
    private Integer complate;

    @ApiModelProperty("未完成人数")
    @TableField("uncomplate")
    private Integer uncomplate;

    @ApiModelProperty(value = "题目数量",required = true)
    @TableField("quantity")
    private Integer quantity;

    @ApiModelProperty("作业创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("截止时间")
    @TableField("deadline")
    private LocalDateTime deadline;

    @ApiModelProperty("作业状态，0：未发布，已发布：1，已结束：-1")
    @TableField("state")
    private Integer state;

    @ApiModelProperty(value = "题目类型（选择题/填空题/简答题/混合题型的话使用字符串和逗号连接）",required = true)
    @TableField("problem_type")
    private String problemType;

    @ApiModelProperty(value = "是否公开，0不公开，1公开",required = true)
    @TableField("is_open")
    private Integer isOpen;

    @ApiModelProperty(value = "作业类型，0：普通作业，1：考试",required = true)
    @TableField("h_type")
    private Integer hType;

    @ApiModelProperty(value = "作业难度",required = true)
    @TableField("difficulty")
    private String  difficulty;

    @TableField(exist = false)
    private Integer isSend;
    @TableField(exist = false)
    private String className;


}
