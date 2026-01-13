package com.bing.tpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bing.tpa.domain.entity.TpaHomeworkSummary;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TpaHomeworkSummaryMapper extends BaseMapper<TpaHomeworkSummary> {
    Integer updateScore(@Param("uid") Integer uid,@Param("hid") Integer hid,@Param("totalScore") double totalScore);

    Integer totalScore(@Param("sid") Integer sid);
}
