package com.bing.tpa.utils;

import cn.hutool.log.Log;
import com.bing.tpa.domain.dto.ResponseBuilder.Document;
import com.bing.tpa.domain.dto.ResponseBuilder.ResponseData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.dynalink.linker.LinkerServices;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Component
public class CozeUploadProgress {

    private static final String API_URL = "https://api.coze.cn/v1/datasets/{dataset_id}/process";
    private static final String Authorization = "Bearer pat_fJXS14Ols8zjwU5MSsE2QzjiLEA6VqFhicVgxqKc0NtXXcJBHkAE21moYWG8j0LE"; // 替换为你的 Access Token
//pat_fJXS14Ols8zjwU5MSsE2QzjiLEA6VqFhicVgxqKc0NtXXcJBHkAE21moYWG8j0LE
    private static  String documentId;

    private static final RestTemplate restTemplate=new RestTemplate();

//    获取正在上传的文件的上传进度
    public static Integer getProgress(Long datasetId, String... documentId){

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", Authorization);
        headers.set("Agw-Js-Conv", "str");

//        构建路径请求参数
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .buildAndExpand(datasetId);

//        设置请求体
        Map<String, Object> requestBody = new HashMap<>();

        List<String> documentIds=new ArrayList<>();
        Collections.addAll(documentIds, documentId);

        requestBody.put("document_ids",documentIds);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(uriBuilder.toUriString(), requestEntity, String.class);
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        // 输出响应
        System.out.println("Response: " + response.getBody());
//        解析出progress
        ObjectMapper objectMapper=new ObjectMapper();
        ResponseData responseData= null;
        try {
            responseData = objectMapper.readValue(response.getBody(), ResponseData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Integer progress = null;
        if(responseData.getData()!=null&&responseData.getData().getDocuments()!=null){
            for (Document document:responseData.getData().getDocuments()){
                progress=document.getProgress();
            }
        }
        return progress;
    }

//    todo 上传图片到coze

}
