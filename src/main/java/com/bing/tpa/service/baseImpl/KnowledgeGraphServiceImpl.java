package com.bing.tpa.service.baseImpl;


import com.bing.tpa.domain.VO.KnowledgeEdgeVO;
import com.bing.tpa.domain.VO.KnowledgeGraphVO;
import com.bing.tpa.domain.VO.KnowledgeNodeVO;
import com.bing.tpa.domain.entity.*;
import com.bing.tpa.mapper.*;
import com.bing.tpa.service.baseService.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {
    
    @Autowired
    private TpaSubjectMapper subjectMapper;
    
    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private TopicTemplateMapper topicTemplateMapper;
    
    @Autowired
    private TopicPointMapper topicPointMapper;
    
    @Autowired
    private TopicRelationMapper topicRelationMapper;
    
    @Autowired
    private SchoolTopicMappingMapper schoolTopicMappingMapper;
    
    @Autowired
    private TeachingPlanMapper teachingPlanMapper;
    
    @Override
    public KnowledgeGraphVO getKnowledgeGraphByTsId(Integer tsId) {
        // 获取课程信息
        TpaSubject subject = subjectMapper.selectById(tsId);
        if (subject == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 获取学校信息
        School school = schoolMapper.selectById(subject.getSchoolId());
        if (school == null) {
            throw new RuntimeException("学校不存在");
        }
        
        // 获取课程下的所有知识点
        List<TopicPoint> topicPoints = topicPointMapper.findByTsIdAndSchoolId(tsId, subject.getSchoolId());
        
        // 获取知识点关系
        List<TopicRelation> relations = topicRelationMapper.findByTsIdAndSchoolId(tsId, subject.getSchoolId());
        
        // 获取教学计划
        List<TeachingPlan> teachingPlans = teachingPlanMapper.findByTsIdAndSchoolId(tsId, subject.getSchoolId());
        Map<Integer, Integer> teachingOrderMap = teachingPlans.stream()
                .collect(Collectors.toMap(TeachingPlan::getTssId, TeachingPlan::getPlanOrder));
        
        // 构建知识点节点
        Map<Integer, KnowledgeNodeVO> nodeMap = new HashMap<>();
        for (TopicPoint point : topicPoints) {
            KnowledgeNodeVO node = new KnowledgeNodeVO();
            node.setId(point.getTssId());
            node.setTitle(point.getDefinedTitle() != null ? point.getDefinedTitle() : point.getSecondaryTitle());
            node.setLevel(point.getLevel());
            node.setContent(point.getContent());
            node.setIsTemplate(point.getIsTemplate() == 1);
            node.setTemplateId(point.getTemplateId());
            node.setTeachingOrder(teachingOrderMap.getOrDefault(point.getTssId(), 0));
            node.setChildren(new ArrayList<>());
            node.setRelations(new ArrayList<>());
            
            nodeMap.put(point.getTssId(), node);
        }
        
        // 构建知识点关系
        List<KnowledgeEdgeVO> edges = new ArrayList<>();
        for (TopicRelation relation : relations) {
            KnowledgeEdgeVO edge = new KnowledgeEdgeVO();
            edge.setId(relation.getRelationId());
            edge.setFrom(relation.getParentTssId());
            edge.setTo(relation.getChildTssId());
            edge.setType(relation.getRelationType());
            edge.setIsTemplate(relation.getIsTemplate() == 1);
            edge.setTemplateRelationId(relation.getTemplateRelationId());
            
            edges.add(edge);
            
            // 构建父子关系
            if (relation.getRelationType() == 1 && 
                    nodeMap.containsKey(relation.getParentTssId()) && 
                    nodeMap.containsKey(relation.getChildTssId())) {
                nodeMap.get(relation.getParentTssId()).getChildren().add(nodeMap.get(relation.getChildTssId()));
            }
            
            // 构建关联关系
            if (relation.getRelationType() == 2 && 
                    nodeMap.containsKey(relation.getParentTssId()) && 
                    nodeMap.containsKey(relation.getChildTssId())) {
                nodeMap.get(relation.getParentTssId()).getRelations().add(relation.getChildTssId());
                nodeMap.get(relation.getChildTssId()).getRelations().add(relation.getParentTssId());
            }
        }
        
        // 构建知识点图谱
        KnowledgeGraphVO graph = new KnowledgeGraphVO();
        graph.setTsId(tsId);
        graph.setSubjectName(subject.getSubjectName());
        graph.setSchoolId(school.getSchoolId());
        graph.setSchoolName(school.getSchoolName());
        graph.setNodes(new ArrayList<>(nodeMap.values()));
        graph.setEdges(edges);
        
        return graph;
    }
    
    @Override
    public KnowledgeGraphVO getTemplateKnowledgeGraph(String topTitle) {
        // 获取模板知识点
        List<TopicTemplate> templates = topicTemplateMapper.findByTopTitle(topTitle);
        
        // 构建知识点节点
        Map<Integer, KnowledgeNodeVO> nodeMap = new HashMap<>();
        for (TopicTemplate template : templates) {
            KnowledgeNodeVO node = new KnowledgeNodeVO();
            node.setId(template.getTemplateId());
            node.setTitle(template.getSecondaryTitle());
            node.setLevel(template.getLevel());
            node.setContent(template.getContent());
            node.setIsTemplate(true);
            node.setTemplateId(template.getTemplateId());
            node.setChildren(new ArrayList<>());
            node.setRelations(new ArrayList<>());
            
            nodeMap.put(template.getTemplateId(), node);
        }
        
        // 构建知识点关系（这里简化处理，实际项目中应该有专门的模板关系表）
        List<KnowledgeEdgeVO> edges = new ArrayList<>();
        int edgeId = 1;
        
        // 假设同一主题下的知识点，层级相差1的为父子关系
        for (TopicTemplate parent : templates) {
            for (TopicTemplate child : templates) {
                if (parent.getLevel() + 1 == child.getLevel()) {
                    // 简单判断：二级标题包含一级标题的关键词，可能是父子关系
                    if (child.getSecondaryTitle().contains(parent.getSecondaryTitle()) || 
                            parent.getSecondaryTitle().contains(child.getSecondaryTitle())) {
                        KnowledgeEdgeVO edge = new KnowledgeEdgeVO();
                        edge.setId(edgeId++);
                        edge.setFrom(parent.getTemplateId());
                        edge.setTo(child.getTemplateId());
                        edge.setType(1); // 父子关系
                        edge.setIsTemplate(true);
                        
                        edges.add(edge);
                        
                        // 构建父子关系
                        if (nodeMap.containsKey(parent.getTemplateId()) && 
                                nodeMap.containsKey(child.getTemplateId())) {
                            nodeMap.get(parent.getTemplateId()).getChildren().add(nodeMap.get(child.getTemplateId()));
                        }
                    }
                }
            }
        }
        
        // 构建知识点图谱
        KnowledgeGraphVO graph = new KnowledgeGraphVO();
        graph.setSubjectName(topTitle);
        graph.setNodes(new ArrayList<>(nodeMap.values()));
        graph.setEdges(edges);
        
        return graph;
    }
    
    @Override
    public boolean updateKnowledgeGraph(KnowledgeGraphVO knowledgeGraphVO) {
        // 更新知识点
        for (KnowledgeNodeVO node : knowledgeGraphVO.getNodes()) {
            TopicPoint topicPoint = topicPointMapper.selectById(node.getId());
            if (topicPoint != null) {
                topicPoint.setDefinedTitle(node.getTitle());
                topicPoint.setContent(node.getContent());
                topicPoint.setLevel(node.getLevel());
                topicPointMapper.updateById(topicPoint);
            }
        }
        
        // 更新关系
        // 先删除旧关系
        topicRelationMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<TopicRelation>()
                .eq("ts_id", knowledgeGraphVO.getTsId()));
        
        // 添加新关系
        List<TopicRelation> relations = new ArrayList<>();
        for (KnowledgeEdgeVO edge : knowledgeGraphVO.getEdges()) {
            TopicRelation relation = new TopicRelation();
            relation.setParentTssId(edge.getFrom());
            relation.setChildTssId(edge.getTo());
            relation.setRelationType(edge.getType());
            relation.setTsId(knowledgeGraphVO.getTsId());
            relation.setSchoolId(knowledgeGraphVO.getSchoolId());
            relations.add(relation);
        }
        
        return topicRelationMapper.insertBatch(relations) > 0;
    }
}
