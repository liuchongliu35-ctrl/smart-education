package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.dto.CheckLessonBean;
import com.bing.tpa.domain.entity.TpaSubjectSyllabus;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.exception.DuplicateException;
import com.bing.tpa.exception.LessonException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaSubjectSyllabusService extends IService<TpaSubjectSyllabus> {

//    TODO 弃用，使用topicPointService中的来代替
//    public PointsVo getSyllabusByTeacherId(Integer tid);

    /**
     * 检查老师创建教学设计、预习任务、作业的时候设置的知识点是否在数据库中存在，如果存在就不插入新的知识点并将原有知识点返回当做该次任务的知识点范围，如果没有就插入新的
     */
    //知识点重复检查算法
//    TODO 使用新的知识点网的查重机制来代替
//    public  TpaSubjectSyllabus duplicateCheck(CheckLessonBean checkLesson) throws LessonException, DuplicateException;

}
