package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.KnowledgeSaveRequest;
import com.bing.tpa.domain.dto.TopicPointLinkDto;
import com.bing.tpa.service.baseImpl.KnowledgeGraph;
import com.bing.tpa.service.baseImpl.KnowledgeRelationService;
import com.bing.tpa.service.baseImpl.KnowledgeTemplateService;
import com.bing.tpa.service.baseImpl.SchoolKnowledgeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "知识点接口")
@CrossOrigin
@RestController
@RequestMapping("knowledge")
public class KnowledgeController <T> {
    @Autowired
    private KnowledgeRelationService knowledgeRelationService;

    @Autowired
    private KnowledgeTemplateService knowledgeTemplateService;

    @Autowired
    private SchoolKnowledgeService schoolKnowledgeService;
    @Autowired
    private Result<T> result;

    // 获取模板知识网
    @ApiOperation("获取模板知识网")
    @GetMapping("/template")
    public Result<Map<String, Object>> getTemplateKnowledgeGraph() {
        Map<String, Object> graph = knowledgeTemplateService.getTemplateKnowledgeGraph();
        return result.success(graph);
    }

    // 保存学校知识体系
    @ApiOperation("保存学校知识体系")
    @PostMapping("/save/{schoolId}/{tsId}")
    public Result<Void> saveSchoolKnowledge(@PathVariable Integer schoolId,
                                            @PathVariable Integer tsId,
                                            @RequestBody KnowledgeSaveRequest request) {
        schoolKnowledgeService.saveSchoolKnowledge(schoolId, tsId,
                request.getPoints(), request.getRelations());
        return result.success(null);
    }


    // 获取学校知识图谱
    @ApiOperation("获取学校知识图谱")
    @GetMapping("/graph/{schoolId}/{tsId}")
    public Result<KnowledgeGraph> getSchoolKnowledgeGraph(
            @PathVariable Integer schoolId, @PathVariable Integer tsId) {
        KnowledgeGraph graph = knowledgeRelationService.buildFullKnowledgeGraph(schoolId,tsId);
        if(graph.getNodes().isEmpty() && graph.getLink().isEmpty() && graph.getNodeMap().isEmpty())
            return result.build(null,"405","学校知识图谱为空");
        return result.build(graph, ResultCodeEnum.SUCCESS);
    }

    // 获取知识点的局部关系网
    @ApiOperation("获取知识点的局部关系网")
    @GetMapping("/relations/{schoolId}/{pointId}")
    public Result<List<TopicPointLinkDto>> getPointRelations(
            @PathVariable Integer schoolId,
            @PathVariable Integer pointId, @RequestParam Integer tsId) {
        List<TopicPointLinkDto> relations =
                knowledgeRelationService.getLocalRelationNetwork(schoolId, pointId,tsId);
        return result.build(relations, ResultCodeEnum.SUCCESS);
    }

    // 添加自定义关系
    @ApiOperation("添加自定义关系")
    @PostMapping("/relations")
    public Result<String> addRelation(
            @RequestParam Integer schoolId,
            @RequestParam Integer tsId,
            @RequestParam Integer parentId,
            @RequestParam Integer childId,
            @RequestParam Integer relationType,
            @RequestParam String relationDesc) {
        knowledgeRelationService.addCustomRelation(
                schoolId, tsId, parentId, childId, relationType, relationDesc);

        return result.build(null, ResultCodeEnum.SUCCESS);
    }

    // 迁移模板关系
    @ApiOperation("迁移模板关系")
    @PostMapping("/migrate-relation/{schoolId}/{relationId}")
    public Result<Void> migrateRelation(
            @PathVariable Integer schoolId,
            @PathVariable Integer relationId) {

        knowledgeRelationService.migrateTemplateRelation(schoolId, relationId);
        return result.build(null, ResultCodeEnum.SUCCESS);
    }
}

