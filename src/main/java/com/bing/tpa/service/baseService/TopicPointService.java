package com.bing.tpa.service.baseService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.domain.VO.PointLink;
import com.bing.tpa.domain.dto.TopicPointDTO;
import com.bing.tpa.domain.dto.TopicPointDto2;
import com.bing.tpa.domain.dto.UnifiedKnowledgePoint;
import com.bing.tpa.domain.dto.UnifiedKnowledgeRelation;
import com.bing.tpa.domain.entity.TopicPoint;
import com.bing.tpa.exception.RepeatException;
import com.bing.tpa.service.baseImpl.KnowledgeGraph;

import javax.validation.Valid;
import java.util.List;

public interface TopicPointService extends IService<TopicPoint> {
    // 获取学校的所有知识点（混合模板和自定义）
    List<UnifiedKnowledgePoint> getSchoolKnowledgePoints(Integer schoolId);

    // 获取学校知识点的所有关系
    List<UnifiedKnowledgeRelation> getSchoolKnowledgeRelations(Integer schoolId);

    // 构建学校知识图谱
//    KnowledgeGraph buildSchoolKnowledgeGraph(Integer schoolId);

    // 构建学校知识图谱
    KnowledgeGraph buildSchoolKnowledgeGraph(Integer schoolId, Integer tsId);

    TopicPoint createTopicPoint(TopicPointDTO topicPointDTO, Integer tsId, Integer schoolId);
    
    TopicPoint updateTopicPoint(TopicPointDTO topicPointDTO);
    
    boolean deleteTopicPoint(Integer tssId);
    
    List<TopicPoint> getTopicPointsByTsId(Integer tsId);
    
    List<TopicPoint> getTopicPointsByTemplateId(Integer templateId);
    
    List<TopicPoint> getTopicPointsBySchoolId(Integer schoolId);
    
    TopicPoint getTopicPointById(Integer tssId);
    
    TopicPoint copyFromTemplate(Integer templateId, Integer tsId, Integer schoolId);

    List<TopicPoint> getTopicTree(Integer tsId);

    boolean copyTopicPoints(Integer sourceTsId, Integer targetTsId, List<Integer> topicIds);

    TopicPoint modifyNode(@Valid TopicPoint point);

    void saveKnowledge(Integer schoolId, Integer tsId, UnifiedKnowledgePoint rootNode,Integer parentId) throws RepeatException;

    List<TopicPointDto2> getUnderTow(Integer schoolId, Integer tsId);

    boolean deleteKnowledgePoint(Integer pointId, Integer schoolId, Integer tsId);

    List<PointLink> getPointsList(Integer schoolId, Integer tsId);

}
