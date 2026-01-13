package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.ExtraInfo;
import com.bing.tpa.domain.VO.TeachInfoVo;
import com.bing.tpa.domain.dto.UserLoginDTO;
import com.bing.tpa.domain.entity.TpaSubject;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.exception.IdentityException;
import com.bing.tpa.exception.LessonException;

import java.util.HashMap;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaTeacherService extends IService<TpaTeacher> {

    public Integer newAndAddInfo(TpaTeacher teacher) throws LessonException, IdentityException;

    public int updateInfo(TeachInfoVo teachInfoVo) throws LessonException;

    TpaSubject getLessonIdByTid(Integer tid);

    HashMap<String, String> login(UserLoginDTO userLoginDTO) throws IdentityException;

    TpaTeacher  getCurrentUser();

    boolean updateSchoolInfo(Integer userId, Integer schoolId);

//    退出登录
    void logout();

}
