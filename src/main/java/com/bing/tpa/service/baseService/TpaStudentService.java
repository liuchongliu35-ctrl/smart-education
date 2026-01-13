package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.entity.TpaStudent;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaStudentService extends IService<TpaStudent> {

    /**
     * 添加学生
     */
    public Integer addOneStu(TpaStudent student);

    /**
     * 检查数据库中是否有该学生
     */
    public Integer isExit(TpaStudent tpaStudent);
}
