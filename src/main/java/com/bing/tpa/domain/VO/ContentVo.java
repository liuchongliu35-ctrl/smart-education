package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.NotBlank;

@Data
public class ContentVo {
    @NotBlank
    @ApiModelProperty(value = "富文本框中新的文本",required = true)
    private String newContents;
    @NotNull
    @ApiModelProperty(value = "教学设计id",required = true)
    private Integer tdId;
    @NotNull
    @ApiModelProperty(value = "用户id",required = true)
    private Integer uid;
}
