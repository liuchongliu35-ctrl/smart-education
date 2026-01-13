package com.bing.tpa.domain.digital;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
@Setter
@Getter
public class PptNotesRequest {
    private Map<String, Object> notes;
}
