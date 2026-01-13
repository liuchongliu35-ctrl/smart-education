package com.bing.tpa.domain.dto.ResponseBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Detail {
    @JsonProperty("logid")
    private String logId;

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }
}
