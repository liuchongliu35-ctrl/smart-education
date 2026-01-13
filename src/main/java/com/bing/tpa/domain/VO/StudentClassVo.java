package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaStudent;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StudentClassVo extends TpaStudent {

//    班级Id
@ApiModelProperty(value = "班级的id",required = true)
    private Integer cid;
}
