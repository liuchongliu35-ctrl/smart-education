package com.bing.tpa.service.sendRequest;

import com.bing.tpa.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FlaskResponse {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private  RestTemplate restTemplate;
//    TODO 解析Response的json
public boolean flaskResponseJsonSaveWithLogging(ResponseEntity<String> response, String savePath) {
    // 1. 检查响应状态
    if (!response.getStatusCode().is2xxSuccessful()) {
        System.err.println("Flask 响应失败: " + response.getStatusCode());
        return false;
    }

    // 2. 获取响应数据
    String responseJson = response.getBody();
    if (responseJson == null || responseJson.isEmpty()) {
        System.err.println("Flask 响应体为空");
        return false;
    }

    try {
        System.out.println("接受到的json数据：" + responseJson);

        // 3. 解析json数据
        JsonNode rootNode = objectMapper.readTree(responseJson);

        // 4. 检查是否有错误字段
        if (rootNode.has("error")) {
            String errorMsg = rootNode.get("error").asText();
            System.err.println("Flask 返回错误: " + errorMsg);
            return false;
        }

        // 5. 检查 result 字段是否存在
        if (!rootNode.has("result")) {
            System.err.println("JSON 缺少 'result' 字段");
            return false;
        }

        // 6. 直接获取 result 字段的内容（这是一个对象，不是字符串）
        JsonNode resultData = rootNode.get("result");

        // 7. 转换为格式化的 JSON 字符串
        String jsonData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultData);
//       将json数据格式化
        String saveJson = JsonUtils.sortAndFormatJson(jsonData);
        System.out.println("要保存的 JSON 数据: " + saveJson);

        // 8. 保存到文件
        boolean saveResult = saveJsonToFile(saveJson, savePath);
        if (saveResult) {
            System.out.println("JSON 数据已保存到: " + savePath);
        }
        return saveResult;

    } catch (JsonProcessingException e) {
        System.err.println("JSON 解析失败: " + e.getMessage());
        e.printStackTrace();
        return false;
    } catch (IOException e) {
        System.err.println("文件操作失败: " + e.getMessage());
        return false;
    }
}

//    TODO 解析Response(普通解析)
    /**
     * 1. 基础响应解析 - 对应 C# 的 FlaskResponse
     * 检查响应是否成功且包含 "Success" 结果
     *
     * @param response Flask 响应
     * @return 是否成功
     */
    public boolean parseBasicResponse(ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            return false;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode resultNode = rootNode.path("result");
            return "Success".equals(resultNode.asText());
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 2. 状态轮询解析 - 对应 C# 的 FlaskResponseState
     * 轮询任务状态直到完成或失败
     *
     * @param response 初始响应
     * @param interval 轮询间隔（毫秒）
     * @param originalJsonData 原始 JSON 数据
     * @param stateEndpoint 状态检查端点
     * @return 是否成功完成
     */
    public boolean parseStateResponse(ResponseEntity<String> response, long interval,
                                      String originalJsonData, String stateEndpoint) {
        // 检查初始响应
        if (!response.getStatusCode().is2xxSuccessful()) {
            return false;
        }

        try {
            // 解析初始结果
            String taskId = parseResult(response);
            if ("Failed".equals(taskId)) {
                return false;
            }

            // 提取原始 JSON 中的 User
            JsonNode originalNode = objectMapper.readTree(originalJsonData);
            String user = originalNode.path("User").asText();
            String sessionId = originalNode.path("session_id").asText(); // 从原始JSON中获取session_id
            String task_id = originalNode.path("task_id").asText();
            // 创建状态检查请求体（包含User、Task和session_id）
            String stateCheckJson = String.format("{\"User\":\"%s\"," +
                    "\"Task\":\"%s\"," +
                    "\"session_id\":\"%s\"," +
                    "\"task_id\":\"%s\"}",
                    user, taskId,sessionId,task_id);

            // 配置请求头 - 与 C# 一致
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // 明确设置 JSON 类型
//            headers.setAcceptCharset((List<Charset>) StandardCharsets.UTF_8);   // 设置字符集
            headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

            // 创建请求实体 - 包含头部和内容
            HttpEntity<String> requestEntity = new HttpEntity<>(stateCheckJson, headers);

            // 轮询状态
            while (true) {
                // 发送状态检查请求
                ResponseEntity<String> stateResponse = restTemplate.postForEntity(
                        stateEndpoint,
                        requestEntity,
                        String.class
                );

                // 解析状态结果
                String stateResult = parseResult(stateResponse);
                if ("true".equals(stateResult)) {
                    System.out.println("运行完成");
                    return true;
                } else if("false".equals(stateResult)) {
                    System.out.println("运行中...");
                    TimeUnit.MILLISECONDS.sleep(interval);
                } else if ("Failed".equals(stateResult)) {
                    System.out.println("运行失败");
                    return false;
                }
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }


    //    TODO 解析Response 返回Result
    /**
     * 3. 结果提取 - 对应 C# 的 FlaskResponseResult
     * 提取响应中的 "result" 字段
     *
     * @param response Flask 响应
     * @return 结果字符串
     */
    public String parseResult(ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            return null;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.path("result").asText();
        } catch (IOException e) {
            return null;
        }
    }


    //    TODO 解析Response的video视频数据
    /**
     * 文件保存解析
     * @param response Flask 响应
     * @param savePath 文件保存路径
     * @return 是否成功保存
     */
    public boolean parseFileSaveResponseWithLogging(ResponseEntity<String> response, String savePath,String videoName) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("Flask 响应失败: " + response.getStatusCode());
            return false;
        }

        try {
            System.out.println("解析文件保存响应...");
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            // 检查 result 字段
            if (!rootNode.has("result")) {
                System.err.println("JSON 缺少 'result' 字段");
                return false;
            }

            String result = rootNode.get("result").asText();
            if ("Failed".equals(result)) {
                System.err.println("Flask 返回失败状态");

                // 提取错误信息（如果有）
                if (rootNode.has("error")) {
                    System.err.println("错误详情: " + rootNode.get("error").asText());
                }
                return false;
            }

            // 获取 base64 数据
            String fileDataBase64 = rootNode.get("result").asText();
            if (fileDataBase64 == null || fileDataBase64.isEmpty()) {
                System.err.println("文件数据为空");
                return false;
            }

            // 解码 base64
            byte[] fileData = Base64.getDecoder().decode(fileDataBase64);
            System.out.println("解码文件数据成功，大小: " + fileData.length + " bytes");

            // 保存文件
            Path path = Paths.get(savePath,videoName);
            System.out.println("保存路径: " + path.toAbsolutePath());

            // 确保目录存在
            Files.createDirectories(path.getParent());
            Files.write(path, fileData);

            System.out.println("文件保存成功: " + savePath);
            return true;

        } catch (IOException e) {
            System.err.println("文件保存失败: " + e.getMessage());
            return false;
        }
    }



    /**
     * 将 JSON 字符串保存到文件
     *
     * @param jsonData JSON 字符串
     * @param savePath 文件保存路径
     * @return 保存结果（true=成功，false=失败）
     */
    private boolean saveJsonToFile(String jsonData, String savePath) {
        try {
            Path path = Paths.get(savePath);

            // 确保目录存在
            Files.createDirectories(path.getParent());

            // 写入文件
            Files.write(path, jsonData.getBytes());
            return true;
        } catch (IOException e) {
            System.err.println("文件保存失败: " + e.getMessage());
            return false;
        }
    }


}
