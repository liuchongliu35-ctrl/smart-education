package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class PromptVo {
//    教学设计的id，后续结合教学设计进行分析
    @NotNull
    @ApiModelProperty(value = "教学设计id",required = true)
    private Integer tdId;
    @NotNull
    @ApiModelProperty(value = "框选的文字",required = true)
    private String text;
    @NotNull
    @ApiModelProperty(value = "Ai写作提示选择(1：生成提示结构2：直接生成内容3：进行扩写，其他：根据用户输入的需求来实现)",required = true)
    private Integer select;
    @ApiModelProperty("其他写作需求")
    private String other;

}
