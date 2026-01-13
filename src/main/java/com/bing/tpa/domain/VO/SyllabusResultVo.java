package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SyllabusResultVo {
    @ApiModelProperty("大纲文本")
    private String content;
    @ApiModelProperty("大纲一级标题")
    private String designTitle;
    @ApiModelProperty("大纲二级标题")
    private String secondaryTitle;

}
