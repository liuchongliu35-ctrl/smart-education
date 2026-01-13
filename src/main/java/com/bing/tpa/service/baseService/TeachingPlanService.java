package com.bing.tpa.service.baseService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.domain.entity.TeachingPlan;

import java.util.List;

public interface TeachingPlanService extends IService<TeachingPlan> {
    boolean saveTeachingPlan(Integer tsId, Integer schoolId, List<Integer> tssIds);
    
    List<TeachingPlan> getTeachingPlanByTsId(Integer tsId);
    
    List<TeachingPlan> getTeachingPlanByTsIdAndSchoolId(Integer tsId, Integer schoolId);
    
    List<Integer> getTeachingOrderByTsId(Integer tsId);
}
