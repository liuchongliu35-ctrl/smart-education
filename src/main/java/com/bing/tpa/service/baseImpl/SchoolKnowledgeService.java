package com.bing.tpa.service.baseImpl;

import com.bing.tpa.domain.dto.UnifiedKnowledgePoint;
import com.bing.tpa.domain.dto.UnifiedKnowledgeRelation;
import com.bing.tpa.domain.entity.*;
import com.bing.tpa.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class SchoolKnowledgeService {
    @Autowired
    private TopicPointMapper topicPointMapper;

    @Autowired
    private TopicRelationMapper topicRelationMapper;

    @Autowired
    private SchoolTopicMappingMapper schoolTopicMappingMapper;

    @Autowired
    private TopicTemplateMapper topicTemplateMapper;

    @Autowired
    private TopicTemplateRelationMapper topicTemplateRelationMapper;

    private static final Logger log = Logger.getLogger(SchoolKnowledgeService.class.getName());



//    TODO 根据ts_id和school_id查找是否有重复的知识点，有就不进行插入

    /**
     * 保存学校知识体系
     * @param schoolId 学校ID
     * @param tsId 课程ID
     * @param points 知识点列表（包括新增和修改的）
     * @param relations 关系列表（包括新增和修改的）
     */
    public void saveSchoolKnowledge(Integer schoolId, Integer tsId,
                                    List<UnifiedKnowledgePoint> points,
                                    List<UnifiedKnowledgeRelation> relations) {

         // 0. 获取现有数据快照
        Set<Integer> existingPointIds = getExistingPointIds(schoolId, tsId);
        Set<Integer> existingRelationIds = getExistingRelationIds(schoolId, tsId);

        // 1. 处理知识点 - 重构部分
        Map<Integer, UnifiedKnowledgePoint> distinctPoints = new HashMap<>();
        Map<Integer, Integer> oldToNewPointIdMap = new HashMap<>();
        Map<Integer, UnifiedKnowledgePoint> pointMap = new HashMap<>();


        // 新增：多级去重映射 (学校ID + 课程ID + 标题 + 层级)
        Map<String, Integer> titleLevelToIdMap = new HashMap<>();

        // ====== 新增：全局知识点去重映射 ======
        Map<String, Integer> globalPointKeyMap = new HashMap<>();
// 预加载已存在的知识点到全局映射
        List<TopicPoint> existingPoints = topicPointMapper.selectBySchoolAndTs(schoolId, tsId);

        for (TopicPoint p : existingPoints) {
            String key = buildGlobalKey(schoolId, tsId, p.getTopTitle(), p.getLevel());
            globalPointKeyMap.put(key, p.getTssId());
        }


        // 第一次遍历：收集并去重所有知识点
        for (UnifiedKnowledgePoint point : points) {
            pointMap.put(point.getId(), point);

            // 构建全局唯一键
            String globalKey = buildGlobalKey(schoolId, tsId, point.getTopTitle(), point.getLevel());

            // 检查是否已存在相同知识点
            if (globalPointKeyMap.containsKey(globalKey)) {
                // 复用已有知识点ID
                oldToNewPointIdMap.put(point.getId(), globalPointKeyMap.get(globalKey));
            } else {
                // 首次出现的知识点，添加到处理列表
                distinctPoints.put(point.getId(), point);
                // 临时使用前端ID，后续替换为真实ID
                globalPointKeyMap.put(globalKey, point.getId());
            }
        }


        // 1.2 第二次遍历：处理知识点
        for (UnifiedKnowledgePoint point : distinctPoints.values()) {
            // 构建全局唯一键
            String globalKey = buildGlobalKey(schoolId, tsId, point.getTopTitle(), point.getLevel());

            // 检查是否已存在数据库记录
            Integer existingId = findExistingPointByTitleLevel(schoolId, tsId, point.getTopTitle(), point.getLevel());

            // 知识点处理逻辑
            if (existingId != null) {
                // 复用已存在的知识点ID
                oldToNewPointIdMap.put(point.getId(), existingId);
                globalPointKeyMap.put(globalKey, existingId);
            } else {
                // 插入新知识点
                TopicPoint newPoint = convertToTopicPoint(point, schoolId, tsId);

                // 最终去重检查（防止并发插入相同知识点）
                Integer finalExistingId = topicPointMapper.findByAttributes(
                        schoolId, tsId, newPoint.getTopTitle(), newPoint.getLevel(), newPoint.getTemplateId());

                if (finalExistingId != null) {
                    // 使用已存在的知识点ID
                    oldToNewPointIdMap.put(point.getId(), finalExistingId);
                    globalPointKeyMap.put(globalKey, finalExistingId);
                } else {
                    topicPointMapper.insert(newPoint);
                    int newId = newPoint.getTssId();
                    oldToNewPointIdMap.put(point.getId(), newId);
                    globalPointKeyMap.put(globalKey, newId);
                    updateSchoolMapping(schoolId, point, newId);
                }
            }
        }

        // 2. 处理关系 - 重构部分
        Map<String, Set<Integer>> parentChildMap = new HashMap<>();
        Map<String,String> relationDescMap=new HashMap<>();
        // 2. 处理关系（使用全局映射转换ID）
        for (UnifiedKnowledgeRelation rel : relations) {
            // 转换父节点ID（使用全局映射）
            Integer parentId = convertIdWithGlobalMap(
                    rel.getParentId(),
                    oldToNewPointIdMap,
                    pointMap,
                    schoolId,
                    tsId,
                    globalPointKeyMap
            );

            // 转换子节点ID
            Integer childId = convertIdWithGlobalMap(
                    rel.getChildId(),
                    oldToNewPointIdMap,
                    pointMap,
                    schoolId,
                    tsId,
                    globalPointKeyMap
            );

            if (parentId == null || childId == null) {
                log.warning("Invalid relation: parent={}, child={}"+rel.getParentId()+rel.getChildId());
                continue;
            }
            relationDescMap.put(parentId+"-"+childId,rel.getRelationDesc());//储存这两个关系的描述
            // 对于层级关系（relationType=1），聚合子节点
            if (rel.getRelationType() == 1) {
                String parentKey = parentId.toString();
                if (!parentChildMap.containsKey(parentKey)) {
                    parentChildMap.put(parentKey, new HashSet<>());
                }
                parentChildMap.get(parentKey).add(childId);
            } else {
                // 非层级关系直接保存
                if ("custom".equals(rel.getSource())) {
                    saveCustomRelation(schoolId, tsId, rel, parentId, childId,relationDescMap.get(parentId+"-"+childId));
                } else if ("template".equals(rel.getSource())) {
                    handleTemplateRelation(schoolId, tsId, rel, parentId, childId, pointMap);
                }
            }
        }

        // 2.1 保存聚合后的层级关系
        for (Map.Entry<String, Set<Integer>> entry : parentChildMap.entrySet()) {
            Integer parentId = Integer.parseInt(entry.getKey());
            for (Integer childId : entry.getValue()) {
                saveLevelRelation(schoolId, tsId, parentId, childId,relationDescMap.get(parentId+"-"+childId));
            }
        }

        // 3. 删除标记为删除的知识点和关系
        deleteRemovedPoints(points, existingPointIds, schoolId, tsId);
        deleteRemovedRelations(relations, existingRelationIds, schoolId, tsId);
    }

    // ====== 新增辅助方法 ======
    private String buildGlobalKey(Integer schoolId, Integer tsId, String title, int level) {
        return schoolId + "-" + tsId + "-" + title + "-" + level;
    }

    // 统一ID转换方法（使用全局映射）
    private Integer convertIdWithGlobalMap(Integer pointId,
                                           Map<Integer, Integer> idMap,
                                           Map<Integer, UnifiedKnowledgePoint> pointMap,
                                           Integer schoolId,
                                           Integer tsId,
                                           Map<String, Integer> globalMap) {
        if (pointId == null) return null;

        // 1. 优先使用映射表（已处理的ID）
        if (idMap.containsKey(pointId)) {
            return idMap.get(pointId);
        }

        // 2. 检查全局映射
        UnifiedKnowledgePoint point = pointMap.get(pointId);
        if (point != null) {
            String globalKey = buildGlobalKey(schoolId, tsId, point.getTopTitle(), point.getLevel());
            if (globalMap.containsKey(globalKey)) {
                return globalMap.get(globalKey);
            }
        }

        // 3. 正常处理其他ID
        return convertNegativeIdWithGlobalMap(pointId, idMap, pointMap, schoolId, tsId, globalMap);
    }

    // 处理负数ID（使用全局映射）
    private Integer convertNegativeIdWithGlobalMap(Integer id,
                                                   Map<Integer, Integer> idMap,
                                                   Map<Integer, UnifiedKnowledgePoint> pointMap,
                                                   Integer schoolId,
                                                   Integer tsId,
                                                   Map<String, Integer> globalMap) {
        if (id == null) return null;

        if (id < 0) {
            if (idMap.containsKey(id)) {
                return idMap.get(id);
            }

            UnifiedKnowledgePoint point = pointMap.get(id);
            if (point != null && "custom".equals(point.getSource())) {
                // 构建全局键
                String globalKey = buildGlobalKey(schoolId, tsId, point.getTopTitle(), point.getLevel());

                // 检查全局映射
                if (globalMap.containsKey(globalKey)) {
                    int existingId = globalMap.get(globalKey);
                    idMap.put(id, existingId);
                    return existingId;
                }

                TopicPoint customPoint = convertToTopicPoint(point, schoolId, tsId);

                // 最终去重检查
                Integer existingId = topicPointMapper.findByAttributes(
                        schoolId, tsId, customPoint.getTopTitle(),
                        customPoint.getLevel(), customPoint.getTemplateId());

                if (existingId != null) {
                    idMap.put(id, existingId);
                    globalMap.put(globalKey, existingId);
                    return existingId;
                }

                topicPointMapper.insert(customPoint);
                int newId = customPoint.getTssId();
                idMap.put(id, newId);
                globalMap.put(globalKey, newId);
                updateSchoolMapping(schoolId, point, newId);
                return newId;
            }
        }

        return  resolveRealPointId(id, pointMap, schoolId, tsId, globalMap);
    }


    // 新增：检查知识点是否已存在（按标题和层级）
    private Integer findExistingPointByTitleLevel(Integer schoolId, Integer tsId,
                                                  String title, int level) {
        return topicPointMapper.findByTitleAndLevel(schoolId, tsId, title, level);
    }

    // 保存层级关系（一对多）
    private void saveLevelRelation(Integer schoolId, Integer tsId, Integer parentId, Integer childId,String relationDesc) {
        // 检查关系是否已存在
        if (topicRelationMapper.existsRelation(parentId, childId, 1, schoolId, tsId)>0) {
            return;
        }

        // 创建新关系
        TopicRelation relation = new TopicRelation();
        relation.setParentTssId(parentId);
        relation.setChildTssId(childId);
        relation.setRelationType(1);
        relation.setTsId(tsId);
        relation.setSchoolId(schoolId);
        relation.setIsTemplate(0);
        relation.setRelationDesc(relationDesc);
        topicRelationMapper.insert(relation);
    }

    // 保存自定义关系
    private void saveCustomRelation(Integer schoolId, Integer tsId,
                                    UnifiedKnowledgeRelation rel,
                                    Integer parentId, Integer childId, String relationDesc) {
        TopicRelation customRel = new TopicRelation();
        customRel.setRelationId(rel.getId() != null && rel.getId() > 1000000 ? rel.getId() : null);
        customRel.setParentTssId(parentId);
        customRel.setChildTssId(childId);
        customRel.setRelationType(rel.getRelationType());
        customRel.setTsId(tsId);
        customRel.setSchoolId(schoolId);
        customRel.setIsTemplate(0);
        customRel.setRelationDesc(relationDesc);
        if (customRel.getRelationId() == null) {
            topicRelationMapper.insert(customRel);
        } else {
            topicRelationMapper.updateById(customRel);
        }
    }

    // 处理模板关系
    private void handleTemplateRelation(Integer schoolId, Integer tsId,
                                        UnifiedKnowledgeRelation rel,
                                        Integer parentId, Integer childId,
                                        Map<Integer, UnifiedKnowledgePoint> pointMap) {
        if (isRelationModified(rel, pointMap)) {
            TopicRelation disableRel = new TopicRelation();
            disableRel.setParentTssId(parentId);
            disableRel.setChildTssId(childId);
            disableRel.setRelationType(3); // 禁用关系
            disableRel.setTsId(tsId);
            disableRel.setSchoolId(schoolId);
            disableRel.setIsTemplate(0);
            disableRel.setTemplateRelationId(rel.getTemplateRelationId());
            disableRel.setRelationDesc(rel.getRelationDesc());
            topicRelationMapper.insert(disableRel);
        }
    }


    // 构建知识点唯一标识
    private String buildUniqueKey(Integer schoolId, Integer tsId, UnifiedKnowledgePoint point) {
        return schoolId + "-" + tsId + "-" +
                (point.getTemplateId() != null ?
                        "tpl_" + point.getTemplateId() :
                        "cust_" + point.getTopTitle() + "_" + point.getLevel());
    }

    // 检查知识点是否已存在
    private Integer findExistingPointId(Integer schoolId, Integer tsId, UnifiedKnowledgePoint point) {
        // 优先通过模板ID查找
        if (point.getTemplateId() != null) {
            List<SchoolTopicMapping> mappings = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(schoolId, point.getTemplateId());
            if (!mappings.isEmpty()) {
                return mappings.get(0).getTssId();
            }
        }

        // 其次通过标题和层级查找
        return topicPointMapper.findByUniqueAttributes(
                schoolId,
                tsId,
                point.getTopTitle(),
                point.getLevel()
        );
    }


    // 解析真实知识点ID（处理混合来源）
    private Integer resolveRealPointId(Integer pointId,
                                       Map<Integer, UnifiedKnowledgePoint> pointMap,
                                       Integer schoolId,
                                       Integer tsId,
                                       Map<String, Integer> globalMap) {

        if (pointId == null) return null;
// 如果是负数ID，尝试从映射中获取真实ID
        if (pointId < 0) {
            UnifiedKnowledgePoint point = pointMap.get(pointId);
            if (point != null && "custom".equals(point.getSource())) {
                // 构建全局键
                String globalKey = buildGlobalKey(schoolId, tsId, point.getTopTitle(), point.getLevel());

                // 检查全局映射
                if (globalMap.containsKey(globalKey)) {
                    return globalMap.get(globalKey);
                }

                TopicPoint customPoint = convertToTopicPoint(point, schoolId, tsId);

                // 最终去重检查
                Integer existingId = topicPointMapper.findByAttributes(
                        schoolId, tsId, customPoint.getTopTitle(),
                        customPoint.getLevel(), customPoint.getTemplateId());

                if (existingId != null) {
                    globalMap.put(globalKey, existingId);
                    return existingId;
                }

                topicPointMapper.insert(customPoint);
                int newId = customPoint.getTssId();
                globalMap.put(globalKey, newId);
                updateSchoolMapping(schoolId, point, newId);
                return newId;
            }
            return null;
        }


        UnifiedKnowledgePoint point = pointMap.get(pointId);
        if (point == null) return null;
        if ("custom".equals(point.getSource())) {
            return point.getId(); // 自定义知识点的真实ID
        } else if ("template".equals(point.getSource())) {
            // 查找或创建模板映射
//            System.out.println("报错前");
            List<SchoolTopicMapping> mappingList = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(schoolId, point.getTemplateId());
            SchoolTopicMapping mapping = !mappingList.isEmpty() ? mappingList.get(0) : null;
//            System.out.println("报错后");
            if (mapping != null && mapping.getTssId() != null) {
                return mapping.getTssId();
            }
            // 创建模板知识点的代理记录
            return createTemplateProxy(point, schoolId, tsId);
        }
        return null;
    }

    // 创建模板知识点的代理记录
    private Integer createTemplateProxy(UnifiedKnowledgePoint templatePoint,
                                        Integer schoolId,
                                        Integer tsId) {
        TopicPoint proxy = new TopicPoint();
        proxy.setTsId(tsId);
        proxy.setSchoolId(schoolId);
        proxy.setTemplateId(templatePoint.getTemplateId());
        proxy.setTopTitle(templatePoint.getTopTitle());
        proxy.setSecondaryTitle(templatePoint.getSecondaryTitle());
        proxy.setLevel(templatePoint.getLevel());
        proxy.setIsTemplate(1); // 标记为模板代理
        topicPointMapper.insert(proxy);

        // 创建映射
        SchoolTopicMapping mapping = new SchoolTopicMapping();
        mapping.setSchoolId(schoolId);
        mapping.setTemplateId(templatePoint.getTemplateId());
        mapping.setTssId(proxy.getTssId());
        mapping.setIsUsed(1);
        mapping.setIsCustomized(0);
        schoolTopicMappingMapper.insert(mapping);

        return proxy.getTssId();
    }

    private Set<Integer> getExistingPointIds(Integer schoolId, Integer tsId) {
        return new HashSet<>(topicPointMapper.selectExistingPointIds(schoolId, tsId));
    }

    private Set<Integer> getExistingRelationIds(Integer schoolId, Integer tsId) {
        return new HashSet<>(topicRelationMapper.selectExistingRelationIds(schoolId, tsId));
    }
    // 删除被移除的知识点
    private void deleteRemovedPoints(List<UnifiedKnowledgePoint> currentPoints,
                                     Set<Integer> existingPointIds,
                                     Integer schoolId, Integer tsId) {
        // 获取当前有效ID集合：使用source和id属性
        Set<Integer> currentIds = currentPoints.stream()
                .filter(p -> "custom".equals(p.getSource())) // 只处理自定义知识点
                .map(UnifiedKnowledgePoint::getId)           // 使用id属性
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 计算需要删除的ID
        Set<Integer> toDelete = existingPointIds.stream()
                .filter(id -> !currentIds.contains(id))
                .collect(Collectors.toSet());

        // 执行删除（批量操作）
        if (!toDelete.isEmpty()) {
            topicPointMapper.batchDeleteByIds(new ArrayList<>(toDelete));
            // 级联删除相关关系
            topicRelationMapper.deleteByPointIds(new ArrayList<>(toDelete));
            // 清理映射表
            schoolTopicMappingMapper.deleteByTssIds(new ArrayList<>(toDelete));
        }
    }

    // 删除被移除的关系
    private void deleteRemovedRelations(List<UnifiedKnowledgeRelation> currentRelations,
                                        Set<Integer> existingRelationIds,
                                        Integer schoolId, Integer tsId) {
        // 获取当前有效关系ID（使用统一ID）
        Set<Integer> currentIds = currentRelations.stream()
                .map(UnifiedKnowledgeRelation::getId) // 使用统一ID
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 计算需要删除的关系ID
        Set<Integer> toDelete = existingRelationIds.stream()
                .filter(id -> !currentIds.contains(id))
                .collect(Collectors.toSet());

        // 执行删除（只删除自定义关系）
        if (!toDelete.isEmpty()) {
            topicRelationMapper.batchDeleteByIds(new ArrayList<>(toDelete));
        }
    }

    private TopicPoint convertToTopicPoint(UnifiedKnowledgePoint point, Integer schoolId, Integer tsId) {
        TopicPoint customPoint = new TopicPoint();
        // 修改ID检查逻辑：正数ID且大于1000000才视为已存在
        customPoint.setTssId((point.getId() != null && point.getId() > 1000000) ? point.getId() : null);
        customPoint.setTsId(tsId);
        customPoint.setSchoolId(schoolId);
        customPoint.setTemplateId(point.getTemplateId());
        customPoint.setTopTitle(point.getTopTitle());
        customPoint.setSecondaryTitle(point.getSecondaryTitle());
        customPoint.setDefinedTitle(point.getSecondaryTitle()); // 自定义标题
        customPoint.setContent(point.getContent());
        customPoint.setLevel(point.getLevel());
        customPoint.setIsTemplate(0); // 自定义
        return customPoint;
    }

    private TopicRelation convertToTopicRelation(UnifiedKnowledgeRelation rel,
                                                 Integer schoolId, Integer tsId,
                                                 Integer parentId, Integer childId) {
        TopicRelation customRel = new TopicRelation();
        customRel.setRelationId(rel.getSource().equals("custom") ? null : rel.getTemplateRelationId());
        customRel.setParentTssId(parentId);
        customRel.setChildTssId(childId);
        customRel.setRelationType(rel.getRelationType());
//        customRel.setRelationDesc(rel.getRelationDesc());
        customRel.setTsId(tsId);
        customRel.setSchoolId(schoolId);
        customRel.setIsTemplate(0); // 自定义
        customRel.setTemplateRelationId(rel.getTemplateRelationId()); // 关联的模板关系ID
        return customRel;
    }

    private void updateSchoolMapping(Integer schoolId, UnifiedKnowledgePoint point, Integer newId) {
        if (point.getTemplateId() != null) {
            // 基于模板的自定义
            List<SchoolTopicMapping> mappingList = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(schoolId, point.getTemplateId());
            SchoolTopicMapping mapping = null;
            if(!mappingList.isEmpty()){
                mapping=mappingList.get(0);
            }
            if (mapping != null) {
                mapping.setTssId(newId);
                mapping.setIsCustomized(1);
                schoolTopicMappingMapper.updateById(mapping);
            } else {
                mapping = new SchoolTopicMapping();
                mapping.setSchoolId(schoolId);
                mapping.setTemplateId(point.getTemplateId());
                mapping.setTssId(newId);
                mapping.setIsUsed(1);
                mapping.setIsCustomized(1);
                schoolTopicMappingMapper.insert(mapping);
            }
        } else {
            // 完全自定义的知识点
            SchoolTopicMapping mapping = new SchoolTopicMapping();
            mapping.setSchoolId(schoolId);
            mapping.setTssId(newId);
            mapping.setIsUsed(1);
            mapping.setIsCustomized(1);
//            template_id默认为null
            schoolTopicMappingMapper.insert(mapping);
        }
    }

    private void ensureTemplateMapping(Integer schoolId, Integer templateId) {
        List<SchoolTopicMapping> mappingList = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(schoolId, templateId);
        SchoolTopicMapping mapping = null;
        if(!mappingList.isEmpty()){
            mapping=mappingList.get(0);
        }
        if (mapping == null) {
            mapping = new SchoolTopicMapping();
            mapping.setSchoolId(schoolId);
            mapping.setTemplateId(templateId);
            mapping.setIsUsed(1);
            mapping.setIsCustomized(0); // 使用模板，未自定义
            schoolTopicMappingMapper.insert(mapping);
        }
    }

// 改进的关系修改检查
private boolean isRelationModified(UnifiedKnowledgeRelation rel,
                                   Map<Integer, UnifiedKnowledgePoint> pointMap) {
    UnifiedKnowledgePoint parent = pointMap.get(rel.getParentId());
    UnifiedKnowledgePoint child = pointMap.get(rel.getChildId());

    // 如果任意一端是自定义的，关系必须自定义
    if (parent != null && "custom".equals(parent.getSource())) return true;
    if (child != null && "custom".equals(child.getSource())) return true;

    // 检查关系属性是否修改
    TopicTemplateRelation original = getTemplateRelation(rel.getTemplateRelationId());
    if (original == null) return true;

    return !Objects.equals(original.getRelationType(), rel.getRelationType()) ||
            !Objects.equals(original.getRelationDesc(), rel.getRelationDesc());
}

    // 辅助方法：获取原始模板关系
    private TopicTemplateRelation getTemplateRelation(Integer templateRelationId) {
        if (templateRelationId == null) {
            return null;
        }

        // 使用MyBatis查询数据库
        return topicTemplateRelationMapper.selectById(templateRelationId);
    }
}


// TODO 原始代码
// // 1. 处理知识点
//        Map<String, UnifiedKnowledgePoint> uniquePointMap = new HashMap<>();//创建去重映射表
//        Map<Integer, Integer> oldToNewPointIdMap = new HashMap<>(); // 旧ID到新ID的映射
//        Map<Integer, UnifiedKnowledgePoint> pointMap = new HashMap<>();
//
//        // 2. 构建唯一标识映射 (学校ID + 课程ID + 模板ID/标题)
//        for (UnifiedKnowledgePoint point : points) {
//            String uniqueKey = buildUniqueKey(schoolId, tsId, point);
//            if (!uniquePointMap.containsKey(uniqueKey)) {
//                uniquePointMap.put(uniqueKey, point);
//                pointMap.put(point.getId(), point);
//            } else {
//                log.warning("Duplicate point detected: " + point.getTopTitle());
//            }
//        }
//        // 3. 只处理去重后的知识点
//        for (UnifiedKnowledgePoint point : uniquePointMap.values()) {
//            pointMap.put(point.getId(), point);
//
//            if ("custom".equals(point.getSource())) {
//                // 自定义知识点：新增或更新
//                TopicPoint customPoint = convertToTopicPoint(point, schoolId, tsId);
//                //新添point.getId() < 0 ||
//                if (point.getId() < 0 ||customPoint.getTssId() == null) {
//                    // 新增知识点
//                    topicPointMapper.insert(customPoint);
//                    oldToNewPointIdMap.put(point.getId(), customPoint.getTssId());
//                    updateSchoolMapping(schoolId, point, customPoint.getTssId());//新添
//                } else {
//                    // 更新知识点
//                    topicPointMapper.updateById(customPoint);
//                }
//
/// /                // 更新或创建映射关系
/// /                updateSchoolMapping(schoolId, point, customPoint.getTssId());
//            } else if ("template".equals(point.getSource())) {
//                // 模板知识点：确保映射存在
//                ensureTemplateMapping(schoolId, point.getId());
//            }
////            if ("custom".equals(point.getSource())) {
////                TopicPoint customPoint = convertToTopicPoint(point, schoolId, tsId);
//////                if(customPoint.getTemplateId()!=null){
//////                    TopicTemplate topicTemplate = topicTemplateMapper.selectById(customPoint.getTemplateId());
//////                    if(!Objects.equals(topicTemplate.getTopTitle(), customPoint.getTopTitle()) || !Objects.equals(topicTemplate.getContent(), customPoint.getContent())){
//////                        topicPointMapper.insert(customPoint);
//////                        points.remove(point);
//////                    }
//////                }
////                // 4. 新增检查：通过唯一标识检查是否已存在
////                Integer existingId = findExistingPointId(schoolId, tsId, point);
////                if (existingId != null) {
////                    // 更新已有记录
////                    customPoint.setTssId(existingId);
////                    topicPointMapper.updateById(customPoint);
////                    oldToNewPointIdMap.put(point.getId(), existingId);
////                } else if (point.getId() < 0 || customPoint.getTssId() == null) {
////                    // 插入新记录
////                    topicPointMapper.insert(customPoint);
////                    oldToNewPointIdMap.put(point.getId(), customPoint.getTssId());
////                    updateSchoolMapping(schoolId, point, customPoint.getTssId());
////                } else {
////                    // 更新已有记录
////                    topicPointMapper.updateById(customPoint);
////                }
////            } else if ("template".equals(point.getSource())) {
////                ensureTemplateMapping(schoolId, point.getTemplateId());
////            }
//        }
//
//        // 2. 处理关系
////        for (UnifiedKnowledgeRelation rel : relations) {
////            // 更新关系中的ID（如果知识点ID发生了变化）
////            Integer parentId = oldToNewPointIdMap.getOrDefault(rel.getParentId(), rel.getParentId());
////            Integer childId = oldToNewPointIdMap.getOrDefault(rel.getChildId(), rel.getChildId());
////
////            UnifiedKnowledgePoint parentPoint = pointMap.get(rel.getParentId());
////            UnifiedKnowledgePoint childPoint = pointMap.get(rel.getChildId());
////
////            if ("custom".equals(rel.getSource())) {
////                // 自定义关系：新增或更新
////                TopicRelation customRel = convertToTopicRelation(rel, schoolId, tsId, parentId, childId);
////
////                if (customRel.getRelationId() == null) {
////                    topicRelationMapper.insert(customRel);
////                } else {
////                    topicRelationMapper.updateById(customRel);
////                }
////            } else if ("template".equals(rel.getSource())) {
////                // 模板关系：如果被删除或修改过，则创建自定义禁用关系
////                if (isRelationModified(rel, parentPoint, childPoint)) {
////                    // 创建禁用关系
////                    TopicRelation disableRel = new TopicRelation();
////                    disableRel.setParentTssId(parentId);
////                    disableRel.setChildTssId(childId);
////                    disableRel.setRelationType(3); // 3表示禁用关系
////                    disableRel.setTsId(tsId);
////                    disableRel.setSchoolId(schoolId);
////                    disableRel.setIsTemplate(0);
////                    disableRel.setTemplateRelationId(rel.getTemplateRelationId());
////                    topicRelationMapper.insert(disableRel);
////                }
////            }
////        }
//        // 3. 处理关系 - 重构部分
//        for (UnifiedKnowledgeRelation rel : relations) {
//            // 首先处理负数ID的转换
//            Integer parentId = rel.getParentId();
//            Integer childId = rel.getChildId();
//            // 确定真实ID（可能是模板ID或自定义ID）
////            Integer realParentId = resolveRealPointId(rel.getParentId(), pointMap, schoolId, tsId);
////            Integer realChildId = resolveRealPointId(rel.getChildId(), pointMap, schoolId, tsId);
//            // 将负数ID转换为真实ID（如果存在映射）
//            Integer realParentId = convertNegativeId(parentId, oldToNewPointIdMap, pointMap, schoolId, tsId);
//            Integer realChildId = convertNegativeId(childId, oldToNewPointIdMap, pointMap, schoolId, tsId);
//
//            if (realParentId == null || realChildId == null) {
//                log.warning("Invalid relation: parent={}, child={}"+rel.getParentId()+rel.getChildId());
//                continue;
//            }
//
//            if ("custom".equals(rel.getSource())) {
//                // 自定义关系处理
//                TopicRelation customRel = new TopicRelation();
//                customRel.setRelationId(rel.getId() != null && rel.getId() > 1000000 ? rel.getId() : null);
//                customRel.setParentTssId(realParentId);
//                customRel.setChildTssId(realChildId);
//                customRel.setRelationType(rel.getRelationType());
//                customRel.setTsId(tsId);
//                customRel.setSchoolId(schoolId);
//                customRel.setIsTemplate(0);
//
//                if (customRel.getRelationId() == null) {
//                    topicRelationMapper.insert(customRel);
//                } else {
//                    topicRelationMapper.updateById(customRel);
//                }
//            } else if ("template".equals(rel.getSource())) {
//                // 模板关系处理
//                if (isRelationModified(rel, pointMap)) {
//                    TopicRelation disableRel = new TopicRelation();
//                    disableRel.setParentTssId(realParentId);
//                    disableRel.setChildTssId(realChildId);
//                    disableRel.setRelationType(3); // 禁用关系
//                    disableRel.setTsId(tsId);
//                    disableRel.setSchoolId(schoolId);
//                    disableRel.setIsTemplate(0);
//                    disableRel.setTemplateRelationId(rel.getTemplateRelationId());
//                    topicRelationMapper.insert(disableRel);
//                }
//            }
//        }
//
//        // 4. 删除标记为删除的知识点和关系
//        // 根据业务需求实现
//        deleteRemovedPoints(points, existingPointIds, schoolId, tsId);
//        deleteRemovedRelations(relations, existingRelationIds, schoolId, tsId);


// TODO 策略2
// // 0. 获取现有数据快照
//        Set<Integer> existingPointIds = getExistingPointIds(schoolId, tsId);
//        Set<Integer> existingRelationIds = getExistingRelationIds(schoolId, tsId);
//
//        // 1. 处理知识点 - 重构部分
//        Map<Integer, UnifiedKnowledgePoint> distinctPoints = new HashMap<>();
//        Map<Integer, Integer> oldToNewPointIdMap = new HashMap<>();
//        Map<Integer, UnifiedKnowledgePoint> pointMap = new HashMap<>();
//
//
//        // 新增：多级去重映射 (学校ID + 课程ID + 标题 + 层级)
//        Map<String, Integer> titleLevelToIdMap = new HashMap<>();
//
//        // 第一次遍历：收集并去重所有章节（包括顶级章节）
//        for (UnifiedKnowledgePoint point : points) {
//            pointMap.put(point.getId(), point);
//
//            // 创建唯一键：学校ID-课程ID-标题-层级
//            String uniqueKey = schoolId + "-" + tsId + "-" +
//                    point.getTopTitle() + "-" + point.getLevel();
//
//            // 只处理需要去重的层级（1=顶级章节，2=大章节）
//            if (point.getLevel() == 1 || point.getLevel() == 2) {
//                // 如果已存在相同章节，记录ID映射关系
//                if (titleLevelToIdMap.containsKey(uniqueKey)) {
//                    int existingId = titleLevelToIdMap.get(uniqueKey);
//                    oldToNewPointIdMap.put(point.getId(), existingId);
//                }
//                // 首次出现的章节，添加到去重映射
//                else {
//                    distinctPoints.put(point.getId(), point);
//                    // 临时使用当前ID，后续替换为真实ID
//                    titleLevelToIdMap.put(uniqueKey, point.getId());
//                }
//            }
//            // 小节直接添加到处理列表
//            else {
//                distinctPoints.put(point.getId(), point);
//            }
//        }
//
//
//        // 1.2 第二次遍历：处理知识点
//        for (UnifiedKnowledgePoint point : distinctPoints.values()) {
//            // 创建唯一键
//            String uniqueKey = schoolId + "-" + tsId + "-" +
//                    point.getTopTitle() + "-" + point.getLevel();
//
//            // 特殊处理需要去重的章节（层级1或2）
//            if (point.getLevel() == 1 || point.getLevel() == 2) {
//                // 检查是否已存在数据库记录
//                Integer existingId = findExistingPointByTitleLevel(
//                        schoolId, tsId, point.getTopTitle(), point.getLevel());
//
//                if (existingId != null) {
//                    // 复用已存在的章节ID
//                    oldToNewPointIdMap.put(point.getId(), existingId);
//                    titleLevelToIdMap.put(uniqueKey, existingId);
//                } else {
//                    // 插入新章节
//                    TopicPoint newPoint = convertToTopicPoint(point, schoolId, tsId);
//                    topicPointMapper.insert(newPoint);
//                    int newId = newPoint.getTssId();
//                    oldToNewPointIdMap.put(point.getId(), newId);
//                    titleLevelToIdMap.put(uniqueKey, newId);
//                    updateSchoolMapping(schoolId, point, newId);
//                }
//            }
//            else if ("custom".equals(point.getSource())) {
//                // 自定义知识点：新增或更新
//                TopicPoint customPoint = convertToTopicPoint(point, schoolId, tsId);
//
//                if (point.getId() < 0 || customPoint.getTssId() == null) {
//                    // 新增知识点
//                    topicPointMapper.insert(customPoint);
//                    oldToNewPointIdMap.put(point.getId(), customPoint.getTssId());
//
//                    updateSchoolMapping(schoolId, point, customPoint.getTssId());
//                } else {
//                    // 更新知识点
//                    topicPointMapper.updateById(customPoint);
//                    oldToNewPointIdMap.put(point.getId(), customPoint.getTssId());
//                }
//            } else if ("template".equals(point.getSource())) {
//                // 模板知识点：确保映射存在
//                ensureTemplateMapping(schoolId, point.getId());
//            }
//        }
//
//        // 2. 处理关系 - 重构部分
//        Map<String, Set<Integer>> parentChildMap = new HashMap<>(); // 新增：父节点到子节点集合的映射
//
//        // 2. 处理关系
//        for (UnifiedKnowledgeRelation rel : relations) {
//            // 转换父节点ID（使用统一映射）
//            Integer parentId = convertId(
//                    rel.getParentId(),
//                    oldToNewPointIdMap,
//                    pointMap,
//                    schoolId,
//                    tsId,
//                    titleLevelToIdMap
//            );
//
//            // 转换子节点ID
//            Integer childId = convertId(
//                    rel.getChildId(),
//                    oldToNewPointIdMap,
//                    pointMap,
//                    schoolId,
//                    tsId,
//                    titleLevelToIdMap
//            );
//
//            if (parentId == null || childId == null) {
//                log.warning("Invalid relation: parent={}, child={}"+rel.getParentId()+rel.getChildId());
//                continue;
//            }
//
//            // 对于层级关系（relationType=1），聚合子节点
//            if (rel.getRelationType() == 1) {
//                String parentKey = parentId.toString();
//                if (!parentChildMap.containsKey(parentKey)) {
//                    parentChildMap.put(parentKey, new HashSet<>());
//                }
//                parentChildMap.get(parentKey).add(childId);
//            } else {
//                // 非层级关系直接保存
//                if ("custom".equals(rel.getSource())) {
//                    saveCustomRelation(schoolId, tsId, rel, parentId, childId);
//                } else if ("template".equals(rel.getSource())) {
//                    handleTemplateRelation(schoolId, tsId, rel, parentId, childId, pointMap);
//                }
//            }
//        }
//
//        // 2.1 保存聚合后的层级关系
//        for (Map.Entry<String, Set<Integer>> entry : parentChildMap.entrySet()) {
//            Integer parentId = Integer.parseInt(entry.getKey());
//            for (Integer childId : entry.getValue()) {
//                saveLevelRelation(schoolId, tsId, parentId, childId);
//            }
//        }
//
//        // 3. 删除标记为删除的知识点和关系
//        deleteRemovedPoints(points, existingPointIds, schoolId, tsId);
//        deleteRemovedRelations(relations, existingRelationIds, schoolId, tsId);