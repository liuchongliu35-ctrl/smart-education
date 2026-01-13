package com.bing.tpa.service.baseImpl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.dto.SchoolDTO;
import com.bing.tpa.domain.entity.School;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.mapper.SchoolMapper;
import com.bing.tpa.service.baseService.SchoolService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.jwt.RedisCache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements SchoolService {
    
    @Autowired
    private SchoolMapper schoolMapper;

    @Autowired
    private TpaTeacherService userService;

    @Autowired
    private RedisCache redisCache;

//    新建学校，并将该学校绑定当前用户
    @Override
    public School saveSchool(SchoolDTO schoolDTO) {
        School school = new School();
        BeanUtils.copyProperties(schoolDTO, school);
        
        if (schoolDTO.getSchoolId() == null) {
            // 新增学校
            school.setCreateTime(LocalDateTime.now());
            school.setIsActive(1);
//            schoolMapper.insert(school);
            boolean save = save(school);
//            将学校绑定当前用户
            if(save){
                TpaTeacher currentUser = userService.getCurrentUser();
                boolean update = userService.lambdaUpdate()
                        .eq(TpaTeacher::getUid, currentUser.getUid())
                        .set(TpaTeacher::getSchoolId, school.getSchoolId())
                        .update();
                if(!update) return null;
//                更新当前用户的学校数据
                currentUser.setSchoolId(school.getSchoolId());
                redisCache.setCacheObject("login:"+currentUser.getUid(),currentUser);
            }
        } else {
            // 更新学校
            schoolMapper.updateById(school);
        }
        return school;
    }
    
    @Override
    public School getSchoolById() {
        Integer schoolId;
        TpaTeacher currentUser = userService.getCurrentUser();
        if(currentUser.getSchoolId()!=null){
            schoolId=currentUser.getSchoolId();
        }else {
            throw new RuntimeException("当前用户没有绑定学校");
        }
        return schoolMapper.selectById(schoolId);
    }
}
