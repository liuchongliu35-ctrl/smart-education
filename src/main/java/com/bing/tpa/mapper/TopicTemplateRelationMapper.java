package com.bing.tpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.TopicTemplateRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TopicTemplateRelationMapper extends BaseMapper<TopicTemplateRelation> {

    List<TopicTemplateRelation> findByPointIds(@Param("pointIds") List<Integer> pointIds);

    List<TopicTemplateRelation> getAllTemplateRelations();

    @Select("SELECT * FROM topic_template_relations")
    List<TopicTemplateRelation> findAll();
}
