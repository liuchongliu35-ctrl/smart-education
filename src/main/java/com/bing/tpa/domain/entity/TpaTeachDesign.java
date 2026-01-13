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
@TableName("tpa_teach_design")
@ApiModel(value = "TpaTeachDesign对象", description = "")
public class TpaTeachDesign implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("教学设计的id")
    @TableId(value = "td_id", type = IdType.AUTO)
    private Integer tdId;

    @ApiModelProperty(value = "教学设计名称",required = true)
    @TableField("design_name")
    private String designName;

    @ApiModelProperty(value = "教学设计的涉及的人工智能通识课的章节",required = true)
    @TableField("design_title")
    private String designTitle;

    @ApiModelProperty(value = "教学设计的涉及的人工智能通识课的章节的小节）",required = true)
    @TableField("secondary_title")
    private String secondaryTitle;

    @ApiModelProperty(value = "教学设计的学科",required = true)
    @TableField("subject")
    private String subject;

    @ApiModelProperty(value = "作者id",required = true)
    @TableField("author_id")
    private Integer authorId;

    @ApiModelProperty("是否公开，0：不公开，1：公开")
    @TableField("is_open")
    private Integer isOpen;

    @ApiModelProperty("教学设计的内容（富文本）")
    @TableField("content")
    private String content;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "课时数",required = true)
    @TableField("class_time")
    private String classTime;

    @ApiModelProperty(value = "授课对象（比如：高中一年级学生）",required = true)
    @TableField("target")
    private String target;

    @ApiModelProperty("最近的修改时间")
    @TableField("last_modify")
    private String lastModify;

    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private Boolean isHaveVideo;


}
