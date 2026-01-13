package com.bing.tpa.mapper;

import com.bing.tpa.domain.VO.StudentListVo;
import com.bing.tpa.domain.entity.StudentClass;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.TpaStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Mapper
public interface StudentClassMapper extends BaseMapper<StudentClass> {

    List<StudentClass> getStudent(@Param("cid") Integer cid);

    List<StudentListVo> getList(Integer cid);
}
