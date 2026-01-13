package com.bing.tpa.domain.VO;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeNodeVO {
    private Integer id;
    private String title;
    private Integer level;
    private String content;
    private Boolean isTemplate;
    private Integer templateId;
    private List<KnowledgeNodeVO> children;
    private List<Integer> relations;
    private Integer teachingOrder;
}
