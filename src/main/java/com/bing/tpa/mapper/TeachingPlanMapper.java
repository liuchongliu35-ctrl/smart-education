package com.bing.tpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.TeachingPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeachingPlanMapper extends BaseMapper<TeachingPlan> {
    List<TeachingPlan> findByTsId(@Param("tsId") Integer tsId);
    
    List<TeachingPlan> findByTsIdAndSchoolId(@Param("tsId") Integer tsId, @Param("schoolId") Integer schoolId);
    
    List<TeachingPlan> findByTsIdOrderByPlanOrder(@Param("tsId") Integer tsId);
}
