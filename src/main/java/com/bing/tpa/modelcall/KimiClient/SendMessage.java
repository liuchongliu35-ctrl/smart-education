package com.bing.tpa.modelcall.KimiClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 与kimi交互的类
 */
@Component
public class SendMessage {
    private static final Logger logger=LoggerFactory.getLogger(SendMessage.class);

    @Autowired
    private Client client;

    public  String sendMessageToKiMi(List<ChatCompletionMessage> messages,String apiKey){
        logger.info("接受到的请求："+messages);
        Tool tool=new Tool("builtin_function",new Function("$web_search"));
        List<Tool> tools=new ArrayList<>();
        tools.add(tool);
//        moonshot-v1-8k
        ChatCompletionRequest request = new ChatCompletionRequest("moonshot-v1-128k", messages, 0.3f,tools);
//        Client client=new Client();
        String result = client.chatCompletionStream(request);
        logger.info("kimi返回的结果为：："+result);
        return result;
    }
}
