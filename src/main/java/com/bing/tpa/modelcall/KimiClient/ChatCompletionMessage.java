package com.bing.tpa.modelcall.KimiClient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatCompletionMessage {
    private String role;
    private String content;

    @JsonCreator
    public ChatCompletionMessage(@JsonProperty("role") String role, @JsonProperty("content") String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
