package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaClass;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Mapper
public interface TpaClassMapper extends BaseMapper<TpaClass> {

//      修改班级人数
    Integer updatePersonNUm(@Param("cid") Integer cid, @Param("num") int num);
//      根据班级id或者老师id获取班级人数
    Integer getClassPersonNum(@Param("cid") Integer cid,@Param("tid") Integer tid);
}
