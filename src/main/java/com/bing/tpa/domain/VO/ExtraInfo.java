package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaTeacher;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExtraInfo extends TpaTeacher {
    @ApiModelProperty(value = "教授课本副标题")
    private String subtitle;
    @ApiModelProperty(value = "教程版本")
    private String volume;
}
