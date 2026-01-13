package com.bing.tpa.domain.dto;

import com.bing.tpa.excelResource;
import lombok.Data;

@Data
public class StudentExcel {
    @excelResource("学号")
    private String stuNum;

    @excelResource("姓名")
    private String name;

    @excelResource("年级")
    private String stuStage;

}
