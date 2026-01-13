package com.bing.tpa.modelcall.designCall;

import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.ChatEvent;
import com.coze.openapi.client.chat.model.ChatEventType;
import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ChatWithModel {
    private static final String token = "pat_roj2ylghfN0Ri9LcM8LiZ6cijILOTcqum0etQQs5mxKk1ZiNGGwZVbz9KueIao9X";
    private static final String baseUrl = "https://api.coze.cn";
    private static final int TIMEOUT_SECONDS = 400;

    public String chatClient(String text, String userId, String botId) throws Exception {
        CozeAPI client = createCozeClient();
        CreateChatReq req = buildChatRequest(userId, text, botId);
        return executeSyncRequest(client, req).replace("\\", "");
    }

    public static CozeAPI createCozeClient() {
        return new CozeAPI.Builder()
                .baseURL(baseUrl)
                .auth(new TokenAuth(token))
                .readTimeout(15000)
                .connectTimeout(5000)
                .build();
    }

    public static CreateChatReq buildChatRequest(String userId, String message, String botId) {
        return CreateChatReq.builder()
                .botID(botId)
                .userID(userId)
                .messages(Collections.singletonList(Message.buildUserQuestionText(message)))
                .build();
    }

    public static String executeSyncRequest(CozeAPI coze, CreateChatReq req) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> result = new AtomicReference<>("");
        final StringBuffer contentBuilder = new StringBuffer();
        final AtomicReference<Throwable> error = new AtomicReference<>();

        Flowable<ChatEvent> resp = coze.chat().stream(req);
        resp.subscribeOn(Schedulers.io())
                .subscribe(
                        event -> {
                            if (ChatEventType.CONVERSATION_MESSAGE_DELTA.equals(event.getEvent())) {
                                String content = event.getMessage().getContent();
                                System.out.println(content);
                                contentBuilder.append(content);
                                // 实时更新结果，确保错误时也能获取已接收内容
                                result.set(contentBuilder.toString());
                            }
                            if (ChatEventType.CONVERSATION_CHAT_COMPLETED.equals(event.getEvent())) {
                                result.set(contentBuilder.toString());
                                latch.countDown();
                            }
                        },
                        throwable -> {
                            error.set(throwable);
                            // 即使出错，也设置已接收的内容
                            result.set(contentBuilder.toString());
                            latch.countDown();
                        },
                        () -> {
                            if (latch.getCount() > 0) {
                                latch.countDown();
                            }
                        }
                );

        // 等待结果（带超时）
        if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            coze.shutdownExecutor();
            throw new RuntimeException("Request timeout after " + TIMEOUT_SECONDS + " seconds");
        }

        // 关闭资源
        coze.shutdownExecutor();

        // 处理错误 - 即使有错误也返回已接收的内容
        if (error.get() != null) {
            // 记录错误日志但继续返回部分结果
            System.err.println("Chat error: " + error.get().getMessage());
            // 如果有部分结果，返回它；否则返回空字符串
            return result.get();
        }

        return result.get();
    }
}