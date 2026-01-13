package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.domain.dto.SchoolDTO;
import com.bing.tpa.domain.entity.School;
import com.bing.tpa.service.baseService.SchoolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "学校接口")
@RestController
@RequestMapping("school")
public class SchoolController<T> {
    
    @Autowired
    private SchoolService schoolService;

    @Autowired
    private Result<T> result;
    
    @PostMapping
    @ApiOperation("新建学校")
    public Result<School> saveSchool(@RequestBody @Valid SchoolDTO schoolDTO) {
        School school = schoolService.saveSchool(schoolDTO);
        return result.success(school);
    }
    @ApiOperation("获取学校信息")
    @GetMapping("/currentSchool")
    public Result<School> getSchool() {
        School school = schoolService.getSchoolById();
        return result.success(school);
    }
}
