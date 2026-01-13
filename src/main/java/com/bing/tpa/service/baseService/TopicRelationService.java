package com.bing.tpa.service.baseService;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.domain.dto.TopicRelationDTO;
import com.bing.tpa.domain.entity.TopicRelation;

import java.util.List;

public interface TopicRelationService extends IService<TopicRelation> {
    TopicRelation createTopicRelation(TopicRelationDTO topicRelationDTO, Integer tsId, Integer schoolId);
    
    boolean updateTopicRelation(TopicRelationDTO topicRelationDTO);
    
    boolean deleteTopicRelation(Integer relationId);
    
    List<TopicRelation> getRelationsByTsId(Integer tsId);
    
    List<TopicRelation> getRelationsByTsIdAndSchoolId(Integer tsId, Integer schoolId);
    
    List<TopicRelation> getRelationsByParentTssId(Integer parentTssId);
    
    List<TopicRelation> getRelationsByChildTssId(Integer childTssId);
}
