package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;

import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.domain.digital.BaseSetupInfo;
import com.bing.tpa.domain.digital.LoginRequest;
import com.bing.tpa.exception.DigitalException;
import com.bing.tpa.service.sendRequest.FlaskResponse;
import com.bing.tpa.service.sendRequest.FlaskUploader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.nio.file.Paths;

//TODO 该类主要处理登录数字人系统，配置数字人形象，配置音频的任务
@Component
public class CommonTask {
    @Autowired
    private InMemoryDataStore globalConfig;

    @Autowired
    private FlaskUploader flaskUploader;

    @Autowired
    private FlaskResponse responseDeal;

//    private final String flaskServerUrl="http://127.0.0.1:5000";
    @Value("${scheduler.flask.baseurl}")
    private String flaskServerUrl;
//    TODO 登录并配置数字人形象和声音
      public void loginAndSetup(LoginRequest loginInfo, BaseSetupInfo baseInfo,String avatarPath) {
          ObjectMapper mapper = new ObjectMapper();
//          TODO 登录
          try {
              ObjectNode jsonNodes = mapper.createObjectNode();
              jsonNodes.put("User", loginInfo.getUsername());
              jsonNodes.put("Password", loginInfo.getPassword());
              String jsonData = mapper.writeValueAsString(jsonNodes);
              ResponseEntity<String> response = flaskUploader.connectFlask(flaskServerUrl + "/Login", jsonData);
              if(!responseDeal.parseBasicResponse(response)){
                  throw new DigitalException("登录数字人系统失败");
              }

              // 保存用户信息到全局配置
              String body = response.getBody();

              // 将JSON响应转换为JsonNode
              JsonNode responseJson = mapper.readTree(body);
              // 提取session_id（注意字段名要与Flask返回的一致）
              String sessionId = responseJson.get("session_id").asText();
              if (sessionId == null || sessionId.isEmpty()) {
                  throw new DigitalException("响应中未包含session_id");
              }
              // 保存用户信息和session_id到全局配置
              globalConfig.put("user", loginInfo.getUsername());
              globalConfig.put("session_id", sessionId); // 存储session_id供后续使用
          } catch (Exception e) {
              e.printStackTrace();
          }


//          TODO 配置数字人形象和声音配置
          try {
              //测试先使用固定的数字人形象
              ObjectNode jsonNodes = mapper.createObjectNode();
              jsonNodes.put("User", globalConfig.get("user").toString());
              jsonNodes.put("session_id", globalConfig.get("session_id").toString());
              String jsonData = mapper.writeValueAsString(jsonNodes);

              // 保存配置
              globalConfig.put("digitalMotion", baseInfo.getDigitalMotion());
              if (baseInfo.getEnhancer() != null) {
                  globalConfig.put("m_enhancer",baseInfo.getEnhancer());
              }
              Path tempVideo = Paths.get(avatarPath);
              String uploadUrl = flaskServerUrl + "/Send_Teacher_Video";
              //  发送请求
              ResponseEntity<String> response = flaskUploader.connectFlask(uploadUrl, jsonData, tempVideo);
              if (responseDeal.parseBasicResponse(response)) {
                  System.out.println("数字人配置成功");
              } else {
                 throw new DigitalException("数字人形象配置失败");
              }
          } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
          } catch (DigitalException e) {
              throw new RuntimeException(e);
          }

//          TODO 配置声音参数
          try {
              globalConfig.put("m_index", baseInfo.getGender());

              ObjectNode vitsParams = mapper.createObjectNode();

              // 添加User和Index字段
              vitsParams.put("User",  globalConfig.get("user").toString());
              vitsParams.put("session_id", globalConfig.get("session_id").toString());
              vitsParams.put("Index", globalConfig.get("m_index").toString());

              // 序列化为JSON字符串
              String vitsJson = mapper.writeValueAsString(vitsParams);

              ResponseEntity<String> vitsResponse = flaskUploader.connectFlask(flaskServerUrl + "/Send_Select_VITS_Model", vitsJson);
              if (!responseDeal.parseBasicResponse(vitsResponse)) {
                  throw new DigitalException("VITS参数失败");
              }

              ObjectNode wav2LipJson = mapper.createObjectNode();
              ObjectNode config = mapper.createObjectNode();
              wav2LipJson.put("quality",  (Boolean) globalConfig.get("m_enhancer") ? "Enhanced" : "Improved");

              config.put("User",  globalConfig.get("user").toString());
              config.put("session_id", globalConfig.get("session_id").toString());
              config.set("Wav2Lip_Config", wav2LipJson);
              String wav2lipJson = mapper.writeValueAsString(config);
              ResponseEntity<String> wav2LipResponse = flaskUploader.connectFlask(flaskServerUrl + "/Send_Wav2Lip_Config", wav2lipJson);

              if (responseDeal.parseBasicResponse(wav2LipResponse)) {
                  System.out.println("声音配置成功");
              } else {
                  throw new DigitalException("Wav2Lip参数失败");
              }
          } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
          } catch (DigitalException e) {
              throw new RuntimeException(e);
          }
      }

}
