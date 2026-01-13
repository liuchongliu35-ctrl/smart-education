package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import  com.bing.tpa.service.baseService.TopicRelationService;
import com.bing.tpa.domain.dto.TopicRelationDTO;
import com.bing.tpa.domain.entity.TopicRelation;
import com.bing.tpa.mapper.TopicRelationMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TopicRelationServiceImpl extends ServiceImpl<TopicRelationMapper, TopicRelation> implements TopicRelationService {
    
    @Autowired
    private TopicRelationMapper topicRelationMapper;
    
    @Override
    public TopicRelation createTopicRelation(TopicRelationDTO topicRelationDTO, Integer tsId, Integer schoolId) {
        TopicRelation topicRelation = new TopicRelation();
        BeanUtils.copyProperties(topicRelationDTO, topicRelation);
        topicRelation.setTsId(tsId);
        topicRelation.setSchoolId(schoolId);
        topicRelation.setCreateTime(LocalDateTime.now());
        
        topicRelationMapper.insert(topicRelation);
        return topicRelation;
    }
    
    @Override
    public boolean updateTopicRelation(TopicRelationDTO topicRelationDTO) {
        TopicRelation topicRelation = topicRelationMapper.selectById(topicRelationDTO.getRelationId());
        if (topicRelation == null) {
            return false;
        }
        
        BeanUtils.copyProperties(topicRelationDTO, topicRelation, "tsId", "schoolId", "isTemplate", "templateRelationId");
        
        return topicRelationMapper.updateById(topicRelation) > 0;
    }
    
    @Override
    public boolean deleteTopicRelation(Integer relationId) {
        return topicRelationMapper.deleteById(relationId) > 0;
    }
    
    @Override
    public List<TopicRelation> getRelationsByTsId(Integer tsId) {
        return topicRelationMapper.findByTsId(tsId);
    }
    
    @Override
    public List<TopicRelation> getRelationsByTsIdAndSchoolId(Integer tsId, Integer schoolId) {
        return topicRelationMapper.findByTsIdAndSchoolId(tsId, schoolId);
    }
    
    @Override
    public List<TopicRelation> getRelationsByParentTssId(Integer parentTssId) {
        return topicRelationMapper.findByParentTssId(parentTssId);
    }
    
    @Override
    public List<TopicRelation> getRelationsByChildTssId(Integer childTssId) {
        return topicRelationMapper.findByChildTssId(childTssId);
    }
}
