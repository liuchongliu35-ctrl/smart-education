package com.bing.tpa.modelcall.KimiClient;

public enum ChatMessageRole {
    SYSTEM, USER, ASSISTANT;

    public String value() {
        return this.name().toLowerCase();
    }
}