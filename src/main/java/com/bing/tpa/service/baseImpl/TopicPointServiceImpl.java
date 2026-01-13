package com.bing.tpa.service.baseImpl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.VO.PointLink;
import com.bing.tpa.domain.dto.TopicPointDTO;
import com.bing.tpa.domain.dto.TopicPointDto2;
import com.bing.tpa.domain.dto.UnifiedKnowledgePoint;
import com.bing.tpa.domain.dto.UnifiedKnowledgeRelation;
import com.bing.tpa.domain.entity.*;
import com.bing.tpa.exception.RepeatException;
import com.bing.tpa.mapper.*;
import com.bing.tpa.service.baseService.SchoolTopicMappingService;
import com.bing.tpa.service.baseService.TopicPointService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopicPointServiceImpl extends ServiceImpl<TopicPointMapper, TopicPoint> implements TopicPointService {

    @Autowired
    private TopicPointMapper topicPointMapper;

    @Autowired
    private TopicTemplateMapper topicTemplateMapper;

    @Autowired
    private SchoolTopicMappingMapper schoolTopicMappingMapper;

    @Autowired
    private SchoolTopicMappingService schoolTopicMappingService;

    @Autowired
    private TopicRelationMapper topicRelationMapper;

    @Autowired
    private TopicTemplateRelationMapper topicTemplateRelationMapper;


    // 获取学校的所有知识点（混合模板和自定义）
    @Override
    public List<UnifiedKnowledgePoint> getSchoolKnowledgePoints(Integer schoolId) {
        // 1. 查询学校的知识点映射
        List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolId(schoolId);

        List<UnifiedKnowledgePoint> knowledgePoints = new ArrayList<>();

        for (SchoolTopicMapping mapping : mappings) {
            if (mapping.getIsCustomized() == 1 && mapping.getTssId() != null) {
                // 自定义知识点
                TopicPoint customPoint = topicPointMapper.selectById(mapping.getTssId());
                if (customPoint != null) {
                    UnifiedKnowledgePoint unified = convertToUnifiedPoint(customPoint);
                    unified.setSource("custom");
                    knowledgePoints.add(unified);
                }
            } else if (mapping.getTemplateId() != null) {
                // 模板知识点
                TopicTemplate template = topicTemplateMapper.selectById(mapping.getTemplateId());
                if (template != null) {
                    UnifiedKnowledgePoint unified = convertToUnifiedPoint(template);
                    unified.setSource("template");
                    knowledgePoints.add(unified);
                }
            }
        }

        return knowledgePoints;
    }
    // 转换自定义知识点为统一格式
    private UnifiedKnowledgePoint convertToUnifiedPoint(TopicPoint point) {
        UnifiedKnowledgePoint unified = new UnifiedKnowledgePoint();
        unified.setId(point.getTssId());
        unified.setTopTitle(point.getTopTitle());
        unified.setSecondaryTitle(point.getDefinedTitle() != null ?
                point.getDefinedTitle() : point.getSecondaryTitle());
        unified.setContent(point.getContent());
        unified.setLevel(point.getLevel());
        unified.setSchoolId(point.getSchoolId());
        unified.setTsId(point.getTsId());
        unified.setTemplateId(point.getTemplateId());
        return unified;
    }

    // 转换模板知识点为统一格式
    private UnifiedKnowledgePoint convertToUnifiedPoint(TopicTemplate template) {
        UnifiedKnowledgePoint unified = new UnifiedKnowledgePoint();
        unified.setId(template.getTemplateId());
        unified.setTopTitle(template.getTopTitle());
        unified.setSecondaryTitle(template.getSecondaryTitle());
        unified.setContent(template.getContent());
        unified.setLevel(template.getLevel());
        unified.setTsId(template.getTsId());
        return unified;
    }

    // 获取学校知识点的所有关系
    @Override

    public List<UnifiedKnowledgeRelation> getSchoolKnowledgeRelations(Integer schoolId) {
        // 1. 获取学校所有知识点ID
        List<UnifiedKnowledgePoint> points = getSchoolKnowledgePoints(schoolId);
        List<Integer> allPointIds = points.stream()
                .map(UnifiedKnowledgePoint::getId)
                .collect(Collectors.toList());

        // 2. 查询模板关系
        List<TopicTemplateRelation> templateRelations =
                topicTemplateRelationMapper.findByPointIds(allPointIds);

        // 3. 查询自定义关系
        List<TopicRelation> customRelations =
                topicRelationMapper.findByPointIds(schoolId, allPointIds);

        // 4. 转换为统一格式
        List<UnifiedKnowledgeRelation> results = new ArrayList<>();

        for (TopicTemplateRelation tr : templateRelations) {
            UnifiedKnowledgeRelation ur = new UnifiedKnowledgeRelation();
            ur.setSource("template");
            ur.setParentId(Math.toIntExact(tr.getParentTemplateId()));
            ur.setChildId(Math.toIntExact(tr.getChildTemplateId()));
            ur.setRelationType(tr.getRelationType());
            ur.setRelationDesc(tr.getRelationDesc());
            results.add(ur);
        }

        for (TopicRelation cr : customRelations) {
            UnifiedKnowledgeRelation ur = new UnifiedKnowledgeRelation();
            ur.setSource("custom");
            ur.setParentId(cr.getParentTssId());
            ur.setChildId(cr.getChildTssId());
            ur.setRelationType(cr.getRelationType());
            ur.setSchoolId(cr.getSchoolId());
            ur.setTsId(cr.getTsId());
            ur.setTemplateRelationId(cr.getTemplateRelationId());
            results.add(ur);
        }

        return results;
    }


    // 构建学校知识图谱
    @Override
    public KnowledgeGraph buildSchoolKnowledgeGraph(Integer schoolId, Integer tsId) {

        KnowledgeGraph graph = new KnowledgeGraph();

        // 1. 获取知识点
        List<UnifiedKnowledgePoint> points = getSchoolKnowledgePoints(schoolId, tsId);
        Map<Integer, UnifiedKnowledgePoint> pointMap = new HashMap<>();
        for (UnifiedKnowledgePoint point : points) {
            // 初始化子节点列表
            point.setChildren(new ArrayList<>());
            point.setParents(new ArrayList<>());
            pointMap.put(point.getId(), point);
//            if(point.getTemplateId()==null){
            graph.addNode(point);
//            }else {
//                graph.addNodeWithTemplate(point);
//            }
////            新加的：
//            if (point.getTemplateId() != null) {
//                pointMap.put(point.getTemplateId(), point);
//            }
        }

        // 2. 获取关系
        List<UnifiedKnowledgeRelation> relations = getSchoolKnowledgeRelations(schoolId, tsId);

        // 3. 构建树形结构和知识图谱边
        List<UnifiedKnowledgePoint> rootPoints = new ArrayList<>();
        Set<Integer> childIds = new HashSet<>(); // 用于识别根节点

        // 处理所有关系
        for (UnifiedKnowledgeRelation rel : relations) {
//            UnifiedKnowledgePoint parent = pointMap.get(rel.getParentId());
//            UnifiedKnowledgePoint child = pointMap.get(rel.getChildId());
            // 查找实际的知识点对象
            UnifiedKnowledgePoint parent = findActualPoint(rel.getParentId(), pointMap);
            UnifiedKnowledgePoint child = findActualPoint(rel.getChildId(), pointMap);
            if (parent != null && child != null) {
                // 添加到图谱边
                graph.addLink(parent, child, rel);

                // 如果是层级关系，构建树形结构
                if (rel.getRelationType() == 1) {
                    // 添加子节点
                    parent.setChildren(new ArrayList<UnifiedKnowledgePoint>());
                    parent.getChildren().add(child);
                    // 添加父节点引用
//                    child.getParents().add(parent);

                    // 记录子节点ID
                    childIds.add(child.getId());
                }
            }
        }

        List<UnifiedKnowledgePoint> nodes = graph.getNodes();
        nodes.sort(Comparator.comparing(UnifiedKnowledgePoint::getId).thenComparing(UnifiedKnowledgePoint::getLevel));
        graph.setPointsNum(nodes.size());
//
//        graph.setRootPoints(rootPoints); TODO 暂时不用
////        将edge中完整的child添加到node的child中
        return graph;
    }

    // 获取学校知识点的所有关系
    private List<UnifiedKnowledgeRelation> getSchoolKnowledgeRelations(Integer schoolId, Integer tsId) {

        // 1. 获取学校所有知识点（包括模板和自定义）
        List<UnifiedKnowledgePoint> points = getSchoolKnowledgePoints(schoolId, tsId);

        // 2. 收集所有知识点ID（包括模板ID和自定义ID）
        Set<Integer> allPointIds = points.stream()
                .map(UnifiedKnowledgePoint::getId)
                .collect(Collectors.toSet());

        // 3. 获取所有模板ID
        Set<Integer> templateIds = points.stream()
                .filter(p -> "template".equals(p.getSource()))
                .map(UnifiedKnowledgePoint::getTemplateId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4. 合并所有ID（知识点ID + 模板ID）
        Set<Integer> allIds = new HashSet<>(allPointIds);
        allIds.addAll(templateIds);

        // 5. 查询所有关系（包括模板关系和自定义关系）
        List<TopicTemplateRelation> templateRelations =
                topicTemplateRelationMapper.findByPointIds(new ArrayList<>(allIds));

        List<TopicRelation> customRelations =
                topicRelationMapper.findByPointIds(schoolId, new ArrayList<>(allIds));

        // 6. 转换为统一格式并过滤禁用关系
        List<UnifiedKnowledgeRelation> results = new ArrayList<>();
        List<TopicRelation> disableRelations = topicRelationMapper.findByType(schoolId, tsId, 3);
        Set<String> disabledKeys = disableRelations.stream()
                .map(dr -> dr.getTemplateRelationId() + "-" + dr.getParentTssId() + "-" + dr.getChildTssId())
                .collect(Collectors.toSet());

        // 处理模板关系
        for (TopicTemplateRelation tr : templateRelations) {
            String key = tr.getTemplateRelationId() + "-" + tr.getParentTemplateId() + "-" + tr.getChildTemplateId();
            if (!disabledKeys.contains(key)) {
                results.add(convertToUnifiedRelation(tr));
            }
        }

        // 处理自定义关系（过滤掉禁用关系）
        for (TopicRelation cr : customRelations) {
//            if (cr.getRelationType() != 3) { // 跳过禁用关系
            results.add(convertToUnifiedRelation(cr));
//            }
        }

        return results;
    }
    private UnifiedKnowledgeRelation convertToUnifiedRelation(TopicTemplateRelation relation) {
        UnifiedKnowledgeRelation unified = new UnifiedKnowledgeRelation();
        if(unified.getTemplateRelationId()!=null){
            unified.setSource("template");
        }
        unified.setParentId(Math.toIntExact(relation.getParentTemplateId()));
        unified.setTemplateRelationId(Math.toIntExact(relation.getTemplateRelationId())); // 设置模板关系ID
        unified.setId(Math.toIntExact(relation.getTemplateRelationId())); // 新增：统一ID字段
        unified.setChildId(Math.toIntExact(relation.getChildTemplateId()));
        unified.setRelationType(relation.getRelationType());
        unified.setRelationDesc(relation.getRelationDesc());//添加关系描述！！！！
        return unified;
    }

    private UnifiedKnowledgeRelation convertToUnifiedRelation(TopicRelation relation) {
        UnifiedKnowledgeRelation unified = new UnifiedKnowledgeRelation();
        unified.setSource("custom");
        unified.setRelationId(relation.getRelationId()); // 设置自定义关系ID
        unified.setId(relation.getRelationId()); // 新增：统一ID字段
        unified.setParentId(relation.getParentTssId());
        unified.setChildId(relation.getChildTssId());
        unified.setRelationType(relation.getRelationType());
//        unified.setRelationDesc(relation.getRelationDesc());
        unified.setSchoolId(relation.getSchoolId());
        unified.setTsId(relation.getTsId());
        unified.setRelationDesc(relation.getRelationDesc());//添加关系描述！！！！
        unified.setTemplateRelationId(relation.getTemplateRelationId());
        return unified;
    }


    // 获取学校的所有知识点（混合模板和自定义）
    private List<UnifiedKnowledgePoint> getSchoolKnowledgePoints(Integer schoolId, Integer tsId) {
//        // 1. 查询学校的知识点映射
//        List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolId(schoolId);
//
//        List<UnifiedKnowledgePoint> knowledgePoints = new ArrayList<>();
//
//        for (SchoolTopicMapping mapping : mappings) {
//            if (mapping.getIsCustomized() == 1 && mapping.getTssId() != null) {
//                // 自定义知识点
//                TopicPoint customPoint = topicPointMapper.selectById(mapping.getTssId());
//                if (customPoint != null) {
//                    UnifiedKnowledgePoint unified = convertToUnifiedPoint(customPoint);
//                    unified.setSource("custom");
//                    knowledgePoints.add(unified);
//                }
//            } else if (mapping.getTemplateId() != null) {
//                // 模板知识点
//                TopicTemplate template = topicTemplateMapper.selectById(mapping.getTemplateId());
//                if (template != null) {
//                    UnifiedKnowledgePoint unified = convertToUnifiedPoint(template);
//                    unified.setSource("template");
//                    knowledgePoints.add(unified);
//                }
//            }
//        }
//        return knowledgePoints;

        List<UnifiedKnowledgePoint> knowledgePoints = new ArrayList<>();
        Set<Integer> processedIds = new HashSet<>();

        // 1. 查询当前课程的所有知识点
        List<TopicPoint> coursePoints = topicPointMapper.findBySchoolAndTs(schoolId, tsId);
        for (TopicPoint point : coursePoints) {
            if (!processedIds.contains(point.getTssId())) {
                UnifiedKnowledgePoint unified = convertToUnifiedPoint(point);

                // 判断知识点来源
                if (point.getTemplateId()!=null&&point.getTemplateId()>0) {
                    unified.setSource("template");
                } else {
                    unified.setSource("custom");
                }

                knowledgePoints.add(unified);
                processedIds.add(point.getTssId());
            }
        }

        // 2. 查询映射关系，确保包含所有模板知识点
        List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolId1(schoolId);
        for (SchoolTopicMapping mapping : mappings) {
            // 跳过已处理的知识点
            if (mapping.getTssId() != null && processedIds.contains(mapping.getTssId())) {
                continue;
            }

            // 处理模板知识点
            if (mapping.getTemplateId() != null && !processedIds.contains(mapping.getTemplateId())) {
                TopicTemplate template = topicTemplateMapper.selectById(mapping.getTemplateId());
                if (template != null) {
                    UnifiedKnowledgePoint unified = convertToUnifiedPoint(template);
                    unified.setSource("template");

                    // 创建或查找学校知识点ID
                    Integer schoolPointId = findOrCreateSchoolPointId(schoolId, tsId, mapping);
                    if (schoolPointId != null) {
                        unified.setId(schoolPointId);
                        knowledgePoints.add(unified);
                        processedIds.add(schoolPointId);
                    }
                }
            }
        }

        return knowledgePoints;
    }


    //     TODO 辅助方法
// 查找或创建学校知识点的代理ID
// 查找或创建学校知识点的代理ID
    private Integer findOrCreateSchoolPointId(Integer schoolId, Integer tsId, SchoolTopicMapping mapping) {
        // 1. 如果映射中已有tss_id，直接使用
        if (mapping.getTssId() != null) {
            return mapping.getTssId();
        }

        // 2. 尝试从知识点表中查找
        TopicPoint existing = topicPointMapper.findByTemplateId1(schoolId, tsId, mapping.getTemplateId());
        if (existing != null) {
            return existing.getTssId();
        }

        // 3. 创建新的代理知识点
        TopicTemplate template = topicTemplateMapper.selectById(mapping.getTemplateId());
        if (template == null) return null;

        TopicPoint proxy = new TopicPoint();
        proxy.setTsId(tsId);
        proxy.setSchoolId(schoolId);
        proxy.setTemplateId(template.getTemplateId());
        proxy.setTopTitle(template.getTopTitle());
        proxy.setSecondaryTitle(template.getSecondaryTitle());
        proxy.setLevel(template.getLevel());
        proxy.setIsTemplate(1);
        topicPointMapper.insert(proxy);

        // 更新映射记录
        mapping.setTssId(proxy.getTssId());
        schoolTopicMappingMapper.updateById(mapping);

        return proxy.getTssId();
    }

    // 辅助方法：通过ID查找实际的知识点对象
    private UnifiedKnowledgePoint findActualPoint(Integer id, Map<Integer, UnifiedKnowledgePoint> pointMap) {
        if (id == null) return null;

        // 直接查找
        if (pointMap.containsKey(id)) {
            return pointMap.get(id);
        }

        // 尝试查找ID对应的知识点
        for (UnifiedKnowledgePoint point : pointMap.values()) {
            if (point.getId() != null && point.getId().equals(id)) {
                return point;
            }
        }

        return null;
    }

    @Override
    public TopicPoint createTopicPoint(TopicPointDTO topicPointDTO, Integer tsId, Integer schoolId) {
        TopicPoint topicPoint = new TopicPoint();
        BeanUtils.copyProperties(topicPointDTO, topicPoint);
        topicPoint.setTsId(tsId);
        topicPoint.setSchoolId(schoolId);
        topicPoint.setCreateTime(LocalDateTime.now());
        topicPoint.setUpdateTime(LocalDateTime.now());

        if (topicPointDTO.getTemplateId() != null) {
            // 基于模板创建
            TopicTemplate template = topicTemplateMapper.selectById(topicPointDTO.getTemplateId());
            if (template != null) {
                topicPoint.setTopTitle(template.getTopTitle());
                topicPoint.setSecondaryTitle(template.getSecondaryTitle());
                topicPoint.setContent(template.getContent());
                topicPoint.setLevel(template.getLevel());
                topicPoint.setIsTemplate(0); // 非模板

                // 更新映射关系
                List<SchoolTopicMapping> mappingList = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(schoolId, topicPointDTO.getTemplateId());
                SchoolTopicMapping mapping = null;
                if(!mappingList.isEmpty()){
                    mapping=mappingList.get(0);
                }
                if (mapping != null) {
                    mapping.setIsCustomized(1);
                    mapping.setTssId(topicPoint.getTssId());
                    schoolTopicMappingMapper.updateById(mapping);
                }
            }
        } else {
            // 完全自定义知识点
            topicPoint.setIsTemplate(0);
        }

        topicPointMapper.insert(topicPoint);
        return topicPoint;
    }

    @Override
    public TopicPoint updateTopicPoint(TopicPointDTO topicPointDTO) {
        TopicPoint topicPoint = topicPointMapper.selectById(topicPointDTO.getTssId());
        if (topicPoint == null) {
            throw new RuntimeException("知识点不存在");
        }

        BeanUtils.copyProperties(topicPointDTO, topicPoint, "tsId", "schoolId", "templateId", "isTemplate");
        topicPoint.setUpdateTime(LocalDateTime.now());

        topicPointMapper.updateById(topicPoint);
        return topicPoint;
    }

    //     TODO 不使用该删除方法，使用下面的方案
    @Override
    public boolean deleteTopicPoint(Integer tssId) {
        TopicPoint topicPoint = topicPointMapper.selectById(tssId);
        if (topicPoint == null) {
            return false;
        }

        // 如果是基于模板的自定义知识点，只标记映射关系
        if (topicPoint.getTemplateId() != null) {
//            SchoolTopicMapping mapping = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(
//                    topicPoint.getSchoolId(), topicPoint.getTemplateId()).get(0);
            List<SchoolTopicMapping> mappingList = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(topicPoint.getSchoolId(),
                    topicPoint.getTemplateId());
            SchoolTopicMapping mapping = null;
            if(!mappingList.isEmpty()){
                mapping=mappingList.get(0);
            }
            if (mapping != null) {
                mapping.setIsCustomized(0);
                mapping.setTssId(null);
                schoolTopicMappingMapper.updateById(mapping);
            }
        }
        // 物理删除知识点
        return topicPointMapper.deleteById(tssId) > 0;
    }

    @Override
    public List<TopicPoint> getTopicPointsByTsId(Integer tsId) {
        return topicPointMapper.findByTsId(tsId);
    }

    @Override
    public List<TopicPoint> getTopicPointsByTemplateId(Integer templateId) {
        return topicPointMapper.findByTemplateId(templateId);
    }

    @Override
    public List<TopicPoint> getTopicPointsBySchoolId(Integer schoolId) {
        return topicPointMapper.findBySchoolId(schoolId);
    }

    @Override
    public TopicPoint getTopicPointById(Integer tssId) {
        return topicPointMapper.selectById(tssId);
    }

    @Override
    @Transactional
    public TopicPoint copyFromTemplate(Integer templateId, Integer tsId, Integer schoolId) {
        TopicTemplate template = topicTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        // 检查是否已存在自定义版本
        List<SchoolTopicMapping> mappingList = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(schoolId, templateId);
        SchoolTopicMapping mapping = null;
        if (mappingList != null && !mappingList.isEmpty()) {
            mapping = mappingList.get(0);
        }
        if (mapping != null && mapping.getIsCustomized() == 1 && mapping.getTssId() != null) {
            return topicPointMapper.selectById(mapping.getTssId());
        }

        // 创建新的知识点
        TopicPoint topicPoint = new TopicPoint();
        topicPoint.setTsId(tsId);
        topicPoint.setSchoolId(schoolId);
        topicPoint.setTemplateId(templateId);
        topicPoint.setTopTitle(template.getTopTitle());
        topicPoint.setSecondaryTitle(template.getSecondaryTitle());
        topicPoint.setContent(template.getContent());
        topicPoint.setLevel(template.getLevel());
        topicPoint.setIsTemplate(0);
        topicPoint.setCreateTime(LocalDateTime.now());
        topicPoint.setUpdateTime(LocalDateTime.now());

        topicPointMapper.insert(topicPoint);

        // 更新映射关系
        if (mapping != null) {
            mapping.setIsCustomized(1);
            mapping.setTssId(topicPoint.getTssId());
            schoolTopicMappingMapper.updateById(mapping);
        } else {
            // 创建新的映射关系
            mapping = new SchoolTopicMapping();
            mapping.setSchoolId(schoolId);
            mapping.setTemplateId(templateId);
            mapping.setTssId(topicPoint.getTssId());
            mapping.setIsUsed(1);
            mapping.setIsCustomized(1);
            schoolTopicMappingMapper.insert(mapping);
        }

        return topicPoint;
    }


    @Override
    public List<TopicPoint> getTopicTree(Integer tsId) {
        // 1. 获取课程下的所有知识点，并按层级排序
        List<TopicPoint> allTopics = topicPointMapper.findByTsId(tsId);

        if (allTopics.isEmpty()) {
            return Collections.emptyList();
        }

        // 按level字段排序
        allTopics.sort(Comparator.comparingInt(TopicPoint::getLevel));

        // 2. 构建树结构
        Map<Integer, TopicPoint> topicMap = new HashMap<>();
        List<TopicPoint> rootTopics = new ArrayList<>();

        for (TopicPoint topic : allTopics) {
            topicMap.put(topic.getTssId(), topic);

            // 假设level=1的是根节点
            if (topic.getLevel() == 1) {
                rootTopics.add(topic);
            } else {
                // 尝试找到父节点
                TopicPoint parent = findParentByLevel(topic, topicMap);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(topic);
                }
            }
        }

        return rootTopics;
    }

    private TopicPoint findParentByLevel(TopicPoint topic, Map<Integer, TopicPoint> topicMap) {
        // 简单实现：假设父节点的level是当前节点level-1
        int parentLevel = topic.getLevel() - 1;

        // 从后往前找，确保找到最近的父节点
        for (int i = topic.getTssId() - 1; i > 0; i--) {
            TopicPoint candidate = topicMap.get(i);
            if (candidate != null && candidate.getLevel() == parentLevel) {
                return candidate;
            }
        }

        return null;
    }

    @Override
    public boolean copyTopicPoints(Integer sourceTsId, Integer targetTsId, List<Integer> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return true;
        }

        // 1. 查询源知识点
        List<TopicPoint> sourceTopics = topicPointMapper.selectBatchIds(topicIds);
        if (sourceTopics.isEmpty()) {
            return false;
        }

        // 2. 验证所有知识点都属于源课程
        boolean allInSourceCourse = sourceTopics.stream()
                .allMatch(topic -> topic.getTsId().equals(sourceTsId));
        if (!allInSourceCourse) {
            throw new IllegalArgumentException("部分知识点不属于源课程");
        }

        // 3. 复制知识点到目标课程
        Map<Integer, Integer> oldIdToNewIdMap = new HashMap<>();

        for (TopicPoint sourceTopic : sourceTopics) {
            // 创建新的知识点对象
            TopicPoint newTopic = new TopicPoint();
            BeanUtils.copyProperties(sourceTopic, newTopic);

            // 修改关键属性
            newTopic.setTssId(null);  // 自增主键
            newTopic.setTsId(targetTsId);
            newTopic.setCreateTime(LocalDateTime.now());
            newTopic.setUpdateTime(LocalDateTime.now());

            // 插入新记录
            topicPointMapper.insert(newTopic);

            // 记录旧ID到新ID的映射
            oldIdToNewIdMap.put(sourceTopic.getTssId(), newTopic.getTssId());
        }

        // 4. 复制知识点关系
        copyTopicRelations(sourceTsId, targetTsId, oldIdToNewIdMap);

        // 5. 复制知识点映射关系（如果有）
        copyTopicMappings(sourceTsId, targetTsId, oldIdToNewIdMap);

        return true;
    }

    /**
     * 对一个节点进行修改
     * @param point
     * @return
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public TopicPoint modifyNode(TopicPoint point) {
//        先根据id获取原等于该id的知识点内容的所有id
        List<Integer> ids = topicPointMapper.selectIds1(point.getTssId());
        log.warn("ids:{}"+ids);
        point.setTssId(null);
        boolean update = lambdaUpdate()
                .set(!Objects.equals(point.getTopTitle(), "")&&point.getTopTitle()!=null, TopicPoint::getTopTitle, point.getTopTitle())
                .set(!Objects.equals(point.getSecondaryTitle(), "")&&point.getSecondaryTitle()!=null, TopicPoint::getSecondaryTitle, point.getSecondaryTitle())
                .set(!Objects.equals(point.getDefinedTitle(), "")&&point.getDefinedTitle()!=null, TopicPoint::getDefinedTitle, point.getDefinedTitle())
                .set(!Objects.equals(point.getContent(), "")&&point.getContent()!=null, TopicPoint::getContent, point.getContent())
                .in(TopicPoint::getTssId, ids)
                .update();
        if (!update) {
            return null;
        }
        return point;
    }

    /**
     * 获取低于2级的知识点
     * @param schoolId
     * @param tsId
     * @return
     */
    @Override
    public List<TopicPointDto2> getUnderTow(Integer schoolId, Integer tsId) {
        List<TopicPointDto2> pointDto2=new ArrayList<>();
        List<TopicPoint> pointList = topicPointMapper.selectUnderTow(schoolId, tsId);
        pointList.forEach(p->{
            TopicPointDto2 dto2 = new TopicPointDto2();
            dto2.setTssId(p.getTssId());
            if(p.getSecondaryTitle()!=null){
                dto2.setTitle(p.getSecondaryTitle()+"--"+p.getTopTitle());
            }else {
                dto2.setTitle(p.getTopTitle());
            }
            dto2.setLevel(p.getLevel());
            pointDto2.add(dto2);
        });
        pointDto2.sort(Comparator.comparing(TopicPointDto2::getLevel));
        return pointDto2;
    }

    /**
     * 删除一个知识点（从映射表和topic_points表中删除）
     * @param pointId
     * @param schoolId
     * @param tsId
     * @return
     */
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public boolean deleteKnowledgePoint(Integer pointId, Integer schoolId, Integer tsId) {
        // 1. 验证知识点是否存在且属于该学校/课程
        TopicPoint point = topicPointMapper.selectById(pointId);
        if (point == null) {
            throw new NullPointerException("知识点不存在: " + pointId);
        }
        if (!point.getSchoolId().equals(schoolId) || !point.getTsId().equals(tsId)) {
            throw new NullPointerException("知识点不属于当前学校/课程");
        }
        // 2. 删除知识点映射
        int deleteMapping = deleteMapping(pointId, schoolId);
        // 3. 删除相关关系（只删除涉及当前知识点的关系）
        deleteRelations(pointId, schoolId, tsId);// TODO 虽然不删掉子节点但是需要将该节点与其子节点的关系删除，不然后续无法根据和这个关系找到父节点就会报错
        // 4. 删除知识点本身
        int deletePoint = deletePoint(pointId);
        log.debug("删除知识点成功 | schoolId={}, tsId={}, pointId={}"+schoolId+tsId+pointId);
        return deleteMapping==1&&deletePoint==1;
    }

    /***
     * 获取结构化的知识点提示
     * @param schoolId
     * @param tsId
     * @return
     */
    @Override
    public List<PointLink> getPointsList(Integer schoolId, Integer tsId) {
//        检查该学校的知识点点最大为多少
        int maxLevel = topicPointMapper.getMaxLevel(schoolId, tsId);
        List<PointLink> pointLinks = new ArrayList<>();
//        先从二级节点开始找
        for(int i=2;i<=maxLevel;i++){
//            先找到level为i的节点
            List<TopicPoint> levelPoints = topicPointMapper.getLevelPoints(schoolId, tsId, i);
//            找到每一个i级节点的子节点构建List<PointLink>
            for(TopicPoint levelPoint:levelPoints){
                List<TopicPoint> children = topicPointMapper.getChildByParentId(levelPoint.getTssId());
                //  当遍历到倒数第二层前（包括倒数第二层），正常操作
                    if(!children.isEmpty()){
                        PointLink pointLink = getPointLink(levelPoint, children);
                        pointLinks.add(pointLink);
                    }
            }
        }
        return pointLinks;
    }

    @NotNull
    private static PointLink getPointLink(TopicPoint levelPoint, List<TopicPoint> children) {
        PointLink pointLink = new PointLink();
        pointLink.setPointId(levelPoint.getTssId());
        pointLink.setTitle(levelPoint.getTopTitle());
        pointLink.setParentSecondTitle(levelPoint.getSecondaryTitle());
        pointLink.setLevel(levelPoint.getLevel());
        pointLink.setChildren(new ArrayList<>());
        if(!children.isEmpty()){
            for (TopicPoint child: children){
                System.out.println(child.getRelationType());
                PointLink.ChildPoints childPoints = pointLink.new ChildPoints();
                childPoints.setPointId(child.getTssId());
                childPoints.setTitle(child.getTopTitle());
                childPoints.setChildSecondTitle(child.getSecondaryTitle());
                if(pointLink.getRelationType()==null){
                    pointLink.setRelationType(child.getRelationType());
                }

                if(Objects.equals(levelPoint.getLevel(), child.getLevel())){
                    childPoints.setLevel(child.getLevel()+1);//处理一下同等级但是有父子关系的知识点
                }else {
                    childPoints.setLevel(child.getLevel());
                }
                pointLink.getChildren().add(childPoints);
            }
        }
        return pointLink;
    }

    //     TODO 删除节点映射的辅助方法
    private int deleteMapping(Integer pointId, Integer schoolId) {
        // 构建查询条件
        QueryWrapper<SchoolTopicMapping> wrapper = new QueryWrapper<>();
        wrapper.eq("school_id", schoolId)
                .eq("tss_id", pointId);

        // 执行删除
        int delete = schoolTopicMappingMapper.delete(wrapper);
        log.debug("删除知识点映射 | pointId={}, schoolId={}"+pointId+schoolId);
        return delete;
    }

    //     TODO 删除节点相连的关系的辅助方法
    private void deleteRelations(Integer pointId, Integer schoolId, Integer tsId) {
        // 删除该知识点作为父节点的关系
        QueryWrapper<TopicRelation> parentWrapper = new QueryWrapper<>();
        parentWrapper.eq("parent_tss_id", pointId)
                .eq("school_id", schoolId)
                .eq("ts_id", tsId);
        topicRelationMapper.delete(parentWrapper);

        // 删除该知识点作为子节点的关系
        QueryWrapper<TopicRelation> childWrapper = new QueryWrapper<>();
        childWrapper.eq("child_tss_id", pointId)
                .eq("school_id", schoolId)
                .eq("ts_id", tsId);
        topicRelationMapper.delete(childWrapper);

        log.debug("删除相关关系 | pointId={}, schoolId={}, tsId={}"+pointId+schoolId+tsId);
    }


    //     TODO 删除节点知识点的辅助方法
    private int deletePoint(Integer pointId) {
        int byId = topicPointMapper.deleteById(pointId);
        log.debug("删除知识点 | pointId={}"+pointId);
        return byId;
    }



    /**
     * 保存给学校知识点网新增的局部知识点
     * @param schoolId
     * @param tsId
     * @param rootNode
     * ROLE_ADMIN
     */
    @Override
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")//todo 只有管理员可以进行修改
    public void saveKnowledge(Integer schoolId, Integer tsId, UnifiedKnowledgePoint rootNode,Integer parentId) throws RepeatException {
        Integer rootNodeId = saveNodeTree(schoolId, tsId, rootNode, null);
//        Integer newNodeId = saveOrUpdateNode(schoolId, tsId, rootNode.getChildren().get(0));
        /*TODO 特别处理一下该新增的局部知识点的上一级*/
        if(parentId!=null&&parentId>=1){
//            层级校验！！！
            validateLevelRelation(parentId, rootNode);
            saveRelation(parentId,rootNodeId ,schoolId,tsId);
        }
    }
    // todo 层级校验方法（子节点的level不得大于父节点的level）
    private void validateLevelRelation(Integer parentId, UnifiedKnowledgePoint childNode) {
        if (parentId == null) return;
//      寻找父节点
        TopicPoint parent = topicPointMapper.selectById(parentId);
        if (parent == null) {
            throw new RuntimeException("父节点不存在: " + parentId);
        }

        // 子节点层级必须大于父节点层级（level值更大）
        if (childNode.getLevel() <= parent.getLevel()) {
            throw new RuntimeException(
                    String.format("层级校验失败: 父节点[%s]层级=%d ≥ 子节点[%s]层级=%d",
                            parent.getSecondaryTitle(), parent.getLevel(),
                            childNode.getSecondaryTitle(), childNode.getLevel()
                    )
            );
        }
    }

    private Integer saveNodeTree(Integer schoolId,Integer tsId,UnifiedKnowledgePoint node,Integer parentId) throws RepeatException {
//        1.保存当前节点
        Integer nodeId = saveOrUpdateNode(schoolId, tsId, node);
        // 2. 如果存在父节点，保存关系
        if (parentId != null) {
//            todo 层级校验！！
            validateLevelRelation(parentId, node);
            saveRelation(parentId, nodeId, schoolId, tsId);
        }

        // 3. 递归保存子节点
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (UnifiedKnowledgePoint child : node.getChildren()) {
                saveNodeTree(schoolId, tsId, child, nodeId);
            }
        }

        return nodeId;
    }
    private Integer saveOrUpdateNode(Integer schoolId, Integer tsId,
                                     UnifiedKnowledgePoint node) throws RepeatException {
//      采取最顶端的重复就取消所有操作的措施,只要遍历到的节点中有重复存在数据库的就拒绝
//      检查即将插入的节点关键值是否为空
        if(node.getSource()==null||
                node.getSource().isEmpty()||
                node.getLevel()==null||
                Objects.equals(node.getTopTitle(), "") ||
                Objects.equals(node.getSecondaryTitle(), "")){
            throw new RuntimeException("新增节点失败，关键参数不能为空");
        }
        //            检查是否重复
        // 模板节点需要转换为自定义节点
        if ("template".equals(node.getSource())) {
            return convertTemplateToCustom(schoolId, tsId, node);
        }
        // 自定义节点处理
        TopicPoint point;
//        根据节点的两个字段：
        if ((node.getId() != null && node.getId() > 0)) {
            // 更新现有节点
            point = topicPointMapper.selectById(node.getId());
            if (point == null) {
                throw new RuntimeException("知识点不存在: " + node.getId());
            }
        } else {
            // 如果该节点是新节点(即没有id)，但是该节点已经出现在了数据库中，就不能进行插入
            boolean exists = lambdaQuery()
                    .eq(TopicPoint::getSchoolId, schoolId)//学校过滤
                    .eq(TopicPoint::getTsId, tsId)//学科过滤
                    .eq(node.getTopTitle()!=null,TopicPoint::getTopTitle, node.getTopTitle())
                    .eq(node.getSecondaryTitle() != null, TopicPoint::getSecondaryTitle, node.getSecondaryTitle())
                    .exists();
            if(exists) throw new RepeatException("节点重复");
            else {
                point = new TopicPoint();
                point.setCreateTime(LocalDateTime.now());
            }
        }

        // 设置/更新节点属性
        point.setTsId(tsId);
        point.setSchoolId(schoolId);
        point.setTopTitle(node.getTopTitle());
        point.setSecondaryTitle(node.getSecondaryTitle());
        point.setContent(node.getContent());
        point.setLevel(node.getLevel());
//        point.setTemplateId(node.getTemplateId());
        point.setIsTemplate(0); // 自定义节点
        point.setUpdateTime(LocalDateTime.now());

        if (point.getTssId() == null) {
            topicPointMapper.insert(point);
        } else {
            topicPointMapper.updateById(point);
        }
        // 新增：处理自定义知识点的映射关系
        if (point.getTssId() != null && node.getId() == null) {
            updateSchoolTopicMappingForCustom(schoolId, point.getTssId());
        }
        return point.getTssId();
    }

    /**
     * 为纯自定义知识点（无模板来源）更新学校-知识点映射
     */
    private void updateSchoolTopicMappingForCustom(Integer schoolId, Integer tssId) {
        // 检查是否已存在映射
        QueryWrapper<SchoolTopicMapping> wrapper = new QueryWrapper<>();
        wrapper.eq("school_id", schoolId)
                .eq("tss_id", tssId);
        SchoolTopicMapping mapping = schoolTopicMappingMapper.selectOne(wrapper);

        if (mapping == null) {
            // 创建新映射
            mapping = new SchoolTopicMapping();
            mapping.setSchoolId(schoolId);
            mapping.setTssId(tssId);
            mapping.setIsUsed(1);
            mapping.setIsCustomized(1);
            mapping.setCreateTime(LocalDateTime.now());
            mapping.setUpdateTime(LocalDateTime.now());
            schoolTopicMappingMapper.insert(mapping);
        } else {
            // 更新映射状态
            mapping.setIsUsed(1);
            mapping.setIsCustomized(1);
            mapping.setUpdateTime(LocalDateTime.now());
            schoolTopicMappingMapper.updateById(mapping);
        }
    }

    private Integer convertTemplateToCustom(Integer schoolId, Integer tsId,
                                            UnifiedKnowledgePoint templateNode) {
        // 检查是否已存在自定义版本
        Integer existingId = findExistingCustomNode(schoolId, templateNode.getTemplateId());
        if (existingId != null) {
            return existingId;
        }

        // 创建新的自定义节点
        TopicPoint customPoint = new TopicPoint();
        customPoint.setTsId(tsId);
        customPoint.setSchoolId(schoolId);
        customPoint.setTemplateId(templateNode.getTemplateId());
        customPoint.setTopTitle(templateNode.getTopTitle());
        customPoint.setSecondaryTitle(templateNode.getSecondaryTitle());
        customPoint.setContent(templateNode.getContent());
        customPoint.setLevel(templateNode.getLevel());
        customPoint.setIsTemplate(0); // 自定义节点
        customPoint.setCreateTime(LocalDateTime.now());
        customPoint.setUpdateTime(LocalDateTime.now());

        topicPointMapper.insert(customPoint);

        // 更新映射关系
        updateSchoolTopicMapping(schoolId, templateNode.getTemplateId(), customPoint.getTssId());

        return customPoint.getTssId();
    }

    /**
     * 查找已存在的自定义节点
     */
    private Integer findExistingCustomNode(Integer schoolId, Integer templateId) {
        List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(
                schoolId, templateId);

        if (mappings != null && !mappings.isEmpty()) {
            SchoolTopicMapping mapping = mappings.get(0);
            if (mapping.getIsCustomized() == 1 && mapping.getTssId() != null) {
                return mapping.getTssId();
            }
        }
        return null;
    }

    /**
     * 更新学校-知识点映射
     */
    private void updateSchoolTopicMapping(Integer schoolId, Integer templateId, Integer tssId) {
        List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(
                schoolId, templateId);

        SchoolTopicMapping mapping;
        if (mappings != null && !mappings.isEmpty()) {
            mapping = mappings.get(0);
        } else {
            mapping = new SchoolTopicMapping();
            mapping.setSchoolId(schoolId);
            mapping.setTemplateId(templateId);
            mapping.setIsUsed(1);
        }

        mapping.setTssId(tssId);
        mapping.setIsCustomized(1);
        mapping.setUpdateTime(LocalDateTime.now());

        if (mapping.getMappingId() == null) {
            mapping.setCreateTime(LocalDateTime.now());
            schoolTopicMappingMapper.insert(mapping);
        } else {
            schoolTopicMappingMapper.updateById(mapping);
        }
    }

//    TODO 保存新增节点之间的关系
    /**
     * 保存节点关系
     */
    private void saveRelation(Integer parentId, Integer childId,
                              Integer schoolId, Integer tsId) {
        // 检查关系是否已存在
        QueryWrapper<TopicRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_tss_id", parentId)
                .eq("child_tss_id", childId)
                .eq("relation_type", 1); // 层级关系

        TopicRelation existing = topicRelationMapper.selectOne(wrapper);
        if (existing != null) {
            return; // 关系已存在，无需重复保存
        }

        // 创建新关系
        TopicRelation relation = new TopicRelation();
        relation.setParentTssId(parentId);
        relation.setChildTssId(childId);
        relation.setRelationType(1); // 层级关系
        relation.setSchoolId(schoolId);
        relation.setIsTemplate(0);
        relation.setTsId(tsId);
        relation.setCreateTime(LocalDateTime.now());

        topicRelationMapper.insert(relation);
    }

    /**
     * 复制知识点关系
     */
    private void copyTopicRelations(Integer sourceTsId, Integer targetTsId,
                                    Map<Integer, Integer> idMap) {
        // 查询源课程的知识点关系
        List<TopicRelation> sourceRelations = topicRelationMapper.findByTsId(sourceTsId);

        for (TopicRelation sourceRelation : sourceRelations) {
            // 只处理需要复制的知识点之间的关系
            if (idMap.containsKey(sourceRelation.getParentTssId()) &&
                    idMap.containsKey(sourceRelation.getChildTssId())) {

                // 创建新的关系对象
                TopicRelation newRelation = new TopicRelation();
                BeanUtils.copyProperties(sourceRelation, newRelation);

                // 修改关键属性
                newRelation.setTsId(targetTsId);
                newRelation.setParentTssId(idMap.get(sourceRelation.getParentTssId()));
                newRelation.setChildTssId(idMap.get(sourceRelation.getChildTssId()));
                newRelation.setCreateTime(LocalDateTime.now());

                // 插入新关系
                topicRelationMapper.insert(newRelation);
            }
        }
    }

    /**
     * 复制知识点映射关系
     */
    private void copyTopicMappings(Integer sourceTsId, Integer targetTsId,
                                   Map<Integer, Integer> oldTssIdToNewTssIdMap) {
        // 查询源课程下的知识点（这里sourceTsId实际用于查询topic_points表）
        List<TopicPoint> sourceTopics = topicPointMapper.findByTsId(sourceTsId);

        // 提取知识点ID列表
        List<Integer> sourceTssIds = sourceTopics.stream()
                .map(TopicPoint::getTssId)
                .collect(Collectors.toList());

        // 查询这些知识点的映射关系（注意：这里使用school_id而非ts_id）
        List<SchoolTopicMapping> sourceMappings = schoolTopicMappingMapper.selectList(
                new QueryWrapper<SchoolTopicMapping>()
                        .in("tss_id", sourceTssIds)
        );

        for (SchoolTopicMapping sourceMapping : sourceMappings) {
            // 检查是否在需要复制的知识点范围内
            if (oldTssIdToNewTssIdMap.containsKey(sourceMapping.getTssId())) {
                SchoolTopicMapping newMapping = new SchoolTopicMapping();
                BeanUtils.copyProperties(sourceMapping, newMapping);

                // 重置主键
                newMapping.setMappingId(null);

                // 更新为新知识点ID
                newMapping.setTssId(
                        oldTssIdToNewTssIdMap.get(sourceMapping.getTssId())
                );

                // 保持其他属性不变（school_id、template_id等）
                newMapping.setCreateTime(LocalDateTime.now());
                newMapping.setUpdateTime(LocalDateTime.now());

                // 插入新映射
                schoolTopicMappingMapper.insert(newMapping);
            }
        }
    }
}



////    TODO 知识点完整性检查
//private void ensureNodeCompleteness(KnowledgeGraph graph,
//                                    Map<Integer, UnifiedKnowledgePoint> pointMap,
//                                    List<UnifiedKnowledgeRelation> relations) {
//    // 1. 收集所有节点ID
//    Set<Integer> allNodeIds = new HashSet<>(pointMap.keySet());
//
//    // 2. 检查根节点是否包含所有一级节点
//    List<UnifiedKnowledgePoint> rootPoints = graph.getRootPoints();
//    Set<Integer> includedIds = new HashSet<>();
//    for (UnifiedKnowledgePoint root : rootPoints) {
//        collectChildIds(root, includedIds);
//    }
//
//    // 3. 查找缺失的节点
//    Set<Integer> missingIds = new HashSet<>(allNodeIds);
//    missingIds.removeAll(includedIds);
//
//    // 4. 将缺失节点添加到根节点下
//    for (Integer missingId : missingIds) {
//        UnifiedKnowledgePoint missingNode = pointMap.get(missingId);
//
//        // 查找可能的父节点
//        UnifiedKnowledgePoint parent = findPotentialParent(missingNode, relations, pointMap);
//
//        if (parent != null) {
//            parent.getChildren().add(missingNode);
//            missingNode.getParents().add(parent);
//        } else {
//            // 作为根节点添加
//            rootPoints.add(missingNode);
//        }
//    }
//
//    // 5. 更新根节点列表
//    graph.setRootPoints(rootPoints);
//}
//
//    // 递归收集所有子节点ID
//    private void collectChildIds(UnifiedKnowledgePoint node, Set<Integer> idSet) {
//        idSet.add(node.getId());
//        if (node.getChildren() != null) {
//            for (UnifiedKnowledgePoint child : node.getChildren()) {
//                collectChildIds(child, idSet);
//            }
//        }
//    }
//
//    // 查找可能的父节点
//    private UnifiedKnowledgePoint findPotentialParent(UnifiedKnowledgePoint node,
//                                                      List<UnifiedKnowledgeRelation> relations,
//                                                      Map<Integer, UnifiedKnowledgePoint> pointMap) {
//        // 查找指向该节点的层级关系
//        for (UnifiedKnowledgeRelation rel : relations) {
//            if (rel.getChildId().equals(node.getId()) && rel.getRelationType() == 1) {
//                UnifiedKnowledgePoint parent = pointMap.get(rel.getParentId());
//                if (parent != null) {
//                    return parent;
//                }
//            }
//        }
//
//        // 根据层级自动查找
//        int expectedParentLevel = node.getLevel() - 1;
//        if (expectedParentLevel > 0) {
//            for (UnifiedKnowledgePoint candidate : pointMap.values()) {
//                if (candidate.getLevel() == expectedParentLevel) {
//                    return candidate;
//                }
//            }
//        }
//
//        return null;
//    }
//}


//        // 1. 获取知识点实体
//        List<UnifiedKnowledgePoint> points = getSchoolKnowledgePoints(schoolId);
//        Map<Integer, UnifiedKnowledgePoint> pointMap = new HashMap<>();
//        for (UnifiedKnowledgePoint point : points) {
//            pointMap.put(point.getId(), point);
//        }
//
//        // 2. 获取关系网络
//        List<UnifiedKnowledgeRelation> relations = getSchoolKnowledgeRelations(schoolId);
//
//        // 3. 构建图结构
//        KnowledgeGraph graph = new KnowledgeGraph();
//
//        // 添加节点
//        for (UnifiedKnowledgePoint point : points) {
//            graph.addNode(point);
//        }
//
//        // 添加边
//        for (UnifiedKnowledgeRelation relation : relations) {
//            UnifiedKnowledgePoint parent = pointMap.get(relation.getParentId());
//            UnifiedKnowledgePoint child = pointMap.get(relation.getChildId());
//
//            if (parent != null && child != null) {
//                graph.addEdge(parent, child, relation);
//            }
//        }
//
//        return graph;





// TODO 测试
//// 构建学校知识图谱
//    @Override
//    public KnowledgeGraph buildSchoolKnowledgeGraph(Integer schoolId, Integer tsId) {
//
//        KnowledgeGraph graph = new KnowledgeGraph();
//        Map<Integer, Integer> templateToCustomMap = new HashMap<>(); // 新增映射表
//
//        // 1. 获取知识点
//        List<UnifiedKnowledgePoint> points = getSchoolKnowledgePoints(schoolId, tsId);
//        Map<Integer, UnifiedKnowledgePoint> pointMap = new HashMap<>();
//
//        for (UnifiedKnowledgePoint point : points) {
//            point.setChildren(new ArrayList<>());
//            point.setParents(new ArrayList<>());
//            pointMap.put(point.getId(), point);
//
//            // 记录模板ID到自定义ID的映射
//            if (point.getTemplateId() != null && "custom".equals(point.getSource())) {
//                templateToCustomMap.put(point.getTemplateId(), point.getId());
//            }
//
//            if (point.getTemplateId() == null) {
//                graph.addNode(point);
//            } else {
//                graph.addNodeWithTemplate(point);
//            }
//        }
//
//        // 2. 获取关系
//        List<UnifiedKnowledgeRelation> relations = getSchoolKnowledgeRelations(schoolId, tsId);
//
//        // 3. 构建树形结构和知识图谱边
//        List<UnifiedKnowledgePoint> rootPoints = new ArrayList<>();
//        Set<Integer> childIds = new HashSet<>(); // 用于识别根节点
//
//        // 处理所有关系
//        for (UnifiedKnowledgeRelation rel : relations) {
//            // 查找实际的知识点对象
//            // 使用增强的findActualPoint方法
//            UnifiedKnowledgePoint parent = findActualPoint(rel.getParentId(), pointMap, templateToCustomMap);
//            UnifiedKnowledgePoint child = findActualPoint(rel.getChildId(), pointMap, templateToCustomMap);
//
//            if (parent != null && child != null) {
//                // 添加到图谱边
//                graph.addEdge(parent, child, rel);
//
//                // 如果是层级关系，构建树形结构
//                if (rel.getRelationType() == 1) {
//                    // 添加子节点
//                    parent.getChildren().add(child);
//                    // 添加父节点引用
/// /                    child.getParents().add(parent);
//                    // 记录子节点ID
//                    childIds.add(child.getId());
//                }
//            }
//        }
//
//        // 4. 找出根节点（没有父节点的节点）
//        for (UnifiedKnowledgePoint point : points) {
//            // 层级为1的节点通常是根节点
//            if (point.getLevel() == 1 && !childIds.contains(point.getId())) {
//                rootPoints.add(point);
//            }
//        }
//
//
//        List<UnifiedKnowledgePoint> nodes = graph.getNodes();
//        nodes.sort(Comparator.comparing(UnifiedKnowledgePoint::getId));
//        nodes.sort(Comparator.comparing(UnifiedKnowledgePoint::getLevel));//根据知识点等级排序
//        graph.setRootPoints(rootPoints);
//
////        将edge中完整的child添加到node的child中
////        List<KnowledgeGraph.KnowledgeEdge> edges = graph.getEdges();
////        for( UnifiedKnowledgePoint node : nodes){
////            if(node.getId()!=null&&node.getId()!=1){
////                //            在edge的from中找到templateId等于node的id的edge
////                for (KnowledgeGraph.KnowledgeEdge edge : edges) {
////                    if(edge.getFrom()!=null&&edge.getFrom().getTemplateId()!=null){
////                        if(edge.getFrom().getTemplateId()!=1&&Objects.equals(edge.getFrom().getTemplateId(), node.getId())){
////                            if(edge.getFrom().getChildren()!=null&&node.getChildren().isEmpty()){
////                                node.setChildren(edge.getFrom().getChildren());
////                            }
////                        }
////                    }
////                }
////            }
////        }
//        return graph;
//    }
//    // 辅助方法：通过ID查找实际的知识点对象
//    private UnifiedKnowledgePoint findActualPoint(Integer id, Map<Integer,
//            UnifiedKnowledgePoint> pointMap, Map<Integer, Integer> templateToCustomMap) {
//        if (id == null) return null;
//
//        // 1. 直接查找
//        if (pointMap.containsKey(id)) {
//            return pointMap.get(id);
//        }
//
//        // 2. 通过模板ID映射查找自定义点
//        if (templateToCustomMap.containsKey(id)) {
//            Integer customId = templateToCustomMap.get(id);
//            return pointMap.get(customId);
//        }
//
//        // 3. 尝试作为模板ID查找
//        for (UnifiedKnowledgePoint point : pointMap.values()) {
//            if (point.getTemplateId() != null &&
//                    point.getTemplateId().equals(id) &&
//                    "template".equals(point.getSource())) {
//                return point;
//            }
//        }
//
//        return null;
//    }
//
//    // 获取学校知识点的所有关系
//    private List<UnifiedKnowledgeRelation> getSchoolKnowledgeRelations(Integer schoolId, Integer tsId) {
////        // 1. 获取学校所有知识点ID
////        List<UnifiedKnowledgePoint> points = getSchoolKnowledgePoints(schoolId, tsId);
////
////        List<Integer> allPointIds = points.stream()
////                .map(UnifiedKnowledgePoint::getId)
////                .collect(Collectors.toList());
////
////        // 2. 查询模板关系
////        List<TopicTemplateRelation> templateRelations =
////                topicTemplateRelationMapper.findByPointIds(allPointIds);
////
////        // 3. 查询自定义关系
////        List<TopicRelation> customRelations =
////                topicRelationMapper.findByPointIds(schoolId, allPointIds);
////
////        // 4. 转换为统一格式并过滤禁用关系
////        List<UnifiedKnowledgeRelation> results = new ArrayList<>();
////
////        // 获取所有禁用关系
////        List<TopicRelation> disableRelations = topicRelationMapper.findByType(schoolId, tsId, 3);
////        Set<String> disabledKeys = disableRelations.stream()
////                .map(dr -> dr.getTemplateRelationId() + "-" + dr.getParentTssId() + "-" + dr.getChildTssId())
////                .collect(Collectors.toSet());
////
////        // 处理模板关系
////        for (TopicTemplateRelation tr : templateRelations) {
////            String key = tr.getTemplateRelationId() + "-" + tr.getParentTemplateId() + "-" + tr.getChildTemplateId();
////            if (!disabledKeys.contains(key)) {
////                results.add(convertToUnifiedRelation(tr));
////            }
////        }
////
////        // 处理自定义关系（过滤掉禁用关系）
////        for (TopicRelation cr : customRelations) {
////            if (cr.getRelationType() != 3) { // 跳过禁用关系
////                results.add(convertToUnifiedRelation(cr));
////            }
////        }
////
////        return results;
//
//
//        // 1. 获取学校所有知识点（包括模板和自定义）
//        List<UnifiedKnowledgePoint> points = getSchoolKnowledgePoints(schoolId, tsId);
//
//        // 2. 收集所有知识点ID（包括模板ID和自定义ID）
//        Set<Integer> allPointIds = points.stream()
//                .map(UnifiedKnowledgePoint::getId)
//                .collect(Collectors.toSet());
//
//        // 3. 获取所有模板ID
//        Set<Integer> templateIds = points.stream()
//                .filter(p -> "template".equals(p.getSource()))
//                .map(UnifiedKnowledgePoint::getTemplateId)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toSet());
//
//        // 4. 合并所有ID（知识点ID + 模板ID）
//        Set<Integer> allIds = new HashSet<>(allPointIds);
//        allIds.addAll(templateIds);
//
//        // 5. 查询所有关系（包括模板关系和自定义关系）
//        List<TopicTemplateRelation> templateRelations =
//                topicTemplateRelationMapper.findByPointIds(new ArrayList<>(allIds));
//
//        List<TopicRelation> customRelations =
//                topicRelationMapper.findByPointIds(schoolId, new ArrayList<>(allIds));
//
//        // 6. 转换为统一格式并过滤禁用关系
//        List<UnifiedKnowledgeRelation> results = new ArrayList<>();
//        List<TopicRelation> disableRelations = topicRelationMapper.findByType(schoolId, tsId, 3);
//        Set<String> disabledKeys = disableRelations.stream()
//                .map(dr -> dr.getTemplateRelationId() + "-" + dr.getParentTssId() + "-" + dr.getChildTssId())
//                .collect(Collectors.toSet());
//
//        // 处理模板关系
//        for (TopicTemplateRelation tr : templateRelations) {
//            String key = tr.getTemplateRelationId() + "-" + tr.getParentTemplateId() + "-" + tr.getChildTemplateId();
//            if (!disabledKeys.contains(key)) {
//                results.add(convertToUnifiedRelation(tr));
//            }
//        }
//
//        // 处理自定义关系（过滤掉禁用关系）
//        for (TopicRelation cr : customRelations) {
//            if (cr.getRelationType() != 3) { // 跳过禁用关系
//                results.add(convertToUnifiedRelation(cr));
//            }
//        }
//
//        return results;
//    }
//    private UnifiedKnowledgeRelation convertToUnifiedRelation(TopicTemplateRelation relation) {
//        UnifiedKnowledgeRelation unified = new UnifiedKnowledgeRelation();
//        unified.setSource("template");
//        unified.setParentId(Math.toIntExact(relation.getParentTemplateId()));
//        unified.setTemplateRelationId(Math.toIntExact(relation.getTemplateRelationId())); // 设置模板关系ID
//        unified.setId(Math.toIntExact(relation.getTemplateRelationId())); // 新增：统一ID字段
//        unified.setChildId(Math.toIntExact(relation.getChildTemplateId()));
//        unified.setRelationType(relation.getRelationType());
//        unified.setRelationDesc(relation.getRelationDesc());
//        return unified;
//    }
//
//    private UnifiedKnowledgeRelation convertToUnifiedRelation(TopicRelation relation) {
//        UnifiedKnowledgeRelation unified = new UnifiedKnowledgeRelation();
//        unified.setSource("custom");
//        unified.setRelationId(relation.getRelationId()); // 设置自定义关系ID
//        unified.setId(relation.getRelationId()); // 新增：统一ID字段
//        unified.setParentId(relation.getParentTssId());
//        unified.setChildId(relation.getChildTssId());
//        unified.setRelationType(relation.getRelationType());
////        unified.setRelationDesc(relation.getRelationDesc());
//        unified.setSchoolId(relation.getSchoolId());
//        unified.setTsId(relation.getTsId());
//        unified.setTemplateRelationId(relation.getTemplateRelationId());
//        return unified;
//    }
//
//
//    // 获取学校的所有知识点（混合模板和自定义）
//    private List<UnifiedKnowledgePoint> getSchoolKnowledgePoints(Integer schoolId, Integer tsId) {
////        // 1. 查询学校的知识点映射
////        List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolId(schoolId);
////
////        List<UnifiedKnowledgePoint> knowledgePoints = new ArrayList<>();
////
////        for (SchoolTopicMapping mapping : mappings) {
////            if (mapping.getIsCustomized() == 1 && mapping.getTssId() != null) {
////                // 自定义知识点
////                TopicPoint customPoint = topicPointMapper.selectById(mapping.getTssId());
////                if (customPoint != null) {
////                    UnifiedKnowledgePoint unified = convertToUnifiedPoint(customPoint);
////                    unified.setSource("custom");
////                    knowledgePoints.add(unified);
////                }
////            } else if (mapping.getTemplateId() != null) {
////                // 模板知识点
////                TopicTemplate template = topicTemplateMapper.selectById(mapping.getTemplateId());
////                if (template != null) {
////                    UnifiedKnowledgePoint unified = convertToUnifiedPoint(template);
////                    unified.setSource("template");
////                    knowledgePoints.add(unified);
////                }
////            }
////        }
////        return knowledgePoints;
//        // 1. 查询学校的知识点映射
//        List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolId(schoolId);
//
//        List<UnifiedKnowledgePoint> knowledgePoints = new ArrayList<>();
//        Set<Integer> processedIds = new HashSet<>(); // 避免重复
//        Map<Integer, Integer> templateToCustomMap = new HashMap<>(); // 新增：模板ID到自定义ID的映射
//
//        for (SchoolTopicMapping mapping : mappings) {
//            if (mapping.getIsCustomized() == 1 && mapping.getTssId() != null) {
//                // 自定义知识点
//                if (!processedIds.contains(mapping.getTssId())) {
//                    TopicPoint customPoint = topicPointMapper.selectById(mapping.getTssId());
//                    if (customPoint != null && customPoint.getTsId().equals(tsId)) {
//                        UnifiedKnowledgePoint unified = convertToUnifiedPoint(customPoint);
//                        unified.setTemplateId(mapping.getTemplateId());
//                        unified.setId(customPoint.getTssId()); // 关键：设置真实ID
//                        unified.setSource("custom");
//                        knowledgePoints.add(unified);
//                        processedIds.add(mapping.getTssId());
//
//                        // 添加到映射表
//                        if (mapping.getTemplateId() != null) {
//                            templateToCustomMap.put(mapping.getTemplateId(), mapping.getTssId());
//                        }
//                    }
//                }
//            } else if (mapping.getTemplateId() != null) {
//                // 模板知识点
//                if (!processedIds.contains(mapping.getTemplateId())) {
//                    TopicTemplate template = topicTemplateMapper.selectById(mapping.getTemplateId());
//                    if (template != null) {
//                        UnifiedKnowledgePoint unified = convertToUnifiedPoint(template);
//                        unified.setSource("template");
//                        knowledgePoints.add(unified);
//                        processedIds.add(mapping.getTemplateId());
//                    }
//                }
//            }
//        }
//        // 添加直接关联到课程的自定义知识点（没有映射记录的）
//        List<TopicPoint> directPoints = topicPointMapper.findBySchoolAndTs(schoolId, tsId);
//        for (TopicPoint point : directPoints) {
//            if (!processedIds.contains(point.getTssId())) {
//                UnifiedKnowledgePoint unified = convertToUnifiedPoint(point);
//                unified.setSource("custom");
//                unified.setId(point.getTssId()); // 关键：设置真实ID
//                knowledgePoints.add(unified);
//            }
//        }
//
//        return knowledgePoints;
//    }

//        // 1. 查询学校的知识点映射
//        List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolId(schoolId);
//
//        List<UnifiedKnowledgePoint> knowledgePoints = new ArrayList<>();
//        Set<Integer> processedIds = new HashSet<>(); // 避免重复
//
//        for (SchoolTopicMapping mapping : mappings) {
//            if (mapping.getIsCustomized() == 1 && mapping.getTssId() != null) {
//                // 自定义知识点
//                if (!processedIds.contains(mapping.getTssId())) {
//                    TopicPoint customPoint = topicPointMapper.selectById(mapping.getTssId());
//                    if (customPoint != null && customPoint.getTsId().equals(tsId)) {
//                        UnifiedKnowledgePoint unified = convertToUnifiedPoint(customPoint);
//                        unified.setSource("custom");
//                        knowledgePoints.add(unified);
//                        processedIds.add(mapping.getTssId());
//                    }
//                }
//            } else if (mapping.getTemplateId() != null) {
//                // 模板知识点
//                if (!processedIds.contains(mapping.getTemplateId())) {
//                    TopicTemplate template = topicTemplateMapper.selectById(mapping.getTemplateId());
//                    if (template != null) {
//                        UnifiedKnowledgePoint unified = convertToUnifiedPoint(template);
//                        unified.setSource("template");
//                        knowledgePoints.add(unified);
//                        processedIds.add(mapping.getTemplateId());
//                    }
//                }
//            }
//        }
//        // 添加直接关联到课程的自定义知识点（没有映射记录的）
//        List<TopicPoint> directPoints = topicPointMapper.findBySchoolAndTs(schoolId, tsId);
//        for (TopicPoint point : directPoints) {
//            if (!processedIds.contains(point.getTssId())) {
//                UnifiedKnowledgePoint unified = convertToUnifiedPoint(point);
//                unified.setSource("custom");
//                unified.setId(point.getTssId()); // 关键：设置真实ID
//                knowledgePoints.add(unified);
//            }
//        }
//
//        return knowledgePoints;