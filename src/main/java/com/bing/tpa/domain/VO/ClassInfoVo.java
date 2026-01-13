package com.bing.tpa.domain.VO;

import lombok.Data;

import java.util.List;

@Data
public class ClassInfoVo {
    private String cName;
    private String person;
    private String shortCode;
    private List<StudentListVo> stuRanking;

}
