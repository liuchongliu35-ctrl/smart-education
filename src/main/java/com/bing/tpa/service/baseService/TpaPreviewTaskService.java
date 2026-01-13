package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.PreviewTaskReleaseVo;
import com.bing.tpa.domain.VO.PreviewTaskVo;
import com.bing.tpa.domain.entity.TpaPreviewTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.exception.FormatException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
public interface TpaPreviewTaskService extends IService<TpaPreviewTask> {

    PreviewTaskVo generateTaskResources(TpaPreviewTask previewTask, Integer tid);

    Integer saveTaskResource(PreviewTaskVo previewTaskVo);

    List<TpaPreviewTask> getTaskList(Integer cid);

    PreviewTaskVo taskByPtId(Integer ptId, Integer id) throws FormatException;

    boolean releaseTask(PreviewTaskReleaseVo taskReleaseVo);

    PreviewTaskVo addQuestions(Integer ptId, Integer num);

    List<TpaPreviewTask> taskByTidAndTitle(String title, Integer tid);

    XWPFDocument generatePreviewWord(Integer ptId) throws IOException, InvalidFormatException;
}
