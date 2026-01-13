package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaPreviewTask;
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
public interface TpaPreviewTaskMapper extends BaseMapper<TpaPreviewTask> {

    String selectSubjectByPtId(@Param("ptId") Integer ptId);

    Integer updateCompleteNum(@Param("ptId") Integer ptId);

    List<TpaPreviewTask> getTaskByTitleAndTid(@Param("title") String title, @Param("tid") Integer tid);
}
