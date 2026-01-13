package com.bing.tpa.modelcall.KimiClient;

import com.google.gson.Gson;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;



class ChatCompletionRequest {
    public String model;
    public List<ChatCompletionMessage> messages;
    public float temperature;
    public boolean stream;
    public List<Tool> tools;
    public ChatCompletionRequest(String model, List<ChatCompletionMessage> messages, float temperature,List<Tool> tools) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
        this.stream = true;
        this.tools = tools;
    }
}

class ChatCompletionStreamChoiceDelta {
    private final String content;

    ChatCompletionStreamChoiceDelta(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

class ChatCompletionStreamChoice {
    private final ChatCompletionStreamChoiceDelta delta;

    ChatCompletionStreamChoice(ChatCompletionStreamChoiceDelta delta) {
        this.delta = delta;
    }

    public ChatCompletionStreamChoiceDelta getDelta() {
        return delta;
    }
}

class ChatCompletionStreamResponse {
    private final List<ChatCompletionStreamChoice> choices;

    ChatCompletionStreamResponse(List<ChatCompletionStreamChoice> choices) {
        this.choices = choices;
    }

    public List<ChatCompletionStreamChoice> getChoices() {
        return choices;
    }
}

@Component
public class Client {
    private static final String DEFAULT_BASE_URL = "https://api.moonshot.cn/v1";
    private static final String CHAT_COMPLETION_SUFFIX = "/chat/completions";
    private final String apiKey;

    private final String baseUrl;
    private final OkHttpClient client;
    private final Gson gson;

//    使用构造函数注入
    @Autowired
    public Client(@Value("${spring.api.key}") String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    public Client(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public String getChatCompletionUrl() {
        return baseUrl + CHAT_COMPLETION_SUFFIX;
    }

    public String chatCompletionStream(ChatCompletionRequest request) {
        StringBuilder result=new StringBuilder();
//        List<String> result = new ArrayList<>();
        try {
            request.stream = true;
//            1.构建请求体
            RequestBody body = RequestBody.create(
                    gson.toJson(request),
                    MediaType.parse("application/json")

            );
//          2，构建http请求
            Request httpRequest = new Request.Builder()
                    .url(getChatCompletionUrl())
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
//          发送http请求并解析结果
            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
//              解析结果
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    throw new IOException("Response body is null");
                }

                try (okio.BufferedSource source = responseBody.source()) {
                    String line;
                    while ((line = source.readUtf8Line()) != null) {
                        if (line.startsWith("data:")) {
                            line = line.substring(5).trim();
                        }
                        if (Objects.equals(line, "[DONE]")) {
                            break;
                        }
                        if (!line.isEmpty()) {
                            ChatCompletionStreamResponse streamResponse = gson.fromJson(line, ChatCompletionStreamResponse.class);
                            if (!streamResponse.getChoices().isEmpty()) {
                                ChatCompletionStreamChoiceDelta delta = streamResponse.getChoices().get(0).getDelta();
                                if (delta != null && delta.getContent() != null) {
                                    result.append(delta.getContent());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
