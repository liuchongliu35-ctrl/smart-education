package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("topic_points")
public class TopicPoint {
    @TableId(type = IdType.AUTO)
    private Integer tssId;
    private Integer tsId;
    private Integer schoolId;
    private Integer templateId;
    private String topTitle;
    private String secondaryTitle;
    private String definedTitle;
    private String content;
    private Integer level;
    private Integer isTemplate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<TopicPoint> children;
    @TableField(exist = false)
    private Integer relationType;
}
