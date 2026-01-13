package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.ClassInfoVo;
import com.bing.tpa.domain.VO.StudentClassVo;
import com.bing.tpa.domain.entity.TpaClass;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.domain.query.PageDTO;
import com.bing.tpa.service.baseImpl.TpaClassServiceImpl;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaClassService extends IService<TpaClass> {

    public TpaClassServiceImpl.ClassData addClass(TpaClass tpaClass);

//    boolean addList(String path, Integer classId);

    Integer addOne(StudentClassVo student);

    PageDTO<TpaClass> getListByTeacherId(Integer tid);

    ClassInfoVo classInfo(Integer cid);
}
