package com.bing.tpa.domain.dto;

import com.coze.openapi.client.connversations.message.model.Message;
import lombok.Data;

import java.util.List;

/**
 * 数据分析辅助类
 */
@Data
public class ChatSession {
          private final String conversationId;
          private final String chatId;
          private final List<Message> messageHistory;
}
