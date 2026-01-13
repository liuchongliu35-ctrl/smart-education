package com.bing.tpa.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.TopicRelation;
import com.bing.tpa.service.baseImpl.SchoolKnowledgeService;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface TopicRelationMapper extends BaseMapper<TopicRelation> {
    List<TopicRelation> findByTsId(@Param("tsId") Integer tsId);
    
    List<TopicRelation> findByTsIdAndSchoolId(@Param("tsId") Integer tsId, @Param("schoolId") Integer schoolId);
    
    List<TopicRelation> findByParentTssId(@Param("parentTssId") Integer parentTssId);
    
    List<TopicRelation> findByChildTssId(@Param("childTssId") Integer childTssId);
    int insertBatch(List<TopicRelation> relations);


    List<TopicRelation> findByPointIds(@Param("schoolId") Integer schoolId,
                                       @Param("pointIds") List<Integer> pointIds);

    @Select("SELECT relation_id FROM topic_relations " +
            "WHERE school_id = #{schoolId} AND ts_id = #{tsId}")
    Set<Integer> selectExistingRelationIds(@Param("schoolId") Integer schoolId,
                                           @Param("tsId") Integer tsId);

    /**
     * 批量删除关系
     */
    int batchDeleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 根据知识点ID删除相关关系
     */
    int deleteByPointIds(@Param("ids") List<Integer> pointIds);

    List<TopicRelation> findByType(Integer schoolId, Integer tsId, int i);

    List<TopicRelation> findBySchoolAndTs(Integer schoolId, Integer tsId);

    // 检查关系是否已存在
    @Select("SELECT COUNT(*) FROM topic_relations " +
            "WHERE parent_tss_id = #{parentId} " +
            "AND child_tss_id = #{childId} " +
            "AND relation_type = #{relationType} " +
            "AND school_id = #{schoolId} " +
            "AND ts_id = #{tsId}")
    int existsRelation(@Param("parentId") Integer parentId,
                       @Param("childId") Integer childId,
                       @Param("relationType") Integer relationType,
                       @Param("schoolId") Integer schoolId,
                       @Param("tsId") Integer tsId);

//    /**
//     * 获取所有关系的标识符和ID映射
//     */
//    @Select("SELECT relation_id, parent_tss_id, child_tss_id, relation_type " +
//            "FROM topic_relations " +
//            "WHERE school_id = #{schoolId} AND ts_id = #{tsId}")
//    @MapKey("identifier")
//    Map<SchoolKnowledgeService.RelationIdentifier, Integer> selectRelationIdentifiers(
//            @Param("schoolId") Integer schoolId,
//            @Param("tsId") Integer tsId
//    );
}
