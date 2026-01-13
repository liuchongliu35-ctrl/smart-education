package com.bing.tpa.service.baseService;


import com.bing.tpa.domain.VO.KnowledgeGraphVO;

public interface KnowledgeGraphService {
    KnowledgeGraphVO getKnowledgeGraphByTsId(Integer tsId);
    
    KnowledgeGraphVO getTemplateKnowledgeGraph(String topTitle);
    
    boolean updateKnowledgeGraph(KnowledgeGraphVO knowledgeGraphVO);

}
