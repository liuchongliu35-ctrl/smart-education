package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.entity.TeachingPlan;
import com.bing.tpa.mapper.TeachingPlanMapper;
import com.bing.tpa.service.baseService.TeachingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeachingPlanServiceImpl extends ServiceImpl<TeachingPlanMapper, TeachingPlan> implements TeachingPlanService {
    
    @Autowired
    private TeachingPlanMapper teachingPlanMapper;
    
    @Override
    @Transactional
    public boolean saveTeachingPlan(Integer tsId, Integer schoolId, List<Integer> tssIds) {
        // 删除旧的教学计划
        teachingPlanMapper.delete(new QueryWrapper<TeachingPlan>()
                .eq("ts_id", tsId)
                .eq("school_id", schoolId));
        
        // 创建新的教学计划
        List<TeachingPlan> plans = new ArrayList<>();
        for (int i = 0; i < tssIds.size(); i++) {
            TeachingPlan plan = new TeachingPlan();
            plan.setTsId(tsId);
            plan.setSchoolId(schoolId);
            plan.setTssId(tssIds.get(i));
            plan.setPlanOrder(i + 1);
            plans.add(plan);
        }
        
        return saveBatch(plans);
    }
    
    @Override
    public List<TeachingPlan> getTeachingPlanByTsId(Integer tsId) {
        return teachingPlanMapper.findByTsId(tsId);
    }
    
    @Override
    public List<TeachingPlan> getTeachingPlanByTsIdAndSchoolId(Integer tsId, Integer schoolId) {
        return teachingPlanMapper.findByTsIdAndSchoolId(tsId, schoolId);
    }
    
    @Override
    public List<Integer> getTeachingOrderByTsId(Integer tsId) {
        List<TeachingPlan> plans = teachingPlanMapper.findByTsIdOrderByPlanOrder(tsId);
        List<Integer> order = new ArrayList<>();
        for (TeachingPlan plan : plans) {
            order.add(plan.getTssId());
        }
        return order;
    }
}
