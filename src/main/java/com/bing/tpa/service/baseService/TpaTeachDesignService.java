package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.VO.*;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bing.tpa.exception.RedisException;
import com.bing.tpa.service.baseImpl.TpaTeachDesignServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;

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
public interface TpaTeachDesignService extends IService<TpaTeachDesign> {

    public Integer addAndPrepare(TpaTeachDesign teachDesign) throws RedisException;



    String promptFromAI(PromptVo prompt);

    void updateDesignContent(ContentVo contentVo) throws InterruptedException, RedisException;

    List<VideoVo> getVideo(LongTextVo text) throws InterruptedException;

    List<PhotoVo> getPhoto(@RequestBody LongTextVo text);

    List<TpaTeachDesign> matchDesignByTitle(String title);

    String PPTFromDesign(Integer tdId);

    List<PPTVo> getPPtByKnowledge(String title) throws IOException;

    List<DesignAndPPTVo> getAllDesignAndPPT() throws IOException;
}
