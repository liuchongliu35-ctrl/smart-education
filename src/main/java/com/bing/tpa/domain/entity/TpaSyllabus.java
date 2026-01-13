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
import springfox.documentation.annotations.ApiIgnore;

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
@TableName("tpa_syllabus")
@ApiModel(value = "TpaSyllabus对象", description = "")
public class TpaSyllabus implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "生成大纲时不需要这个教学大纲id",required = true)
    @TableId(value = "syllabus_id", type = IdType.AUTO)
    private Integer syllabusId;

    @ApiModelProperty(value = "该教学设计大纲来自于哪一个教学设计，教学设计的id")
    @TableField(value = "td_id")
    private String tdId;


    @ApiModelProperty("大纲的名字")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty(value = "教学设计的作者id，默认为000，表示来自于AI后续根据这个id查找教师教的课程",required = true)
    @TableField(value = "author_id",update = "false")
    @NotNull
    private String authorId;

    @ApiModelProperty(value = "该大纲是否公开，0：不公开，1：公开",required = true)
    @TableField("is_open")
    private Integer isOpen;

    @ApiModelProperty(value = "教学大纲的内容，为富文本的形式",required = true)
    @TableField("content")
    private String content;

    @ApiModelProperty("大纲的类型")
    @TableField("type")
    private String type;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty("是否逻辑删除，0：未删除，1：删除")
    @TableField("is_delete")
    private Integer isDelete;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty("是否可以修改，1表示可以，0表示不可以")
    @TableField("is_update")
    private Integer isUpdate;

    @ApiModelProperty("大纲对应的人工智能通识课的章节")
    @TableField("top_title")
    private String topTitle;

    @ApiModelProperty("大纲章节对应的小节")
    @TableField("secondary_title")
    private String secondaryTitle;

    @TableField(exist = false)
    private Integer num;

}
