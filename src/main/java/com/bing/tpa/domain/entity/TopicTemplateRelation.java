package com.bing.tpa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 知识点模板关系实体类
 * 用于存储通用模板中知识点之间的层级关系和逻辑关联
 */
@Data
@TableName("topic_template_relations")
public class TopicTemplateRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关系ID，主键，自增
     */
    @TableId(value = "template_relation_id",type = IdType.AUTO)
    private Long templateRelationId;

    /**
     * 父知识点ID（引用topic_templates表的template_id）
     */
    @TableField(value = "parent_template_id")
    private Long parentTemplateId;

    /**
     * 子知识点ID（引用topic_templates表的template_id）
     */
    @TableField(value = "child_template_id")
    private Long childTemplateId;

    /**
     * 关系类型：
     * 1-父子关系（默认）
     * 2-前驱后继关系
     * 3-关联关系
     * 4-包含关系
     */
    @TableField(value = "relation_type")
    private Integer relationType;

    /**
     * 关系描述
     */
    @TableField(value = "relation_desc")
    private String relationDesc;

    /**
     * 排序序号，用于同一层级的排序
     */
    @TableField(value = "order_num")
    private Integer orderNum;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
}
