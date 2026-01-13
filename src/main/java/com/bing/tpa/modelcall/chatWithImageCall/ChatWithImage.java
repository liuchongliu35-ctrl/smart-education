package com.bing.tpa.modelcall.chatWithImageCall;

import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.ChatEvent;
import com.coze.openapi.client.chat.model.ChatEventType;
import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.client.connversations.message.model.MessageObjectString;
import com.coze.openapi.client.files.UploadFileReq;
import com.coze.openapi.client.files.model.FileInfo;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;


@Component
public class ChatWithImage {
    //    pat_76hjMyOw3fPj42TPZLBGUNvYEz4bWp9lzwOtHcMe86OhkyDvyqyRQuDXoF8hMRN6
    private static final String token="pat_76hjMyOw3fPj42TPZLBGUNvYEz4bWp9lzwOtHcMe86OhkyDvyqyRQuDXoF8hMRN6";
    //    pat_tnPcM2YJzF0uR4f5tBuRa1fdaYbI7RAK6aC9HiHdIuGdy4SVGl1ztB24kthhdd36
    private static final String botId="7539444584796373044";
    //    private static  String userId="";
    private static final String baseUrl="https://api.coze.cn";
    private static final String userId="100";
    private static final int TIMEOUT_SECONDS=200;

    public String chatWithImageCall(String imagePath,String require){
        System.out.println("开始发起对话");
        TokenAuth authCli = new TokenAuth(token);
        CozeAPI coze =
                new CozeAPI.Builder()
                        .baseURL(baseUrl)
                        .auth(authCli)
                        .readTimeout(10000)
                        .build();
        FileInfo imageInfo = coze.files().upload(UploadFileReq.of(imagePath)).getFileInfo();
        CreateChatReq req =
                CreateChatReq.builder()
                        .botID(botId)
                        .userID(userId)
                        .messages(
                                Collections.singletonList(
                                        Message.buildUserQuestionObjects(
                                                Arrays.asList(
                                                        MessageObjectString.buildText(require),
                                                        MessageObjectString.buildImageByID(imageInfo.getID())))))
                        .build();
        System.out.println("完成图片加载");
        Flowable<ChatEvent> resp = coze.chat().stream(req);
        AtomicReference<StringBuilder> result = new AtomicReference<>(new StringBuilder());
        resp.blockingForEach(
                event -> {
                    if (ChatEventType.CONVERSATION_MESSAGE_DELTA.equals(event.getEvent())) {
                        String content = event.getMessage().getContent();//需要将结果累加到一起
                        if (content!=null && !content.isEmpty()) {
                            result.get().append(content);
                        }
                    }
                });
        System.out.println("成功获取结果：");
        System.out.println(result.get().toString());
        return result.get().toString();//最后将累加到一起的结果返回
    }
}
