package com.bing.tpa.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TopicRelationDTO {
    private Integer relationId;
    private Integer parentTssId;
    private Integer childTssId;
    private Integer relationType;
    private Integer isTemplate;
    private Integer templateRelationId;
}
