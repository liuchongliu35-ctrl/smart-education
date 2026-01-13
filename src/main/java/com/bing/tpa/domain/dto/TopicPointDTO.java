package com.bing.tpa.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class TopicPointDTO {
    private Integer tssId;
    private Integer tsId;
    private Integer templateId;
    private String topTitle;
    private String secondaryTitle;
    private String definedTitle;
    private String content;
    private Integer level;
    private Integer isTemplate;
}
