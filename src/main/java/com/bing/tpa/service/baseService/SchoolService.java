package com.bing.tpa.service.baseService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.domain.dto.SchoolDTO;
import com.bing.tpa.domain.entity.School;

public interface SchoolService extends IService<School> {
    School saveSchool(SchoolDTO schoolDTO);
    
    School getSchoolById();
}
