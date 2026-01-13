package com.bing.tpa.utils;

import com.bing.tpa.modelcall.KimiClient.ChatCompletionMessage;
import com.bing.tpa.modelcall.KimiClient.ChatMessageRole;
import com.bing.tpa.modelcall.KimiClient.SendMessage;
import com.bing.tpa.modelcall.designCall.ChatWithModel;
import com.bing.tpa.modelcall.hanLP.HanlpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 *  调用hanLP的接口对长文本进行关键词提取，关键词组合凝练
 * 先调用hanLP到的关键词提取，再使用语言大模型对关键词进行有效组合成有意义可以根据这个进行搜索的短语
 * kimi的key：sk-FwrFm8LpROLqgeHZiAAWkwmN83u5JIzJtAg98jE3ByUwMYAh
 */
@Component
public class TextSummaryUtil {

    @Autowired
    private HanlpClient client;

    @Autowired
    private SendMessage sendMessageToKimi;

    @Autowired
    private ChatWithModel chatWithModel;

    private static final RestTemplate restTemplate = new RestTemplate();
//cozekey：
    private static final String apiKey="sk-FwrFm8LpROLqgeHZiAAWkwmN83u5JIzJtAg98jE3ByUwMYAh";
    private static final String botId="7473462018583134247";

    private static final String API_KEY = "sk-FwrFm8LpROLqgeHZiAAWkwmN83u5JIzJtAg98jE3ByUwMYAh"; // 替换为您的 API 密钥
    private static final String BASE_URL = "https://api.moonshot.cn/v1"; // 替换为正确的 API 地址
    private static final String MODEL = "moonshot-v1-auto";

    public String textProcessing(String text){
//        先调用hanLP对文本进行关键词提取
        Map<String, Double> keywords = client.getKeywords(text);
        if (keywords.get("code")!=null&&keywords.get("code")==500) throw new NullPointerException("无法根据长句生成关键字");
        List<ChatCompletionMessage> chatMsg=new ArrayList<>();
        StringBuilder txtBuilder=new StringBuilder();
        txtBuilder.append("下面是几个关键词以及其在一个长文本中的权重：\n");
//        将关键词包装为一个字符串
        for (Map.Entry<String, Double> entry : keywords.entrySet()){
            txtBuilder.append("关键词：").append(entry.getKey()).append(",权重：").append(entry.getValue()).append("\n");
        }
        txtBuilder.append("请对以上几个关键字进行组合，形成几个有实际意义且可以当做关键词进行相关图片或视频资源搜索的短语，每一个短语的字数不可以超过10个，短语的个数请根据" +
                "上面关键字的个数来决定，如果有5个以内的关键字就生成3到4个短语，如果有6到10个关键字就生成5到6个短语，且生成内容除了短语以外不要加任何其他的文字或者序号或标点符号，只要相关短语");
        ChatCompletionMessage chatCompletionMessage1 = new ChatCompletionMessage(ChatMessageRole.USER.value(),txtBuilder.toString());
        chatMsg.add(chatCompletionMessage1);
//       再调用kimi对文本进行凝练5到6个关键短语
        return sendMessageToKimi.sendMessageToKiMi(chatMsg, apiKey);
    }

    /**
     *根据关键字获取视频资源
     */
    public List<String> getVideoUrlFromKimi(String keyword){

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "你是 Kimi，由 Moonshot AI 提供的人工智能助手，你更擅长中文和英文的对话。你会为用户提供安全，有帮助，准确的回答。同时，你会拒绝一切涉及恐怖主义，种族歧视，黄色暴力等问题的回答。Moonshot AI 为专有名词，不可翻译成其他语言。"
        ));
        messages.add(Map.of(
                "role", "user",
                "content", "请根据关键词:"+keyword+"生成5个与之相关的视频资源的链接，只要视频的网址链接，除此之外不要生成任何其他文字，也不要生成序号和任何标点符号，只要视频链接即可，且不要生成打不开链接的视频"
        ));

        return mainLoop(messages);
    }

    public static List<String> mainLoop(List<Map<String, Object>> messages) {
        String finishReason = null;

        String[] urls = new String[0];
        while (finishReason == null || "tool_calls".equals(finishReason)) {
            Map<String, Object> requestBody = Map.of(
                    "model", MODEL,
                    "messages", messages,
                    "temperature", 0.3,
                    "tools", List.of(
                            Map.of(
                                    "type", "builtin_function",
                                    "function", Map.of("name", "$web_search")
                            )
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + API_KEY);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(
                    BASE_URL + "/chat/completions",
                    requestEntity,
                    Map.class
            );

            Map<String, Object> response = responseEntity.getBody();
            Map<String, Object> choice = (Map<String, Object>) ((List<?>) response.get("choices")).get(0);

            finishReason = (String) choice.get("finish_reason");
            System.out.println("Finish Reason: " + finishReason);

            if ("tool_calls".equals(finishReason)) {
                // 将 choice.get("message") 转换为 Map<String, Object>
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                messages.add(message);

                // 检查 message 是否包含 "tool_calls" 键
                if (message.containsKey("tool_calls")) {
                    List<?> toolCalls = (List<?>) message.get("tool_calls");
                    System.out.println(toolCalls.get(0));
                    for (Object toolCallObj : toolCalls) {
                        if (!(toolCallObj instanceof Map)) {
                            throw new IllegalArgumentException("Expected a Map for toolCall, but got: " + toolCallObj.getClass());
                        }

                        Map<String, Object> toolCall = (Map<String, Object>) toolCallObj;

                        Object functionObj = toolCall.get("function");
                        if (!(functionObj instanceof Map)) {
                            throw new IllegalArgumentException("Expected a Map for function, but got: " + functionObj.getClass());
                        }

                        Map<String, Object> function = (Map<String, Object>) functionObj;
                        String toolCallName = (String) function.get("name");
                        Object toolCallArguments = function.get("arguments");
                        System.out.println(toolCallArguments);
                        String toolResult = searchImpl(toolCallArguments);

                        Map<String, Object> toolMessage = Map.of(
                                "role", "tool",
                                "tool_call_id", toolCall.get("id"),
                                "name", toolCallName,
                                "content", toolResult
                        );

                        messages.add(toolMessage);
                    }
                }
            }

//            System.out.println("Response: " + choice.get("message"));
            Object message = choice.get("message");
            Map<String, Object> responseData = (Map<String, Object>) message;
            String urlString = (String) responseData.get("content");
            urls = urlString.split("\n");
            for (String url : urls) {
                System.out.println(url);
            }
        }
        return List.of(urls);
    }

    public static String searchImpl(Object arguments) {
        // 实现搜索逻辑
        return arguments.toString();
    }

    /**
     * 根据关键字获取图片资源
     * 调研coze平台的智能体生成图片
     */

    public String getPhotoUrl(String keyword) {
        String question = "请生成与”" + keyword + "“有关的图片";
        String photoUrls = null;
        try {
            photoUrls = chatWithModel.chatClient(question, "1",botId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photoUrls;
    }

}
