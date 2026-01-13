package com.bing.tpa.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.dto.SubjectDTO;
import com.bing.tpa.domain.entity.TpaSubject;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.DatabaseException;
import com.bing.tpa.service.baseService.TpaSubjectService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
//课程模块
@Api(tags = "课程接口")
@CrossOrigin
@RestController
@RequestMapping("subject")
public class TpaSubjectController<T> {
    @Autowired
    private Result<T> result;

    @Autowired
    private TpaSubjectService subjectService;

    @Autowired
    private TpaTeacherService userService;

    //课程编号组成：课程类型+课程面向+三位的随机数，人工智能通识课的类型分为：人工智能基础课，人工智能核心课，人工智能交叉课，人工智能认知课（面向非计算机、非大学生群体）等
    @ApiOperation("获取所有课程信息，课程名称+课程编号")
    @GetMapping("all")
    public Result<List<TpaSubject>> getAllSubjects(){
        QueryWrapper<TpaSubject> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("subject_name","subject_info","subcode","subtitle");
        List<TpaSubject> list = subjectService.list(queryWrapper);
        list.forEach(t->{
            t.setSubjectName(t.getSubjectName()+"--"+t.getSubcode());//显示课程名称和课程编号
        });
        return result.build(list, ResultCodeEnum.SUCCESS);
    }
    @ApiOperation("根据课程id获取课程基本信息")
    @GetMapping("listById/{subjectName}")
    public Result<TpaSubject> getOtherInfoById(@PathVariable String subjectName){
//        根据老师选择的学科，来匹配与该学科相关的其他数据，如该学科属于哪个教育阶段，属于哪个年级，有几册等
        QueryWrapper<TpaSubject> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("subject_name",subjectName);
        TpaSubject subjectList = subjectService.getOne(queryWrapper);
        return result.build(subjectList,ResultCodeEnum.SUCCESS);
    }
//基于数据归一化存储与关联映射机制
//    //登记用户选择的课程，可以新创建，也可以选择现有的，新创建的需要给课程生成课程编号
//    @ApiOperation("登记用户选择的课程，可以新创建，也可以选择现有的，新创建的需要给课程生成课程编号")
//    @PostMapping("selectSub")
//    public Result<TpaSubject> saveSubject(@RequestBody TpaSubject tpaSubject){
//        //如果tsId为空，说明这个新增的课程
//
//        //如果tsId不为空，说明这个课程是用户选择的，需要将用户选择的课程信息保存到用户课程表中
//    }


    @PostMapping
    @ApiOperation("创建课程")
    public Result<TpaSubject> createSubject(@RequestBody @Valid SubjectDTO subjectDTO) throws DatabaseException {
        // 获取当前用户
        TpaTeacher user = userService.getCurrentUser();
        if (user.getSchoolId() == null) {
            return result.build(null,"405","未绑定学校，请先创建课程");
        }
        // 创建课程，并绑定学校id
        TpaSubject subject = subjectService.createSubject(subjectDTO, user.getSchoolId());
        return subject!=null?result.success(subject):result.build(null,"405","创建课程失败");
    }

    @GetMapping
    @ApiOperation("获取当前用户教授的课程")
    public Result<List<TpaSubject>> getSubjects() {
        // 获取当前用户
        TpaTeacher user = userService.getCurrentUser();
//        System.out.println("学校的id为"+user.getSchoolId());
        if (user.getSchoolId()==null) {
            return result.build(null,"405", "用户未绑定学校");
        }

        List<TpaSubject> subjects = subjectService.getSubjectsBySchoolId(user.getSchoolId());
        if (subjects == null || subjects.isEmpty()) {
            return result.build(null,"405", "该学校尚未开设课程");
        }
        return result.success(subjects);
    }

    @ApiOperation("根据课程id获取课程信息")
    @GetMapping("/{subjectId}")
    public Result<TpaSubject> getSubject(@PathVariable Integer subjectId) {
        TpaSubject subject = subjectService.getSubjectById(subjectId);
        return result.success(subject);
    }






}
