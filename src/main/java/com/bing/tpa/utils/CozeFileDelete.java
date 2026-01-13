package com.bing.tpa.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class CozeFileDelete {
    private static final String API_URL = "https://api.coze.cn/open_api/knowledge/document/delete";
    private static final String Authorization = "Bearer pat_fJXS14Ols8zjwU5MSsE2QzjiLEA6VqFhicVgxqKc0NtXXcJBHkAE21moYWG8j0LE"; // 替换为你的 Access Token
//pat_fJXS14Ols8zjwU5MSsE2QzjiLEA6VqFhicVgxqKc0NtXXcJBHkAE21moYWG8j0LE
    private static  String documentId;

    private static final RestTemplate restTemplate=new RestTemplate();

    //    获取正在上传的文件的上传进度
    public static Integer deleteFile(List<String>documentId) {

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", Authorization);
        headers.set("Agw-Js-Conv", "str");


//        设置请求体
        Map<String, Object> requestBody = new HashMap<>();

        // 确保 documentIds 是一个非空列表
        if (documentId == null || documentId.isEmpty()) {
            throw new IllegalArgumentException("documentIds must not be null or empty");
        }

        requestBody.put("document_ids", documentId);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(API_URL, requestEntity, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        // 输出响应
        System.out.println("Response: " + response.getBody());
//        解析出progress
        ObjectMapper objectMapper = new ObjectMapper();
        Response response1 = null;
        try {
            response1 = objectMapper.readValue(response.getBody(), Response.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response1.getCode();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
   static class Response{
       private int code;
       private String msg;

       // Getters and Setters
       public int getCode() {
           return code;
       }

       public void setCode(int code) {
           this.code = code;
       }

       public String getMsg() {
           return msg;
       }

       public void setMsg(String msg) {
           this.msg = msg;
       }
    }
}
