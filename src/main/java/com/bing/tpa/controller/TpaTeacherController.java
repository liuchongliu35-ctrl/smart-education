package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.TeachInfoVo;
import com.bing.tpa.domain.dto.UserLoginDTO;
import com.bing.tpa.domain.entity.School;
import com.bing.tpa.domain.entity.TpaSubject;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.IdentityException;
import com.bing.tpa.exception.LessonException;
import com.bing.tpa.service.baseService.SchoolService;
import com.bing.tpa.service.baseService.TpaSubjectService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
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
@Api(tags = "教师用户接口")
@CrossOrigin
@RestController
@RequestMapping("teacher")
public class TpaTeacherController<T> {
        @Resource
        private Result<T> result;

        @Resource
        private TpaTeacherService teacherService;

        @Autowired
        private SchoolService schoolService;

        @Autowired
        private TpaSubjectService subjectService;
/**
 * INSERT INTO `tpa_teacher` (`ts_id`, `account`, `password`, `teach_stage`, `stage_num`, `teach_lesson`, `phone`, `sex`, `volume`) VALUES
 * (1, 'teacher_zhang', 'zhang123', '高中', 2, '数学', '13800138000', '男', '二');
 */
    /**
     * 教师注册并登录，同时录取基本信息
     */
    @ApiOperation("教师注册并录入教学信息")
    @PostMapping("save")
    public  Result<Integer> teacherRegistration(@RequestBody TpaTeacher teacher) throws IdentityException {
        Integer tid = null;
        try {
            tid = teacherService.newAndAddInfo(teacher);
        } catch (LessonException e) {
            e.printStackTrace();
        }
        if(tid==null) return result.build(null, ResultCodeEnum.SAVE_FAIL);
        if(tid==-1) return result.build(null,ResultCodeEnum.PARAM_ERROR);
        return result.build(tid,"200","登记成功");
    }

    /**
     * 修改信息
     */
    @ApiOperation("修改教师信息")
    @PutMapping("update")
    public Result<T> updateInfo(@RequestBody TeachInfoVo teachInfoVo) throws LessonException {
        int isSuccess = teacherService.updateInfo(teachInfoVo);
        if(isSuccess==0) return result.build(null,"405","没有需要更新的信息");
        return result.success(null);
    }

    @ApiOperation("教师登录")
    @PostMapping("/login")
    public Result<HashMap<String, String>> login(@RequestBody @Valid UserLoginDTO userLoginDTO) throws IdentityException {
        HashMap<String, String> userMap = teacherService.login(userLoginDTO);
        return result.build(userMap,"200","登录成功");
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    public Result<T> logout(){
        teacherService.logout();
        return result.build(null,"200","登出成功");
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public Result<TpaTeacher> getCurrentUser() {
        TpaTeacher user = teacherService.getCurrentUser();
//        获取老师学校信息
        if (user.getSchoolId()!=null) {
            School school = schoolService.getById(user.getSchoolId());
            user.setSchoolName(school.getSchoolName());
        }
//        获取教学科目信息
        List<TpaSubject> subjects = subjectService.getSubjectsBySchoolId(user.getSchoolId());
        user.setTsId(subjects.get(0).getTsId());
        return result.success(user);
    }

    @ApiOperation("更新教师学校信息")
    @PutMapping("/school/{schoolId}")
    public Result<Boolean> updateSchoolInfo(@PathVariable Integer schoolId) {
        TpaTeacher user = teacherService.getCurrentUser();
        boolean result1 = teacherService.updateSchoolInfo(user.getUid(), schoolId);
        return result.success(result1);
    }




}
