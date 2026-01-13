package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.SyllabusResultVo;
import com.bing.tpa.domain.VO.TpaSyllabusWithNeed;
import com.bing.tpa.domain.entity.TpaSyllabus;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.exception.RedisException;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaSyllabusService extends IService<TpaSyllabus> {

    List<TpaSyllabus> getTpaSyllabus(Integer tid);

    SyllabusResultVo getTpaSyllabusFromAI(TpaSyllabusWithNeed withNeed);

    Integer saveSyllabus(TpaSyllabus syllabus, Integer tdId) throws RedisException;
}
