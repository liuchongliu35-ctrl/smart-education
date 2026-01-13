package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("topic_relations")
public class TopicRelation {
    @TableId(type = IdType.AUTO)
    private Integer relationId;
    private Integer parentTssId;
    private Integer childTssId;
    private Integer relationType;
    private Integer tsId;
    private Integer schoolId;
    private Integer isTemplate;
    private Integer templateRelationId;
    private LocalDateTime createTime;
    @TableField("relation_desc")
    private String relationDesc;
}
