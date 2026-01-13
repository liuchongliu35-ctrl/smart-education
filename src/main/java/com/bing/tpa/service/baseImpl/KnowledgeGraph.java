package com.bing.tpa.service.baseImpl;

import com.bing.tpa.domain.dto.TopicPointLinkDto;
import com.bing.tpa.domain.dto.UnifiedKnowledgePoint;
import com.bing.tpa.domain.dto.UnifiedKnowledgeRelation;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class KnowledgeGraph {
    private List<UnifiedKnowledgePoint> nodes = new ArrayList<>();
//    private List<UnifiedKnowledgePoint> nodesWithTemplate = new ArrayList<>();
    private List<TopicPointLinkDto> link = new ArrayList<>();
    private final Map<Integer, UnifiedKnowledgePoint> nodeMap = new HashMap<>();
    private List<UnifiedKnowledgePoint> rootPoints = new ArrayList<>();
    private Integer pointsNum;

    public void addNode(UnifiedKnowledgePoint node) {
        if (!nodeMap.containsKey(node.getId())) {
            nodes.add(node);
            node.setChildren(null);
            //nodeMap.put(node.getId(), node); //TODO 暂时不使用这个map
        }
    }
//    public void addNodeWithTemplate(UnifiedKnowledgePoint node) {
//        if (!nodeMap.containsKey(node.getId())) {
//            nodesWithTemplate.add(node);
//        }
//    }

    // 获取根节点列表
    public List<UnifiedKnowledgePoint> getRootPoints() {
        return Collections.unmodifiableList(rootPoints);
    }
    // 添加根节点
    public void addRootPoint(UnifiedKnowledgePoint root) {
        rootPoints.add(root);
    }
    public void addLink(UnifiedKnowledgePoint from, UnifiedKnowledgePoint to,
                        UnifiedKnowledgeRelation relation) {
//        KnowledgeEdge edge = new KnowledgeEdge(from, to, relation);
        TopicPointLinkDto edge=new TopicPointLinkDto();
        edge.setSource(from.getId());
        edge.setTarget(to.getId());
        edge.setRelationId(relation.getId());
        edge.setRelationDesc(relation.getRelationDesc());
        link.add(edge);
    }

    public UnifiedKnowledgePoint getNodeById(Integer id) {
        return nodeMap.get(id);
    }

    // 获取以指定节点为中心的关系网
    public List<TopicPointLinkDto> getRelationsCenteredAt(Integer pointId) {
        return link.stream()
                .filter(e -> e.getSource().equals(pointId) || e.getTarget().equals(pointId))
                .collect(Collectors.toList());
    }

    // 内部边表示类
    @Data
    public static class KnowledgeEdge {
        private final UnifiedKnowledgePoint from;
        private final UnifiedKnowledgePoint to;
        private final UnifiedKnowledgeRelation relation;

        public KnowledgeEdge(UnifiedKnowledgePoint from, UnifiedKnowledgePoint to,
                             UnifiedKnowledgeRelation relation) {
            this.from = from;
            this.to = to;
            this.relation = relation;
        }
    }
}
