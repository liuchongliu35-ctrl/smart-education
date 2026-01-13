package com.bing.tpa.domain.VO;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeGraphVO {
    private Integer tsId;
    private String subjectName;
    private Integer schoolId;
    private String schoolName;
    private List<KnowledgeNodeVO> nodes;
    private List<KnowledgeEdgeVO> edges;
}
