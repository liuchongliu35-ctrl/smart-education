package com.bing.tpa.domain.dto.ResponseBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Data {
    @JsonProperty("data")
    private List<Document> documents;

    // Getters and Setters
    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}