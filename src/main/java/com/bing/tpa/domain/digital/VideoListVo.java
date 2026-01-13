package com.bing.tpa.domain.digital;

import lombok.Data;

@Data
public class VideoListVo {
    private Integer vid;
    private String videoName;
    private String videoUrl;
    private String videoDesc;//视频的描述
    private String videoWithPPTName;//视频关联的ppt名字
    private String videoWithPPTUrl;//视频关联的ppt地址
    private String videoSize;
    private Boolean isHasVideo;
}
