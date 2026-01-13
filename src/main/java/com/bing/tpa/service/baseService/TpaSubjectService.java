package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.dto.SubjectDTO;
import com.bing.tpa.domain.entity.TpaSubject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.exception.DatabaseException;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaSubjectService extends IService<TpaSubject> {
    /**
     * 检查并更新学科库
     * @return 是否更新，如果更新了学科库就需要更新与他相关的知识点库
     */
    public Integer check(TpaSubject subject, Integer uid);

    Integer getIdByTeacherId(Integer tid);

    TpaSubject createSubject(SubjectDTO subjectDTO, Integer schoolId) throws DatabaseException;

    List<TpaSubject> getSubjectsBySchoolId(Integer schoolId);

    TpaSubject getSubjectById(Integer subjectId);



}
