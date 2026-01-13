package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.entity.StudentClass;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface StudentClassService extends IService<StudentClass> {

    /**
     * 添加学生班级关系
     */
    public boolean addConnection(List<StudentClass> studentClasses);
}
