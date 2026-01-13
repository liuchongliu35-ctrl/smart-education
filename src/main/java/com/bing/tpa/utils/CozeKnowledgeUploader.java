package com.bing.tpa.utils;

import com.bing.tpa.domain.dto.CozeFileResponse;
import com.bing.tpa.domain.dto.CozeFileResponse_2;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 像知识库上传文件
 */
@Component
public class CozeKnowledgeUploader {

    private static final String API_URL = "https://api.coze.cn/open_api/knowledge/document/create";
    private static final String Authorization = "Bearer pat_fJXS14Ols8zjwU5MSsE2QzjiLEA6VqFhicVgxqKc0NtXXcJBHkAE21moYWG8j0LE"; // 替换为你的 Access Token
//pat_fJXS14Ols8zjwU5MSsE2QzjiLEA6VqFhicVgxqKc0NtXXcJBHkAE21moYWG8j0LE
    private static  String documentId;

    private static final RestTemplate restTemplate=new RestTemplate();
//datasetId知识库的Id：7476419813767741494
public static String uploadFileToKnowledgeBase(Long datasetId, File file) throws Exception {
    // 将文件转换为 Base64 编码
    byte[] fileContent = new byte[(int) file.length()];
    try (FileInputStream fis = new FileInputStream(file)) {
        fis.read(fileContent);
    }
    String fileBase64 = Base64.getEncoder().encodeToString(fileContent);

    // 构建请求体
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("dataset_id", datasetId);

    List<Map<String, Object>> documentBases = new ArrayList<>();
    Map<String, Object> documentBase = new HashMap<>();
    documentBase.put("name", file.getName());

    Map<String, Object> sourceInfo = new HashMap<>();
    sourceInfo.put("file_base64", fileBase64);
    sourceInfo.put("file_type", file.getName().substring(file.getName().lastIndexOf(".") + 1));
    documentBase.put("source_info", sourceInfo);

    documentBases.add(documentBase);
    requestBody.put("document_bases", documentBases);

    Map<String, Object> chunkStrategy = new HashMap<>();
    chunkStrategy.put("chunk_type", 1);
    chunkStrategy.put("separator", "\n\n");
    chunkStrategy.put("max_tokens", 800L);
    chunkStrategy.put("remove_extra_spaces", false);
    chunkStrategy.put("remove_urls_emails", false);
    requestBody.put("chunk_strategy", chunkStrategy);

    requestBody.put("format_type", 0); // 文本类型知识库

    // 设置请求头
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", Authorization);
    headers.set("Agw-Js-Conv", "str");

    // 创建请求实体
    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

    // 发送 POST 请求
    ResponseEntity<String> response = restTemplate.postForEntity(API_URL, requestEntity, String.class);

    // 输出响应
    System.out.println("Response: " + response.getBody());
    ObjectMapper objectMapper = new ObjectMapper();
//    解析一下串,包含logid的就是重复了的，没有的就是没有重复的
    CozeFileResponse cozeFileResponse = null;
    if (response.getBody().contains("logid")){
        CozeFileResponse_2 cozeFileResponse_2=objectMapper.readValue(response.getBody(),CozeFileResponse_2.class);
        for (CozeFileResponse_2.DocumentInfo documentInfo : cozeFileResponse_2.getDocument_infos()) {
            documentId=documentInfo.getDocument_id();
        }
    } else {
        cozeFileResponse = objectMapper.readValue(response.getBody(), CozeFileResponse.class);
        //    获取上传的文件的id
        for (CozeFileResponse.DocumentInfo documentInfo : cozeFileResponse.getDocument_infos()) {
            documentId = documentInfo.getDocument_id();
    }

    }

    return documentId;
}
}
