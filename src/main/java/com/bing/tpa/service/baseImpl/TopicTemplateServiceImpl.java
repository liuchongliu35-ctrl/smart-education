package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.entity.TopicTemplate;
import com.bing.tpa.mapper.TopicTemplateMapper;
import com.bing.tpa.service.baseService.TopicTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicTemplateServiceImpl extends ServiceImpl<TopicTemplateMapper, TopicTemplate> implements TopicTemplateService {
    
    @Autowired
    private TopicTemplateMapper topicTemplateMapper;
    
    @Override
    public List<TopicTemplate> getTemplatesByTopTitle(String topTitle) {
        return topicTemplateMapper.findByTopTitle(topTitle);
    }
    
    @Override
    public List<TopicTemplate> getTemplatesByLevel(Integer level) {
        return topicTemplateMapper.findByLevel(level);
    }
    
    @Override
    public TopicTemplate getTemplateById(Integer templateId) {
        return topicTemplateMapper.selectById(templateId);
    }
    
    @Override
    public List<TopicTemplate> getAllTemplates() {
        return topicTemplateMapper.selectList(null);
    }
}
