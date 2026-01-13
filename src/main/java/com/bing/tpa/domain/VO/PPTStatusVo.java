package com.bing.tpa.domain.VO;

import lombok.Data;

@Data
public class PPTStatusVo {
    private Integer tdId;
    private Double progress;
    private String status;
    private String pptName;
}
