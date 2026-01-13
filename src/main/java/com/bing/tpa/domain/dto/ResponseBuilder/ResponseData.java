package com.bing.tpa.domain.dto.ResponseBuilder;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseData {
    private int code;
    private Data data;
    private String msg;
    @JsonProperty("detail")
    private Detail detail;

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }
}