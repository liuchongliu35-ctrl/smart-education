package com.bing.tpa.service.baseService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.domain.entity.TpaHomeworkSummary;

import java.util.List;

public interface TpaHomeworkSummaryService extends IService<TpaHomeworkSummary> {
    List<TpaHomeworkSummary>  getSummarys(Integer hid, Integer cid);
}
