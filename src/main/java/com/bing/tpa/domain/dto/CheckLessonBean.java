package com.bing.tpa.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CheckLessonBean {
//    课程的id
    @ApiModelProperty(value = "课程的id",required = true)
    private Integer lessonId;
    @ApiModelProperty(value = "主题一",required = true)
    private String topTitle;
    @ApiModelProperty(value = "主题二",required = true)
    private String secondaryTitle;
    @ApiModelProperty(value = "自定义知识点,可以为空",required = true)
    private String definedTitle;
}
