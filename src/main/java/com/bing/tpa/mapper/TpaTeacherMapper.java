package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaTeacher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Mapper
public interface TpaTeacherMapper extends BaseMapper<TpaTeacher> {

    int addSubjectId(@Param("tsId") Integer tsId,@Param("uid") Integer uid);
    TpaTeacher findByUsername(@Param("username") String username);

    TpaTeacher findByAccount(@Param("account") String account);
}
