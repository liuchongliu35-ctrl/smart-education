package com.bing.tpa.controller;


import com.bing.tpa.common.Result;
import com.bing.tpa.domain.entity.TopicTemplate;
import com.bing.tpa.service.baseService.TopicTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "模版知识点接口")
@RestController
@RequestMapping("/topic/template")
public class TopicTemplateController<T> {
    
    @Autowired
    private TopicTemplateService topicTemplateService;

    @Autowired
    private Result<T> result;
    
    @GetMapping
    @ApiOperation("获取所有模板")
    public Result<List<TopicTemplate>> getAllTemplates() {
        List<TopicTemplate> templates = topicTemplateService.getAllTemplates();
        return result.success(templates);
    }
    @ApiOperation("获取指定标题的模板")
    @GetMapping("/{topTitle}")
    public Result<List<TopicTemplate>> getTemplatesByTopTitle(@PathVariable String topTitle) {
        List<TopicTemplate> templates = topicTemplateService.getTemplatesByTopTitle(topTitle);
        return result.success(templates);
    }

    @ApiOperation("获取指定层级的知识点模板")
    @GetMapping("/level/{level}")
    public Result<List<TopicTemplate>> getTemplatesByLevel(@PathVariable Integer level) {
        List<TopicTemplate> templates = topicTemplateService.getTemplatesByLevel(level);
        return result.success(templates);
    }

    @ApiOperation("根据知识点id获取模板")
    @GetMapping("/detail/{templateId}")
    public Result<TopicTemplate> getTemplateById(@PathVariable Integer templateId) {
        TopicTemplate template = topicTemplateService.getTemplateById(templateId);
        return result.success(template);
    }
}
