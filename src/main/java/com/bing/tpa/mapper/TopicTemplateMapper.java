package com.bing.tpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.TopicTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TopicTemplateMapper extends BaseMapper<TopicTemplate> {
    List<TopicTemplate> findByTopTitle(@Param("topTitle") String topTitle);
    
    List<TopicTemplate> findByLevel(@Param("level") Integer level);
}
