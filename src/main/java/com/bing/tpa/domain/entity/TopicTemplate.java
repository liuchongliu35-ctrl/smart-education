package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("topic_templates")
public class TopicTemplate {
    @TableId(value = "template_id",type = IdType.AUTO)
    private Integer templateId;
    @TableField(value = "top_title")
    private String topTitle;
    @TableField(value = "secondary_title")
    private String secondaryTitle;
    @TableField(value = "content")
    private String content;
    @TableField(value = "level")
    private Integer level;
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
    @TableField(value = "ts_id")
    private Integer tsId;
}
