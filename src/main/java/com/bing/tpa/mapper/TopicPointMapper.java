package com.bing.tpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.TopicPoint;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface TopicPointMapper extends BaseMapper<TopicPoint> {
    List<TopicPoint> findByTsId(@Param("tsId") Integer tsId);
    
    List<TopicPoint> findByTemplateId(@Param("templateId") Integer templateId);
    // 按模板ID查询知识点
    TopicPoint findByTemplateId1(
            @Param("schoolId") Integer schoolId,
            @Param("tsId") Integer tsId,
            @Param("templateId") Integer templateId);
    
    List<TopicPoint> findBySchoolId(@Param("schoolId") Integer schoolId);
    
    List<TopicPoint> findByTsIdAndSchoolId(@Param("tsId") Integer tsId, @Param("schoolId") Integer schoolId);

    List<TopicPoint> findByLevel(@Param("tsId") Integer tsId,
                                 @Param("schoolId") Integer schoolId,
                                 @Param("level") Integer level);

    /**
     * 获取学校课程下所有自定义知识点的ID
     */
    @Select("SELECT tss_id FROM topic_points " +
            "WHERE school_id = #{schoolId} AND ts_id = #{tsId}")
    Set<Integer> selectExistingPointIds(@Param("schoolId") Integer schoolId,
                                        @Param("tsId") Integer tsId);

    int batchDeleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据知识点ID删除映射关系
     */
    @Delete("DELETE FROM school_topic_mapping WHERE tss_id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    int deleteMappingByTssIds(@Param("ids") List<Integer> ids);

    @Select("SELECT * FROM topic_points WHERE school_id = #{schoolId} AND ts_id = #{tsId}")
    List<TopicPoint> findBySchoolAndTs(
            @Param("schoolId") Integer schoolId,
            @Param("tsId") Integer tsId);

    @Select("SELECT tss_id FROM topic_points " +
            "WHERE school_id = #{schoolId} AND ts_id = #{tsId} " +
            "AND top_title = #{title} AND level = #{level} " +
            "ORDER BY tss_id DESC LIMIT 1")
    Integer findByUniqueAttributes(
            @Param("schoolId") Integer schoolId,
            @Param("tsId") Integer tsId,
            @Param("title") String title,
            @Param("level") Integer level
    );

    Integer findChapterByTitle(
            @Param("schoolId") Integer schoolId,
            @Param("tsId") Integer tsId,
            @Param("title") String title,
            @Param("level") int level
    );

    Integer findByTitleAndLevel(
            @Param("schoolId") Integer schoolId,
            @Param("tsId") Integer tsId,
            @Param("title") String title,
            @Param("level") int level
    );


    @Select("SELECT tss_id FROM topic_points " +
            "WHERE school_id = #{schoolId} AND ts_id = #{tsId} " +
            "AND top_title = #{title} LIMIT 1")
    Integer findByTitle(
            @Param("schoolId") Integer schoolId,
            @Param("tsId") Integer tsId,
            @Param("title") String title);

    List<Integer> selectIds1(@Param("tssId") Integer tssId);

    List<TopicPoint> selectUnderTow(@Param("schoolId") Integer schoolId, @Param("tsId") Integer tsId);

    // 查询学校课程下的所有知识点
    @Select("SELECT * FROM topic_points WHERE school_id = #{schoolId} AND ts_id = #{tsId}")
    List<TopicPoint> selectBySchoolAndTs(
            @Param("schoolId") Integer schoolId,
            @Param("tsId") Integer tsId
    );

    // 根据属性查找知识点
    @Select("SELECT tss_id FROM topic_points " +
            "WHERE school_id = #{schoolId} " +
            "AND ts_id = #{tsId} " +
            "AND top_title = #{title} " +
            "AND level = #{level} " +
            "AND (template_id = #{templateId} OR (template_id IS NULL AND #{templateId} IS NULL))")
    Integer findByAttributes(
            @Param("schoolId") Integer schoolId,
            @Param("tsId") Integer tsId,
            @Param("title") String title,
            @Param("level") Integer level,
            @Param("templateId") Integer templateId
    );

    int getMaxLevel(@Param("schoolId") Integer schoolId, @Param("tsId") Integer tsId);

    List<TopicPoint> getLevelPoints(@Param("schoolId") Integer schoolId, @Param("tsId")Integer tsId, @Param("level") int level);

    List<TopicPoint> getChildByParentId(@Param("tssId") Integer tssId);
}
