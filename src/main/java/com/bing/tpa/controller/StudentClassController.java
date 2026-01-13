package com.bing.tpa.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.StudentListVo;
import com.bing.tpa.domain.entity.StudentClass;
import com.bing.tpa.domain.entity.TpaClass;
import com.bing.tpa.mapper.StudentClassMapper;
import com.bing.tpa.service.baseService.StudentClassService;
import com.bing.tpa.service.baseService.TpaClassService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
//用户-班级模块
@Api(tags = "学生班级关系接口")
@CrossOrigin
@RestController
@RequestMapping("studentClass")
public class StudentClassController {

    @Autowired
    private Result<T> result;

    @Autowired
    private StudentClassService studentClassService;

    @Autowired
    private TpaClassService classService;

    @Autowired
    private StudentClassMapper studentClassMapper;

    //    根据id移除学生
    @ApiOperation("根据id移除学生")
    @DeleteMapping("removeStudent/{cid}/{sid}")
    public Result<T> deleteStudent(@PathVariable String cid, @PathVariable String sid){
        QueryWrapper<StudentClass> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("sid",sid)
                .eq("cid",cid);
        StudentClass student = studentClassService.getOne(queryWrapper);
        boolean update=false;
        if(student!=null){
            studentClassService.removeById(student.getScId());
            TpaClass oneClass = classService.getById(student.getCid());
            if(oneClass!=null&&oneClass.getPerson()>0){
                oneClass.setPerson(oneClass.getPerson()-1);
                update= classService.updateById(oneClass);
            }
        }else return result.build(null,"404","没有找到该学生！");
        if(!update) return result.build(null, ResultCodeEnum.FAIL);
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

//    根据班级id获取班级学生列表
    @ApiOperation("根据班级id获取班级学生列表")
    @GetMapping("list/{cid}")
    public Result<List<StudentListVo>>  getStudentByCid(@PathVariable Integer cid){
        List<StudentListVo> stuList = studentClassMapper.getList(cid);
        if (stuList.size()==0) return result.build(null,"405","该班级没有学生");
        return result.build(stuList,ResultCodeEnum.SUCCESS);
    }

}
