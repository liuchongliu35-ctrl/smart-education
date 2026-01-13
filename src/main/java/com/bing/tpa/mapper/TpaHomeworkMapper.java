package com.bing.tpa.mapper;

import com.bing.tpa.domain.entity.TpaHomework;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.w3c.dom.stylesheets.LinkStyle;

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
public interface TpaHomeworkMapper extends BaseMapper<TpaHomework> {

    String selectSubjectByHid(@Param("hid") Integer hid);

    Integer updateCompleteNum(@Param("hid") Integer hid);

    List<TpaHomework> selectByAuthorIdAndTitle(@Param("title") String title, @Param("uid") Integer uid);
}
