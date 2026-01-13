package com.bing.tpa.domain.dto;

import lombok.Data;

@Data
public class UnifiedKnowledgeRelation {
    private Integer id;//统一字段id
    private Integer relationId;
    private String source;       // 关系来源: 'template' 或 'custom'
    private Integer parentId;
    private Integer childId;
    private Integer relationType;
    private String relationDesc;
    private Integer schoolId;    // 所属学校ID（自定义关系特有）
    private Integer tsId;        // 所属课程ID
    private Integer templateRelationId;
    private Integer orderNum;// 关联的模板关系ID

    // 获取统一ID的方法
    public Integer getId() {
        if ("custom".equals(source)) {
            return relationId;
        } else {
            return templateRelationId;
        }
    }
}
