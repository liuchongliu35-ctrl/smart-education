package com.bing.tpa.modelcall.homeworkCall;

import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.service.service.CozeAPI;
import org.springframework.stereotype.Component;

import static com.bing.tpa.modelcall.designCall.ChatWithModel.*;

@Component
public class TaskGenerate {
//    pat_roj2ylghfN0Ri9LcM8LiZ6cijILOTcqum0etQQs5mxKk1ZiNGGwZVbz9KueIao9X
    private static final String token="pat_roj2ylghfN0Ri9LcM8LiZ6cijILOTcqum0etQQs5mxKk1ZiNGGwZVbz9KueIao9X";
    //    pat_tnPcM2YJzF0uR4f5tBuRa1fdaYbI7RAK6aC9HiHdIuGdy4SVGl1ztB24kthhdd36
    private static final String baseUrl="https://api.coze.cn";
//    /v3/chat/

    private static final int TIMEOUT_SECONDS=30;


    public String chatClient(String text,String userId,String botId) throws Exception {
        // 2. 客户端配置
        CozeAPI client = createCozeClient();

        // 3. 构建请求
        CreateChatReq req = buildChatRequest(userId, text, botId);

        // 4. 同步调用
        return executeSyncRequest(client, req).replace("\\","");

    }
}
