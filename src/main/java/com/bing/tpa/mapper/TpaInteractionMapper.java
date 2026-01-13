package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaInteraction;
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
public interface TpaInteractionMapper extends BaseMapper<TpaInteraction> {

    List<TpaInteraction> selectByDesignIds(@Param("designIds") List<Integer> ids);
}
