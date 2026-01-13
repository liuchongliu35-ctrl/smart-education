package com.bing.tpa.modelcall.designCall;


import cn.hutool.core.text.StrBuilder;
import com.bing.tpa.domain.dto.AnalysisResult;
import com.bing.tpa.domain.dto.ChatSession;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.coze.openapi.client.chat.CreateChatReq;
import com.coze.openapi.client.chat.model.Chat;
import com.coze.openapi.client.chat.model.ChatEvent;
import com.coze.openapi.client.chat.model.ChatEventType;
import com.coze.openapi.client.chat.model.ChatPoll;
import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


@Component
public class AnalysisByModel {
//    pat_roj2ylghfN0Ri9LcM8LiZ6cijILOTcqum0etQQs5mxKk1ZiNGGwZVbz9KueIao9X
    private static final String token="pat_roj2ylghfN0Ri9LcM8LiZ6cijILOTcqum0etQQs5mxKk1ZiNGGwZVbz9KueIao9X";
    //    pat_tnPcM2YJzF0uR4f5tBuRa1fdaYbI7RAK6aC9HiHdIuGdy4SVGl1ztB24kthhdd36
    private static final String botId="7474540258005811210";
    //    private static  String userId="";
    private static final String baseUrl="https://api.coze.cn";

    private static final int TIMEOUT_SECONDS=200;

    /**
     * 使用非流式来进行对话
     */

    CozeAPI cozeClient = new CozeAPI.Builder()
            .baseURL(baseUrl)
            .auth(new TokenAuth(token))
            .readTimeout(10000)
            .build();

//  创建基础对话
    public ChatSession createInitialSession(TpaTeachDesign design, List<Message> messageHistory) throws Exception {
//         Message systemMsg = Message.builder()
//                                .content()
//                                .build();

         CreateChatReq req = CreateChatReq.builder()
                                 .botID(botId)
                                 .userID(design.getAuthorId().toString())
                                 .messages(Collections.singletonList(Message.buildUserQuestionText("你是一个专业的教育数据分析助手，需要分步骤处理多个分析任务")))
                                 .build();

          // 创建并立即轮询结果
          ChatPoll chatPoll = cozeClient.chat().createAndPoll(req);
          return new ChatSession(
                  chatPoll.getChat().getConversationID(),
                  chatPoll.getChat().getID(),
                  new ArrayList<>(messageHistory));
     }

    public AnalysisResult sendAnalysisRequest(String question, String conversationId, List<Message> history,int type) throws Exception {
        List<Message> messages = new ArrayList<>(history);
        messages.add(Message.buildUserQuestionText(question));

        CreateChatReq req = CreateChatReq.builder()
                .botID(botId)
                .userID("用户ID") // 这里需要替换为实际的用户ID
                .messages(messages)
                .conversationID(conversationId)
                .build();
        req.setAutoSaveHistory(true);

        ChatPoll response = cozeClient.chat().createAndPoll(req, 30L);
        history.addAll(response.getMessages());

        AnalysisResult result = parseAnalysisResult(response.getMessages(),response.getChat(),type);
        return result;
    }

    private AnalysisResult parseAnalysisResult(List<Message> allMessage, Chat lastMessage,int type) {
        AnalysisResult result = new AnalysisResult();
//        将allMessage赋给历史消息字段
        result.setMessageHistory(allMessage);
        String analysisResultContent = lastMessage.toString();
        if (type==1) {
            result.setHomeworkAnalysis(analysisResultContent);
        } else if (type==2) {
            result.setPreviewAnalysis(analysisResultContent);
        } else if (type==3) {
            result.setInteractionAnalysis(analysisResultContent);
        }else if (type==4)
        result.setPrediction(analysisResultContent); // 这里需要根据实际的预测结果进行设置
        return result;
    }



}
