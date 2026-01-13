package com.bing.tpa.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SchoolDTO {
    private Integer schoolId;
    
    @NotBlank(message = "学校名称不能为空")
    private String schoolName;
    
    private String schoolShortName;
    private String address;
    private String contact;
    private String contactPhone;
}
