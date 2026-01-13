package com.bing.tpa.domain.dto;

import com.coze.openapi.client.connversations.message.model.Message;
import lombok.Data;

import java.util.List;

@Data
public class AnalysisResult {
          private String homeworkAnalysis;
          private String previewAnalysis;
          private String interactionAnalysis;
          private String prediction;
          private List<Message> messageHistory;
      }
