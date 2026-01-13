package com.bing.tpa.domain.VO;

import com.bing.tpa.domain.dto.UnifiedKnowledgePoint;
import com.bing.tpa.domain.dto.UnifiedKnowledgeRelation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeSaveRequest {
    private List<UnifiedKnowledgePoint> points;
    private List<UnifiedKnowledgeRelation> relations;
}
