package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class InteractionRequireVo {
//    互动环节所属的教学设计id
    @ApiModelProperty(value = "互动所属的教学设计id",required = true)
    private Integer tdId;
//    互动环节数量
    @ApiModelProperty(value = "互动环节数量",required = true)
    private Integer num;
//    对互动环节要求
    @ApiModelProperty(value = "对互动环节的需求",required = true)
    private String require;
}
