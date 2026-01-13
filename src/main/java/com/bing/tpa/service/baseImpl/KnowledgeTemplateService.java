package com.bing.tpa.service.baseImpl;

import com.bing.tpa.domain.dto.UnifiedKnowledgePoint;
import com.bing.tpa.domain.dto.UnifiedKnowledgeRelation;
import com.bing.tpa.domain.entity.TopicTemplate;
import com.bing.tpa.domain.entity.TopicTemplateRelation;
import com.bing.tpa.mapper.TopicTemplateRelationMapper;
import com.bing.tpa.service.baseService.TopicTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KnowledgeTemplateService {
    @Autowired
    private TopicTemplateService topicTemplateService;

    @Autowired
    private TopicTemplateRelationMapper topicTemplateRelationMapper;

    // 获取完整的模板知识网（包括知识点和关系）
    public Map<String, Object> getTemplateKnowledgeGraph() {
        Map<String, Object> result = new HashMap<>();

        // 获取所有模板知识点
        List<TopicTemplate> templates = topicTemplateService.getAllTemplates();
        List<UnifiedKnowledgePoint> points = templates.stream()
                .map(this::convertToUnifiedPoint)
                .collect(Collectors.toList());

        // 获取所有模板关系
        List<TopicTemplateRelation> relations = topicTemplateRelationMapper.getAllTemplateRelations();
        List<UnifiedKnowledgeRelation> unifiedRelations = relations.stream()
                .map(this::convertToUnifiedRelation)
                .collect(Collectors.toList());

        // 构建树形结构
        Map<Integer, UnifiedKnowledgePoint> pointMap = new HashMap<>();
        List<UnifiedKnowledgePoint> rootPoints = new ArrayList<>();

        // 创建节点映射
        for (UnifiedKnowledgePoint point : points) {
            pointMap.put(point.getId(), point);
            if (point.getLevel() == 1) {
                rootPoints.add(point);
            }
        }

        // 构建层级关系
        for (UnifiedKnowledgeRelation rel : unifiedRelations) {
            if (rel.getRelationType() == 1) { // 层级关系
                UnifiedKnowledgePoint parent = pointMap.get(rel.getParentId());
                UnifiedKnowledgePoint child = pointMap.get(rel.getChildId());
                if (parent != null && child != null) {
                    parent.getChildren().add(child);
                }
            }
        }

        result.put("points", points);
        result.put("relations", unifiedRelations);
        result.put("tree", rootPoints);

        return result;
    }

    private UnifiedKnowledgePoint convertToUnifiedPoint(TopicTemplate template) {
        UnifiedKnowledgePoint point = new UnifiedKnowledgePoint();
        point.setId(template.getTemplateId());
        point.setTemplateId(template.getTemplateId());//显式将模版id赋给结果体
        point.setSource("template");
        point.setTopTitle(template.getTopTitle());
        point.setSecondaryTitle(template.getSecondaryTitle());
        point.setContent(template.getContent());
        point.setLevel(template.getLevel());
        point.setTsId(template.getTsId());
        return point;
    }

    private UnifiedKnowledgeRelation convertToUnifiedRelation(TopicTemplateRelation relation) {
        UnifiedKnowledgeRelation unified = new UnifiedKnowledgeRelation();
        unified.setSource("template");
        unified.setParentId(Math.toIntExact(relation.getParentTemplateId()));
        unified.setChildId(Math.toIntExact(relation.getChildTemplateId()));
        unified.setRelationType(relation.getRelationType());
        unified.setRelationDesc(relation.getRelationDesc());
        return unified;
    }
}
