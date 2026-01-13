package com.bing.tpa.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SubjectDTO {
    private Integer tsId;
    private String subjectName;
    private String subjectInfo;
    private String subjectStage;
    private Integer grade;
    private String volume;
    private String subtitle;
    private String customize;
    private String subcode;
    private Integer isOpen;
    private String purpose;
}
