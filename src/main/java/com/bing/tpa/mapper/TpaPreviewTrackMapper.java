package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaPreviewTask;
import com.bing.tpa.domain.entity.TpaPreviewTrack;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.swagger.models.auth.In;
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
public interface TpaPreviewTrackMapper extends BaseMapper<TpaPreviewTrack> {
    List<TpaPreviewTrack> findByIds(@Param("list") List<Integer>  taskIds);

    TpaPreviewTrack selectOneAndName(@Param("uid") Integer uid, @Param("ptId") Integer ptId);

    Integer updateScore(@Param("totalScore") Double totalScore,@Param("ptId") Integer ptId,@Param("uid")Integer uid,@Param("completeNum") Integer completeNum);

    Integer selectScore(@Param("sid")Integer sid);
}
