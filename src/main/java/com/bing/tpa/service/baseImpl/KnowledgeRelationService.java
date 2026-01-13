package com.bing.tpa.service.baseImpl;

import com.bing.tpa.domain.dto.TopicPointLinkDto;
import com.bing.tpa.domain.entity.TopicRelation;
import com.bing.tpa.domain.entity.TopicTemplateRelation;
import com.bing.tpa.mapper.TopicRelationMapper;
import com.bing.tpa.mapper.TopicTemplateRelationMapper;
import com.bing.tpa.service.baseService.TopicPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class KnowledgeRelationService {


    @Autowired
    private TopicPointService topicPointService;

    @Autowired
    private TopicRelationMapper topicRelationMapper;

    @Autowired
    private TopicTemplateRelationMapper topicTemplateRelationMapper;

    // 构建完整的学校知识图谱
    public KnowledgeGraph buildFullKnowledgeGraph(Integer schoolId,Integer tsId) {
        return topicPointService.buildSchoolKnowledgeGraph(schoolId,tsId);
    }

    // 获取知识点的局部关系网
    public List<TopicPointLinkDto> getLocalRelationNetwork(Integer schoolId, Integer pointId, Integer tsId) {
        KnowledgeGraph graph = topicPointService.buildSchoolKnowledgeGraph(schoolId,tsId);
        return graph.getRelationsCenteredAt(pointId);
    }

    // 添加自定义关系
    public void addCustomRelation(Integer schoolId, Integer tsId,
                                  Integer parentId, Integer childId,
                                  Integer relationType, String relationDesc) {
        TopicRelation relation = new TopicRelation();
        relation.setParentTssId(parentId);
        relation.setChildTssId(childId);
        relation.setRelationType(relationType);
        relation.setRelationDesc(relationDesc);//关系描述
        relation.setTsId(tsId);
        relation.setSchoolId(schoolId);
        relation.setCreateTime(LocalDateTime.now());

        topicRelationMapper.insert(relation);
    }

    // 迁移模板关系为自定义关系
    @Transactional
    public void migrateTemplateRelation(Integer schoolId, Integer templateRelationId) {
        // 1. 查询模板关系
        TopicTemplateRelation templateRelation =
                topicTemplateRelationMapper.selectById(templateRelationId);

        if (templateRelation == null) {
            throw new RuntimeException("模板关系不存在");
        }

        // 2. 创建自定义关系
        TopicRelation customRelation = new TopicRelation();
        customRelation.setParentTssId(Math.toIntExact(templateRelation.getParentTemplateId()));
        customRelation.setChildTssId(Math.toIntExact(templateRelation.getChildTemplateId()));
        customRelation.setRelationType(templateRelation.getRelationType());
        customRelation.setTemplateRelationId(templateRelationId);
        customRelation.setSchoolId(schoolId);
        customRelation.setCreateTime(LocalDateTime.now());

        // 3. 保存
        topicRelationMapper.insert(customRelation);
    }
}
