package com.bing.tpa.domain.VO;

import lombok.Data;

import java.util.List;

@Data
public class VideoVo {
    private String keyword;
    private List<String> videoUrl;
}
