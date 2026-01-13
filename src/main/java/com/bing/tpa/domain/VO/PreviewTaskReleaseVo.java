package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PreviewTaskReleaseVo {
//    预习任务id
    @ApiModelProperty(value = "预习任务的id",required = true)
    private Integer ptId;
//    班级id
    @ApiModelProperty(value = "班级的id",required = true)
    private Integer cid;
//    截止时间
    @ApiModelProperty(value = "截止时间",required = true)
    private String deadline;
}
