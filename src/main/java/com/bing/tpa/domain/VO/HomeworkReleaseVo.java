package com.bing.tpa.domain.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class HomeworkReleaseVo {
//    作业id
@ApiModelProperty(value = "作业id",required = true)
    private Integer hid;
//    班级id
@ApiModelProperty(value = "班级id",required = true)
    private Integer cid;
//    截止时间
@ApiModelProperty(value = "截止时间",required = true)
    private String deadline;
}
