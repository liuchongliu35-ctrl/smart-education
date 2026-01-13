package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;


import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.common.ResourceType;
import com.bing.tpa.exception.DigitalException;
import com.bing.tpa.service.baseImpl.ResourceService;
import com.bing.tpa.service.sendRequest.FlaskResponse;
import com.bing.tpa.service.sendRequest.FlaskUploader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

//TODO 该类主要用于处理ppt视频上传、视频融合，视频拉取
@Component
public class AfterConvertTask {

//    private final String flaskServerUrl="http://127.0.0.1:5000";
    @Value("${scheduler.flask.baseurl}")
    private String flaskServerUrl;
    @Autowired
    private InMemoryDataStore globalConfig;

    @Autowired
    private FlaskUploader flaskUploader;

    @Autowired
    private FlaskResponse responseDeal;

    @Autowired
    private ResourceService resource;

    /**
     *
     * @param pptVideoPath ppt视频的地址
     * @param userName
     * @return
     * @throws JsonProcessingException
     * @throws DigitalException
     */
    public String digitalHumanVideo(String pptVideoPath,String userName,String pptBaseName,Integer pptCount) throws JsonProcessingException, DigitalException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonNodes = mapper.createObjectNode();
            ObjectNode jsonNode;
            if (globalConfig.get("user")!=null&&globalConfig.get("session_id")!=null&&globalConfig.get("task-"+pptCount)!=null){
                jsonNode= jsonNodes.put("User", globalConfig.get("user").toString())
                        .put("session_id", globalConfig.get("session_id").toString())
                        .put("task_id", globalConfig.get("task-"+pptCount).toString());
            }else throw new DigitalException("任务id等关键参数为空");

//                    .put("ppt_name", pptBaseName)
//                    .put("page_num", pptCount);

            String jsonData = mapper.writeValueAsString(jsonNode);
            // 验证登录状态
            if (!globalConfig.containsKey("user")) {
               throw new DigitalException("未登录");
            }
//        TODO 上传PPT视频，主程序已经进行了转化
            ResponseEntity<String> videoResponse = flaskUploader.connectFlask(flaskServerUrl + "/Send_Video", jsonData, Paths.get(pptVideoPath));
            if (!responseDeal.parseBasicResponse(videoResponse)) {
               throw new DigitalException("ppt视频上传失败");
            }else {
                System.out.println("ppt视频上传成功！");
            }

//       TODO 视频融合
            ResponseEntity<String> mergeResponse = flaskUploader.connectFlask(flaskServerUrl + "/PPT_Video_Merge", jsonData);
            boolean responseStatus = responseDeal.parseStateResponse(mergeResponse, 5000, jsonData, flaskServerUrl + "/Get_State");
            if (!responseStatus) {
              throw new DigitalException("视频融合失败");
            }

//        TODO 拉取融合视频
            File videoFile = new File(pptVideoPath);
            if(!videoFile.exists()){
              throw new DigitalException("未找到转化的ppt视频");
            }
            ObjectNode jsonNodes1 = mapper.createObjectNode();
            jsonNodes1.put("VideoName", videoFile.getName());

            ResponseEntity<String> response = flaskUploader.connectFlask(flaskServerUrl + "/Pull_Video_Merge", jsonData);
//      准备视频的路径
//            String videoRoot="src/main/resources/videoFile";
//            Path videoDir = Paths.get(videoRoot,userName);// src/main/resources/videoFile/liuc
//            Path videoSavePath = videoDir.resolve(videoFile.getName());// src/main/resources/videoFile/liuc/ppt视频的名字.mp4
//            resource.getResourcePath(ResourceType.VIDEOFILE, "");
            Path videoSavePath = resource.getResourcePath(ResourceType.VIDEOFILE, userName);
//      解析Response并保存视频
            boolean response1 = responseDeal.parseFileSaveResponseWithLogging(response,videoSavePath.toString(),videoFile.getName());

            if (response1) {
                return  videoSavePath.toString();
            } else {
              throw new  DigitalException("视频拉取失败");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (DigitalException e) {
            throw new DigitalException("视频生成失败");
        }
    }

}
