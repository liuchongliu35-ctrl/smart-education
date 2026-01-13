package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.entity.SchoolTopicMapping;
import com.bing.tpa.domain.entity.TopicTemplate;
import com.bing.tpa.mapper.SchoolTopicMappingMapper;
import com.bing.tpa.mapper.TopicTemplateMapper;
import com.bing.tpa.service.baseService.SchoolTopicMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SchoolTopicMappingServiceImpl extends ServiceImpl<SchoolTopicMappingMapper, SchoolTopicMapping> implements SchoolTopicMappingService {
    
    @Autowired
    private SchoolTopicMappingMapper schoolTopicMappingMapper;
    
    @Autowired
    private TopicTemplateMapper topicTemplateMapper;
    
    @Override
    public List<SchoolTopicMapping> getMappingsBySchoolId(Integer schoolId) {
        return schoolTopicMappingMapper.findBySchoolId(schoolId);
    }
    
    @Override
    public List<SchoolTopicMapping> getMappingsByTemplateId(Integer templateId) {
        return schoolTopicMappingMapper.findByTemplateId(templateId);
    }
    
    @Override
    public SchoolTopicMapping getMappingBySchoolIdAndTemplateId(Integer schoolId, Integer templateId) {
        List<SchoolTopicMapping> mappingList = schoolTopicMappingMapper.findBySchoolIdAndTemplateId(schoolId, templateId);
        SchoolTopicMapping mapping = null;
        if(!mappingList.isEmpty()){
            mapping=mappingList.get(0);
        }
        return mapping;
    }
    
    @Override
    @Transactional
    public void initializeSchoolMappings(Integer schoolId, String topTitle) {
        // 获取指定主题的所有模板
        List<TopicTemplate> templates = topicTemplateMapper.findByTopTitle(topTitle);
        
        // 为每个模板创建映射关系
        for (TopicTemplate template : templates) {
            SchoolTopicMapping mapping = new SchoolTopicMapping();
            mapping.setSchoolId(schoolId);
            mapping.setTemplateId(template.getTemplateId());
            mapping.setIsUsed(1); // 默认使用
            mapping.setIsCustomized(0); // 默认未自定义
            schoolTopicMappingMapper.insert(mapping);
        }
    }
    
    @Override
    public boolean updateMappingStatus(Integer mappingId, Integer isUsed) {
        SchoolTopicMapping mapping = schoolTopicMappingMapper.selectById(mappingId);
        if (mapping == null) {
            return false;
        }
        
        mapping.setIsUsed(isUsed);
        return schoolTopicMappingMapper.updateById(mapping) > 0;
    }
    
    @Override
    public boolean markAsCustomized(Integer mappingId, Integer tssId) {
        SchoolTopicMapping mapping = schoolTopicMappingMapper.selectById(mappingId);
        if (mapping == null) {
            return false;
        }
        
        mapping.setIsCustomized(1);
        mapping.setTssId(tssId);
        return schoolTopicMappingMapper.updateById(mapping) > 0;
    }
}
