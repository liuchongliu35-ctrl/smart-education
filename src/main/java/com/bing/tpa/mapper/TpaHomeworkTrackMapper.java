package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaHomeworkTrack;
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
public interface TpaHomeworkTrackMapper extends BaseMapper<TpaHomeworkTrack> {

    List<TpaHomeworkTrack> findBath(@Param("homeworkIds") List<Integer> homeworkIds);

    List<TpaHomeworkTrack> selectOneAndQuestion(@Param("hid") Integer hid,@Param("uid") Integer uid);
}
