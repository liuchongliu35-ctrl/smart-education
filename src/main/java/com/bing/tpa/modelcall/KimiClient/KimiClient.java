//package com.bing.tpa.modelcall.KimiClient;
//
//import lombok.Data;
//import org.springframework.http.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.*;
//
//public class KimiClient {
//    private static final String API_KEY = "sk-FwrFm8LpROLqgeHZiAAWkwmN83u5JIzJtAg98jE3ByUwMYAh"; // 替换为您的 API 密钥
//    private static final String BASE_URL = "https://api.moonshot.cn/v1"; // 替换为正确的 API 地址
//    private static final String MODEL = "moonshot-v1-auto";
//
//    private static RestTemplate restTemplate = new RestTemplate();
//
//    public static void main(String[] args) {
//        List<Map<String, Object>> messages = new ArrayList<>();
//        messages.add(Map.of(
//                "role", "system",
//                "content", "你是 Kimi，由 Moonshot AI 提供的人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一切涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Moonshot AI 为专有名词，不可翻译成其他语言。"
//        ));
//        messages.add(Map.of(
//                "role", "user",
//                "content", "请根据关键词:“李白和将近酒”生成5个与之相关的视频资源的链接，只要视频的网址链接，除此之外不要生成任何其他文字，也不要生成序号和任何标点符号，只要视频链接即可，且不要生成打不开链接的视频"
//        ));
//
//        mainLoop(messages);
//    }
//
//    public static void mainLoop(List<Map<String, Object>> messages) {
//        String finishReason = null;
//
//        while (finishReason == null || "tool_calls".equals(finishReason)) {
//            Map<String, Object> requestBody = Map.of(
//                    "model", MODEL,
//                    "messages", messages,
//                    "temperature", 0.3,
//                    "tools", List.of(
//                            Map.of(
//                                    "type", "builtin_function",
//                                    "function", Map.of("name", "$web_search")
//                            )
//                    )
//            );
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Authorization", "Bearer " + API_KEY);
//
//            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
//                    BASE_URL + "/chat/completions",
//                    requestEntity,
//                    Map.class
//            );
//
//            Map<String, Object> response = responseEntity.getBody();
//            Map<String, Object> choice = (Map<String, Object>) ((List<?>) response.get("choices")).get(0);
//
//            finishReason = (String) choice.get("finish_reason");
//            System.out.println("Finish Reason: " + finishReason);
//
//            if ("tool_calls".equals(finishReason)) {
//                // 将 choice.get("message") 转换为 Map<String, Object>
//                Map<String, Object> message = (Map<String, Object>) choice.get("message");
//                messages.add(message);
//
//                // 检查 message 是否包含 "tool_calls" 键
//                if (message.containsKey("tool_calls")) {
//                    List<?> toolCalls = (List<?>) message.get("tool_calls");
//                    System.out.println(toolCalls.get(0));
//                    for (Object toolCallObj : toolCalls) {
//                        if (!(toolCallObj instanceof Map)) {
//                            throw new IllegalArgumentException("Expected a Map for toolCall, but got: " + toolCallObj.getClass());
//                        }
//
//                        Map<String, Object> toolCall = (Map<String, Object>) toolCallObj;
//
//                        Object functionObj = toolCall.get("function");
//                        if (!(functionObj instanceof Map)) {
//                            throw new IllegalArgumentException("Expected a Map for function, but got: " + functionObj.getClass());
//                        }
//
//                        Map<String, Object> function = (Map<String, Object>) functionObj;
//                        String toolCallName = (String) function.get("name");
//                        Object toolCallArguments = function.get("arguments");
//                        System.out.println(toolCallArguments);
//                        String toolResult = searchImpl(toolCallArguments);
//
//                        Map<String, Object> toolMessage = Map.of(
//                                "role", "tool",
//                                "tool_call_id", toolCall.get("id"),
//                                "name", toolCallName,
//                                "content", toolResult
//                        );
//
//                        messages.add(toolMessage);
//                    }
//                }
//            }
//
//            System.out.println("Response: " + choice.get("message"));
//            Object message = choice.get("message");
//            Map<String, Object> responseData = (Map<String, Object>) message;
//            String urlString = (String)responseData.get("content");
//            String[] urls = urlString .split("\n");
//            for (String url:urls){
//                System.out.println(url);
//            }
//        }
//    }
//
//    public static String searchImpl(Object arguments) {
//        // 实现搜索逻辑
//        return arguments.toString();
//    }
//
//}