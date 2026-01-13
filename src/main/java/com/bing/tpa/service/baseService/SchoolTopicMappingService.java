package com.bing.tpa.service.baseService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.domain.entity.SchoolTopicMapping;

import java.util.List;

public interface SchoolTopicMappingService extends IService<SchoolTopicMapping> {
    List<SchoolTopicMapping> getMappingsBySchoolId(Integer schoolId);
    
    List<SchoolTopicMapping> getMappingsByTemplateId(Integer templateId);
    
    SchoolTopicMapping getMappingBySchoolIdAndTemplateId(Integer schoolId, Integer templateId);
    
    void initializeSchoolMappings(Integer schoolId, String topTitle);
    
    boolean updateMappingStatus(Integer mappingId, Integer isUsed);
    
    boolean markAsCustomized(Integer mappingId, Integer tssId);
}
