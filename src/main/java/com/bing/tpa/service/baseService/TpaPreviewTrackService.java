package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.*;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.domain.entity.TpaPreviewTrack;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.exception.FormatException;
import io.swagger.models.auth.In;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaPreviewTrackService extends IService<TpaPreviewTrack> {

    List<TpaHomeworkDetails> saveTrackToRedis(Integer id, Integer ptId) throws FormatException;

    List<TpaHomeworkDetails> recovery(Integer id, Integer ptId) throws FormatException;

    Integer submitTrack(Integer ptId, Integer uid, Integer complete) throws Exception;

    Integer submitExtraQuestionAnswer(PreviewTextVo previewTextVo) throws Exception;

    PreviewCompleteVo selectAllInfo(Integer ptId, Integer uid);

    SpecialDataVo specialData(Integer uid, Integer ptId);

    List<TpaPreviewTrack> getStudentListAndPreviewSituationVo(Integer ptId, Integer cid);
}
