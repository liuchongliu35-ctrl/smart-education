package com.bing.tpa.service.special;

import com.bing.tpa.domain.entity.TpaSubject;

import java.util.List;

public interface SubjectService {

    // 根据ID获取课程
    TpaSubject getSubjectById(Integer tsId);

    // 获取学校所有课程
    List<TpaSubject> getSubjectsBySchoolId(Integer schoolId);

    // 创建新课程
    TpaSubject createSubject(TpaSubject subject);

    // 更新课程信息
    TpaSubject updateSubject(TpaSubject subject);

    // 删除课程
    boolean deleteSubject(Integer tsId);
}
