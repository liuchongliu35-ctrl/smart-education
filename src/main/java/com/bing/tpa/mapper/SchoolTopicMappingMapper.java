package com.bing.tpa.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.SchoolTopicMapping;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface SchoolTopicMappingMapper extends BaseMapper<SchoolTopicMapping> {
//    List<SchoolTopicMapping> findBySchoolId(@Param("schoolId") Integer schoolId);
    
    List<SchoolTopicMapping> findByTemplateId(@Param("templateId") Integer templateId);
    
    List<SchoolTopicMapping> findBySchoolIdAndTemplateId(@Param("schoolId") Integer schoolId, @Param("templateId") Integer templateId);

    // 新增方法（与XML中的id对应）
    List<SchoolTopicMapping> findUsedTemplatesBySchoolId(@Param("schoolId") Integer schoolId);
    List<SchoolTopicMapping> findCustomizedTemplatesBySchoolId(@Param("schoolId") Integer schoolId);

//    @Select("SELECT * FROM school_topic_mapping WHERE school_id = #{schoolId} AND is_used = 1")
    List<SchoolTopicMapping> findBySchoolId(@Param("schoolId") Integer schoolId);

    /**
     * 根据知识点ID删除映射
     */
    @Delete("DELETE FROM school_topic_mapping WHERE tss_id in" +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int deleteByTssIds(@Param("ids") List<Integer> ids);

    Set<Integer> findDisabledTemplateRelations(Integer schoolId, Integer tsId);

    List<SchoolTopicMapping> findBySchoolIdAndTssId(
            @Param("schoolId") Integer schoolId,
            @Param("tssId") Integer tssId);

    List<SchoolTopicMapping> findBySchoolId1(@Param("schoolId") Integer schoolId);
}
