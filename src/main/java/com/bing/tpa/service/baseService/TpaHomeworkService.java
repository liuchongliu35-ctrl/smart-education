package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.HomeworkReleaseVo;
import com.bing.tpa.domain.VO.HomeworkTotalSituation;
import com.bing.tpa.domain.VO.HomeworkVo;
import com.bing.tpa.domain.entity.TpaHomework;
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
public interface TpaHomeworkService extends IService<TpaHomework> {

     HomeworkVo generateQuestions(TpaHomework homework, Integer tid) throws FormatException;

    boolean releaseHomework(HomeworkReleaseVo releaseVo);

    HomeworkVo addExtraQuestion(Integer hid, Integer num);

    HomeworkTotalSituation totalSituation(Integer hid, Integer cid);

    List<TpaHomework> selectByPointAndCid(String title, Integer uid);
}
