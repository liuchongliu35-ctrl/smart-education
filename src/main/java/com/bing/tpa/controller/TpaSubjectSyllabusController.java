package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.service.baseService.TpaSubjectSyllabusService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
//课程模块
@Api(tags = "课程知识点接口")
@CrossOrigin
@RestController
@RequestMapping("subjectSyllabus")
public class TpaSubjectSyllabusController<T> {

    @Autowired
    private Result<T> result;

    @Autowired
    private TpaSubjectSyllabusService subjectSyllabusService;

    @Autowired
    private TpaTeacherService teacherService;

//    填写教学设计的主题时，给予主题提示  TODO 弃用
//    @ApiOperation("教学设计知识点提示")
//    @GetMapping("syllabus/{tid}")
//    public Result<PointsVo> getSyllabusByTeacherId(@PathVariable Integer tid){
//        PointsVo syllabus = subjectSyllabusService.getSyllabusByTeacherId(tid);
//        if(syllabus==null) return result.build(null,"405","获取知识点提示失败！");
//        return result.build(syllabus, ResultCodeEnum.SUCCESS);
//    }


    /**
     * 该方法用在学生填写教学设计主题知识点以及教学设计的名字的时候进行异步检验，当课程的知识点不符合实际的时候，进行提醒或将标准的替换掉老师输入的
     * 如果重复就会将知识库中的标准形式的知识点返回，响应码为406，前端需要将标准的知识点形式放入输入框中，如果是不同章节知识点一样，就会抛一个405的异常
     * 如果返回的是200，则表示新知识点天啊及成功
     * @param checkLesson
     * @param tid
     * @return
     */
//    TODO 使用新的知识点网的查重机制来代替
//    如果用户不使用提示的主题，自己写，就需要校验该知识点是否重复或者合理，如果不合理就需要进行纠正
//    @ApiOperation("tid为教师id,检查用户输入的知识点是否重复")
//    @PostMapping("check/{tid}")
//    public Result<TpaSubjectSyllabus> checkSyllabus(@RequestBody CheckLessonBean checkLesson, @PathVariable Integer tid) {
////        先根据老师的id查找课程
//        TpaSubject subject = teacherService.getLessonIdByTid(tid);
////        将课程的id赋给CheckLessonBean的课程id属性
//        checkLesson.setLessonId(subject.getTsId());
//        TpaSubjectSyllabus syllabus = null;
//        try {
//            syllabus = subjectSyllabusService.duplicateCheck(checkLesson);
//        } catch (LessonException | DuplicateException e) {
//            e.printStackTrace();
//        }
//        if (syllabus!=null) return result.build(syllabus,"406","知识点重复，已替换为知识库中标准形式");
//            return result.build(null,"200","新知识点添加成功！");
//    }
}
