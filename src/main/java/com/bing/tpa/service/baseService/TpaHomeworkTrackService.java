package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.HomeworkCompleteVo;
import com.bing.tpa.domain.VO.TrackUpdateVo;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.domain.entity.TpaHomeworkTrack;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.exception.FormatException;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaHomeworkTrackService extends IService<TpaHomeworkTrack> {

    List<TpaHomeworkDetails> saveTrackToRedis(Integer id, Integer hid) throws FormatException;

    Integer updateQuestionAnswer(TrackUpdateVo trackUpdateVo,String keyStr);

    List<TpaHomeworkDetails> recovery(Integer hid, Integer id) throws FormatException;

    Integer submit(Integer complete, Integer hid, Integer uid) throws Exception;

    HomeworkCompleteVo selectAllInfo(Integer hid, Integer uid);
}
