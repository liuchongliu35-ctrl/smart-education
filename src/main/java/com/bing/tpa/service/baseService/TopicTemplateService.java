package com.bing.tpa.service.baseService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.domain.entity.TopicTemplate;

import java.util.List;

public interface TopicTemplateService extends IService<TopicTemplate> {
    List<TopicTemplate> getTemplatesByTopTitle(String topTitle);
    
    List<TopicTemplate> getTemplatesByLevel(Integer level);
    
    TopicTemplate getTemplateById(Integer templateId);
    
    List<TopicTemplate> getAllTemplates();
}
