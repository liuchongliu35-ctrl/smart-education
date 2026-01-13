package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
public interface TpaTeachDesignMapper extends BaseMapper<TpaTeachDesign> {

    List<TpaTeachDesign> selectByTitle(@Param("title") String title);

    List<TpaTeachDesign> selectByAuthorId(@Param("uid") Integer uid);
}
