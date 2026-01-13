package com.bing.tpa.service.baseService;

import com.bing.tpa.domain.digital.VideoListVo;

import java.io.IOException;
import java.util.List;

public interface DigitalService {
    List<VideoListVo> getVideoList() throws IOException;

    List<VideoListVo> getVideoByKnowledge(String title) throws IOException;
}
