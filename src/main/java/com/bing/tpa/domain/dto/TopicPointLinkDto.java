package com.bing.tpa.domain.dto;


import lombok.Data;

@Data
public class TopicPointLinkDto {
    private Integer source;
    private Integer target;
    private Integer relationId;
    private String relationDesc;
}