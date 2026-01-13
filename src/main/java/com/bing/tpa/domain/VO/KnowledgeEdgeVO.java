package com.bing.tpa.domain.VO;

import lombok.Data;

@Data
public class KnowledgeEdgeVO {
    private Integer id;
    private Integer from;
    private Integer to;
    private Integer type; // 1=父子关系, 2=关联关系
    private Boolean isTemplate;
    private Integer templateRelationId;
}
