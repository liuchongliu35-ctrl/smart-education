package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.InteractionRequireVo;
import com.bing.tpa.domain.VO.InteractionStatsVO;
import com.bing.tpa.domain.entity.TpaInteraction;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaInteractionService extends IService<TpaInteraction> {
    public InteractionStatsVO getInteractionStats(Integer hdId);

    public List<InteractionStatsVO> getInteractionsByTeachingDesign(Integer tdId);

    List<TpaInteraction> getInteractions(InteractionRequireVo requireVo);

    List<TpaInteraction> interactionList(Integer tdId);
}
