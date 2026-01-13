package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LongTextVo {
    @ApiModelProperty(value = "长文本",required = true)
    private String text;
}
