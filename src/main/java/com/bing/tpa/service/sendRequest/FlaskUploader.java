package com.bing.tpa.service.sendRequest;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FlaskUploader {
    private static final long DEFAULT_TIMEOUT = 7200;

    /**
     * 发送JSON和文件到Flask服务器
     *
     * @param flaskUrl Flask服务器URL
     * @param jsonData JSON数据字符串
     * @param filePath 文件路径
     * @return 响应实体
     */
    public  ResponseEntity<String> connectFlask(
            String flaskUrl,
            String jsonData,
            Path filePath) {

        RestTemplate restTemplate = createRestTemplateWithTimeout();
        // 验证文件是否存在
        File file = filePath.toFile();
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "文件不存在：" + filePath);
            return null;
        }
        try {
            // 1. 创建多部分表单数据
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 添加JSON数据
            body.add("Json", jsonData);

            // 添加文件
            body.add("File", new FileSystemResource(file) {
                @Override
                public String getFilename() {
                    return file.getName();
                }
            });

            // 2. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 3. 创建请求实体
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 5. 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, requestEntity, String.class);
            if(response.getStatusCode().is2xxSuccessful()){
                return  response;
            }else {
                return  null;
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // 处理HTTP错误
            throw new RuntimeException("请求失败: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("发送请求失败: " + e.getMessage());
        }
    }

    public  ResponseEntity<String> connectFlask(String flaskUrl, String jsonData) {
        RestTemplate restTemplate = createRestTemplateWithTimeout();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, headers);
            ResponseEntity<String> response= restTemplate.exchange(
                    flaskUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            if(response.getStatusCode().is2xxSuccessful()){
                return response;
            }else {
                return null;
            }
        } catch (RestClientException ex) {
            // 显示错误信息
            JOptionPane.showMessageDialog(null, ex.getMessage() + "服务器连接失败");
            return null;
        }
    }
    // 创建带超时配置的 RestTemplate
    private  RestTemplate createRestTemplateWithTimeout() {
        // 配置超时设置
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(3600))
                .setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(3600))
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(3600))
                .build();

        // 创建 HTTP 客户端
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        // 创建请求工厂
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }

}

