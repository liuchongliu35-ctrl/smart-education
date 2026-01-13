package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.entity.TpaTeachDesign;
import lombok.Data;

@Data
public class DesignAndPPTVo extends TpaTeachDesign {
    private Integer pptId;
    private String pptName;
    private String pptUrl;
    private String pptSize;
    private Boolean isHavePPT;
}
