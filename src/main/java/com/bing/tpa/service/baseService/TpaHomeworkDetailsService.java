package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaHomeworkDetailsService extends IService<TpaHomeworkDetails> {

    boolean addNewQuestion(List<TpaHomeworkDetails> homeworkDetails, Integer hid);

    List<TpaHomeworkDetails>  automaticMatchByTid(Integer tid);

    XWPFDocument exportToWord(Integer hid, Integer ptId);
}
