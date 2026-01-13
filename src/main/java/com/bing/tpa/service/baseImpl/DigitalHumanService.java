package com.bing.tpa.service.baseImpl;


import com.bing.tpa.config.FlaskConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DigitalHumanService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FlaskConfig flaskConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    public CompletableFuture<String> generateVideoFromPptAsync(MultipartFile pptFile, String userId) {
        try {
            String result = generateVideoFromPpt(pptFile, userId);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public String generateVideoFromPpt(MultipartFile pptFile, String userId) throws Exception {
        // 1. 保存PPT文件
        String pptPath = savePptFile(pptFile, userId);

        // 2. 上传PPT到Flask服务
        String pptUploadResponse = uploadPptToFlask(pptPath, userId);

        // 3. 配置PPT内容（使用PPT批注）
        configurePptContent(userId);

        // 4. 配置数字人（使用默认配置）
        configureDigitalPerson(userId);

        // 5. 配置音频（使用默认配置）
        configureAudioSettings(userId);

        // 6. 配置数字人插入页数（默认全部插入）
        configurePeopleLocation(userId);

        // 7. 启动视频生成
        String taskId = startVideoGeneration(userId);

        // 8. 轮询任务状态
        pollTaskStatus(userId, taskId);

        // 9. 拉取生成的视频
        byte[] videoData = pullGeneratedVideo(userId);

        // 10. 保存视频到本地
        return saveVideoToStorage(videoData, userId);
    }

    private String savePptFile(MultipartFile pptFile, String userId) throws IOException {
        Path storagePath = Paths.get(flaskConfig.getPptStorageLocation());
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        String fileName = "ppt_" + userId + "_" + UUID.randomUUID() + getFileExtension(pptFile.getOriginalFilename());
        Path filePath = storagePath.resolve(fileName);
        Files.copy(pptFile.getInputStream(), filePath);

        return filePath.toString();
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String uploadPptToFlask(String pptPath, String userId) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("Json", createJsonData("User", userId));

        File pptFile = new File(pptPath);
        body.add("File", new ByteArrayResource(Files.readAllBytes(pptFile.toPath())) {
            @Override
            public String getFilename() {
                return pptFile.getName();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                flaskConfig.getBaseUrl() + "/Send_Video",
                requestEntity,
                String.class
        );

        return parseFlaskResponse(response);
    }

    private String createJsonData(String key, Object value) {
        try {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put(key, value);
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            throw new RuntimeException("JSON creation failed", e);
        }
    }

    private String parseFlaskResponse(ResponseEntity<String> response) {
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                if ("Success".equals(responseMap.get("result"))) {
                    return "Success";
                }
                throw new RuntimeException("Flask response error: " + responseMap.get("result"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Flask response", e);
            }
        }
        throw new RuntimeException("Flask request failed: " + response.getStatusCode());
    }

    private void configurePptContent(String userId) {
        Map<String, String> request = new HashMap<>();
        request.put("User", userId);
        request.put("PPT_Remakes", "Auto-generated from Spring Boot"); // 实际应用中应从PPT提取

        HttpEntity<Map<String, String>> requestEntity =
                new HttpEntity<>(request, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                flaskConfig.getBaseUrl() + "/Send_PPT_Remakes",
                requestEntity,
                String.class
        );

        parseFlaskResponse(response);
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void configureDigitalPerson(String userId) {
        // 使用默认数字人配置
        Map<String, Object> config = new HashMap<>();
        config.put("User", userId);
        config.put("enhancer", true);
        config.put("expression_scale", 1.0);

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(config, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                flaskConfig.getBaseUrl() + "/Send_Config",
                requestEntity,
                String.class
        );

        parseFlaskResponse(response);
    }

    private void configureAudioSettings(String userId) {
        // 使用默认音频配置
        Map<String, Object> config = new HashMap<>();
        config.put("User", userId);
        config.put("Index", "0"); // 默认男性声音

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(config, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                flaskConfig.getBaseUrl() + "/Send_Select_VITS_Model",
                requestEntity,
                String.class
        );

        parseFlaskResponse(response);
    }

    private void configurePeopleLocation(String userId) {
        // 默认全部插入数字人
        Map<String, String> request = new HashMap<>();
        request.put("User", userId);
        request.put("People_Location", "all"); // 表示全部插入

        HttpEntity<Map<String, String>> requestEntity =
                new HttpEntity<>(request, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                flaskConfig.getBaseUrl() + "/Send_People_Location",
                requestEntity,
                String.class
        );

        parseFlaskResponse(response);
    }

    private String startVideoGeneration(String userId) {
        Map<String, String> request = new HashMap<>();
        request.put("User", userId);

        HttpEntity<Map<String, String>> requestEntity =
                new HttpEntity<>(request, createJsonHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                flaskConfig.getBaseUrl() + "/PPT_Video_Merge",
                requestEntity,
                String.class
        );

        return parseTaskId(response);
    }

    private String parseTaskId(ResponseEntity<String> response) {
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                if ("Video_Merge".equals(responseMap.get("result"))) {
                    return (String) responseMap.get("taskId");
                }
                throw new RuntimeException("Failed to start video generation: " + responseMap.get("result"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse task ID", e);
            }
        }
        throw new RuntimeException("Failed to start video generation: " + response.getStatusCode());
    }

    private void pollTaskStatus(String userId, String taskId) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        boolean isCompleted = false;

        while (!isCompleted &&
                (System.currentTimeMillis() - startTime) < flaskConfig.getTaskTimeout()) {

            Thread.sleep(flaskConfig.getTaskCheckInterval());

            Map<String, String> statusRequest = new HashMap<>();
            statusRequest.put("User", userId);
            statusRequest.put("Task", taskId);

            HttpEntity<Map<String, String>> requestEntity =
                    new HttpEntity<>(statusRequest, createJsonHeaders());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    flaskConfig.getBaseUrl() + "/Get_State",
                    requestEntity,
                    String.class
            );

            String status = parseStatusResponse(response);
            if ("COMPLETED".equals(status)) {
                isCompleted = true;
            } else if ("FAILED".equals(status)) {
                throw new RuntimeException("Video generation failed");
            }
        }

        if (!isCompleted) {
            throw new RuntimeException("Video generation timed out");
        }
    }

    private String parseStatusResponse(ResponseEntity<String> response) {
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                Map<String, String> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                return responseMap.get("result");
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse status response", e);
            }
        }
        throw new RuntimeException("Failed to get task status: " + response.getStatusCode());
    }

    private byte[] pullGeneratedVideo(String userId) {
        Map<String, String> request = new HashMap<>();
        request.put("User", userId);

        HttpEntity<Map<String, String>> requestEntity =
                new HttpEntity<>(request, createJsonHeaders());

        ResponseEntity<byte[]> response = restTemplate.postForEntity(
                flaskConfig.getBaseUrl() + "/Pull_Video_Merge",
                requestEntity,
                byte[].class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("Failed to download video");
    }

    private String saveVideoToStorage(byte[] videoData, String userId) throws IOException {
        Path storagePath = Paths.get(flaskConfig.getStorageLocation());
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        String fileName = "video_" + userId + "_" + UUID.randomUUID() + ".mp4";
        Path filePath = storagePath.resolve(fileName);
        Files.write(filePath, videoData);

        return filePath.toString();
    }
}

//    private static final String BASE_URL = "http://digital-human-api.com:5000";
//    private static final String USERNAME = "your_username";
//    private static final String PASSWORD = "your_password";
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    // 1. 用户登录
//    public boolean login() {
//        String url = BASE_URL + "/Login";
//
//        Map<String, String> request = new HashMap<>();
//        request.put("User", USERNAME);
//        request.put("Password", PASSWORD);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                url,
//                new HttpEntity<>(request, getJsonHeaders()),
//                Map.class
//        );
//
//        return "Success".equals(response.getBody().get("result"));
//    }
//
//    // 2. 上传PPT视频（需要提前将PPT转为视频文件）
//    public boolean uploadPptVideo(MultipartFile videoFile) throws IOException {
//        String url = BASE_URL + "/Send_Video";
//
//        // 准备文件部分
//        ByteArrayResource fileResource = new ByteArrayResource(videoFile.getBytes()) {
//            @Override
//            public String getFilename() {
//                return videoFile.getOriginalFilename();
//            }
//        };
//
//        // 准备JSON元数据
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("Json", "{\"User\": \"" + USERNAME + "\"}");
//        body.add("File", fileResource);
//
//        // 发送请求
//        HttpHeaders headers = getMultipartHeaders();
//        HttpEntity<MultiValueMap<String, Object>> requestEntity =
//                new HttpEntity<>(body, headers);
//
//        ResponseEntity<Map> response = restTemplate.exchange(
//                url,
//                HttpMethod.POST,
//                requestEntity,
//                Map.class
//        );
//
//        return "Success".equals(response.getBody().get("result"));
//    }
//
//    // 3. 设置PPT批注信息
//    public boolean setPptRemarks(String remarksJson) {
//        String url = BASE_URL + "/Send_PPT_Remakes";
//
//        Map<String, String> request = new HashMap<>();
//        request.put("User", USERNAME);
//        request.put("PPT_Remakes", remarksJson);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                url,
//                new HttpEntity<>(request, getJsonHeaders()),
//                Map.class
//        );
//
//        return "Success".equals(response.getBody().get("result"));
//    }
//
//    // 4. 上传数字人形象照片
//    public boolean uploadDigitalHumanImage(String base64Image) {
//        String url = BASE_URL + "/Send_Image";
//
//        Map<String, String> request = new HashMap<>();
//        request.put("User", USERNAME);
//        request.put("Img", base64Image);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                url,
//                new HttpEntity<>(request, getJsonHeaders()),
//                Map.class
//        );
//
//        return "Success".equals(response.getBody().get("result"));
//    }
//
//    // 5. 执行VITS+SadTalker联合推理
//    public String startInference() {
//        String url = BASE_URL + "/Get_Inference_VITS_Sadtalker";
//
//        Map<String, String> request = new HashMap<>();
//        request.put("User", USERNAME);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                url,
//                new HttpEntity<>(request, getJsonHeaders()),
//                Map.class
//        );
//
//        return (String) response.getBody().get("result");
//    }
//
//    // 6. 视频合成（全插入数字人）
//    public String mergeVideo() {
//        String url = BASE_URL + "/PPT_Video_Merge";
//
//        Map<String, String> request = new HashMap<>();
//        request.put("User", USERNAME);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                url,
//                new HttpEntity<>(request, getJsonHeaders()),
//                Map.class
//        );
//
//        return (String) response.getBody().get("result");
//    }
//
//    // 7. 获取最终生成的视频文件
//    public byte[] getFinalVideo() {
//        String url = BASE_URL + "/Pull_Video_Merge";
//
//        Map<String, String> request = new HashMap<>();
//        request.put("User", USERNAME);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                url,
//                new HttpEntity<>(request, getJsonHeaders()),
//                Map.class
//        );
//
//        String base64Video = (String) response.getBody().get("result");
//        return Base64.getDecoder().decode(base64Video);
//    }
//
//    // 轮询任务状态
//    public boolean pollTaskStatus(String taskName, int timeoutSeconds) throws InterruptedException {
//        String url = BASE_URL + "/Get_State";
//        long endTime = System.currentTimeMillis() + timeoutSeconds * 1000;
//
//        while (System.currentTimeMillis() < endTime) {
//            Map<String, String> request = new HashMap<>();
//            request.put("User", USERNAME);
//            request.put("Task", taskName);
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(
//                    url,
//                    new HttpEntity<>(request, getJsonHeaders()),
//                    Map.class
//            );
//
//            String status = (String) response.getBody().get("result");
//            if ("Success".equals(status)) {
//                return true;
//            } else if ("Failed".equals(status)) {
//                return false;
//            }
//
//            // 每2秒轮询一次
//            TimeUnit.SECONDS.sleep(2);
//        }
//        throw new RuntimeException("Task timed out: " + taskName);
//    }
//
//    // 完整工作流
//    public byte[] generateVideo(
//            MultipartFile pptVideo,
//            String pptRemarks,
//            String base64Image
//    ) throws Exception {
//        // 1. 认证
//        if (!login()) throw new RuntimeException("Login failed");
//
//        // 2. 上传PPT视频
//        if (!uploadPptVideo(pptVideo)) throw new RuntimeException("PPT upload failed");
//
//        // 3. 设置批注
//        if (!setPptRemarks(pptRemarks)) throw new RuntimeException("Remarks setup failed");
//
//        // 4. 上传数字人形象
//        if (!uploadDigitalHumanImage(base64Image)) throw new RuntimeException("Image upload failed");
//
//        // 5. 启动推理
//        String inferenceTask = startInference();
//        if (!"Audio_Video_Inference".equals(inferenceTask))
//            throw new RuntimeException("Inference start failed");
//
//        // 轮询推理状态（超时5分钟）
//        if (!pollTaskStatus(inferenceTask, 300))
//            throw new RuntimeException("Inference task failed");
//
//        // 6. 启动视频合成
//        String mergeTask = mergeVideo();
//        if (!"Video_Merge".equals(mergeTask))
//            throw new RuntimeException("Merge start failed");
//
//        // 轮询合成状态（超时3分钟）
//        if (!pollTaskStatus(mergeTask, 180))
//            throw new RuntimeException("Merge task failed");
//
//        // 7. 获取最终视频
//        return getFinalVideo();
//    }
//
//    // 辅助方法：JSON请求头
//    private HttpHeaders getJsonHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        return headers;
//    }
//
//    // 辅助方法：Multipart请求头
//    private HttpHeaders getMultipartHeaders() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        return headers;
//    }
