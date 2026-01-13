package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;


import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.common.ResourceType;
import com.bing.tpa.domain.digital.CommentInfo;
import com.bing.tpa.domain.digital.FlaskStatus;
import com.bing.tpa.exception.DigitalException;
import com.bing.tpa.service.baseImpl.ResourceService;
import com.bing.tpa.service.sendRequest.FlaskResponse;
import com.bing.tpa.service.sendRequest.FlaskUploader;
import com.bing.tpa.utils.ReadPPTRemarkUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

//TODO 该类主要处理生成ppt视频前对音频进行推理、获取音频时间
@Component
public class BeforeConvertTask {
    @Autowired
    private InMemoryDataStore globalConfig;

    @Autowired
    private ReadPPTRemarkUtil remarkUtil;

    @Autowired
    private FlaskUploader flaskUploader;

    @Autowired
    private FlaskResponse responseDeal;

    @Autowired
    private ResourceService resource;

//    private final String flaskServerUrl="http://127.0.0.1:5000";3
    @Value("${scheduler.flask.baseurl}")
    private String flaskServerUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     *
     * @param pptPath 为ppt的完整路径
     * @throws JsonProcessingException
     */
//    ****/splitPPTFile/雪之下的猫/aaa.pptx
    public Path BeforeConvert(String pptPath, Integer pptCount,String userName,String pptBaseName) throws IOException, DigitalException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNodes = mapper.createObjectNode();
        ObjectNode jsonNode = jsonNodes.put("User", globalConfig.get("user").toString())
                .put("session_id", globalConfig.get("session_id").toString());
//                .put("ppt_name", pptBaseName)
//                .put("page_num", pptCount);
        String jsonData = mapper.writeValueAsString(jsonNode);
//        提取ppt的批注
        System.out.println("开始制作视频");
        String remarkJson=null;
        try {
            System.out.println(pptPath);
            Map<Integer, List<CommentInfo>> map = remarkUtil.readAllComments(pptPath);
//           将批注转为json串
            remarkJson= ReadPPTRemarkUtil.convertToSortedJson(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(!globalConfig.containsKey("user")) {
            throw new DigitalException("用户没有登录");
        }
        ObjectNode remarkNode = mapper.readValue(remarkJson, ObjectNode.class);
//      TODO 上传ppt批注
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("User", globalConfig.get("user").toString())
                .put("session_id", globalConfig.get("session_id").toString());
//                .put("ppt_name", pptBaseName)
//                .put("page_num", pptCount);

        rootNode.set("PPT_Remakes", remarkNode);
        String pptRemarkJson = mapper.writeValueAsString(rootNode);
        ResponseEntity<String> response = flaskUploader.connectFlask(flaskServerUrl + "/Send_PPT_Remakes", pptRemarkJson);

//      todo 解析出任务id并全局保存，后续所有的接口都需要找到对应的任务id并当做参数传递
              String body = response.getBody();
//              // 将JSON响应转换为JsonNode
              JsonNode responseJson = mapper.readTree(body);
//              // 提取task_id
              String taskId = responseJson.get("task_id").asText();
              if (taskId == null || taskId.isEmpty()) {
                  throw new DigitalException("任务创建失败！");
              }
//              todo 保存任务id到全局
//              pptCount为当前处理的是第几页ppt,后续可以通过"task-"+pptCount获取该页的任务id
              globalConfig.put("task-"+pptCount,taskId);

        if(responseDeal.parseBasicResponse(response)){
            System.out.println("批注上传成功！");
            globalConfig.put("remarkUploader",true);//表示ppt备注已上传
//            TODO 推理音频
            jsonNode.put("task_id",taskId);
            String newJsonData = mapper.writeValueAsString(jsonNode);
            System.out.println("推理音频，任务id为："+taskId);
            ResponseEntity<String> inferenceResponse = flaskUploader.connectFlask(flaskServerUrl + "/Get_Inference_VITS_Wav2Lip", newJsonData);
            if (!responseDeal.parseStateResponse(inferenceResponse,5000, newJsonData, flaskServerUrl + "/Get_State")) {
               throw new DigitalException("音频推理失败");
            }
            System.out.println("音频推理成功");
//            TODO 获取音频时间
//            保存json时间的文件
//            先根据用户名创建该用户的json文件夹
//            String rootDir="src/main/resources/json";
//            Path jsonDir= Paths.get(rootDir,userName+"-json");//   src/main/resources/json/liuc-json/
            Path jsonDir = resource.getResourcePath(ResourceType.JSON, userName + "-json");
            if(!Files.exists(jsonDir)){
                Files.createDirectories(jsonDir);
            }
//****/splitPPTFile/雪之下的猫/aaa.pptx
            Path jsonPath= jsonDir.resolve(FilenameUtils.getBaseName(new File(pptPath).getName())+"-"+pptCount+".json");
//            src/main/resources/json/liuc-json/ppt名字-1.json
            ResponseEntity<String> timeResponse = flaskUploader.connectFlask(flaskServerUrl + "/Recive_Wav_Time", newJsonData);
            if (!responseDeal.flaskResponseJsonSaveWithLogging(timeResponse,jsonPath.toString())) {
              throw new DigitalException("音频时间获取失败");
            }
            return jsonPath;
        }else {
            throw new DigitalException("批注上传失败");
        }
    }


    public FlaskStatus getCurrentStatus(String statusUrl) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(statusUrl, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // 解析整个JSON响应为FlaskStatus对象
                FlaskStatus fullStatus = objectMapper.readValue(response.getBody(), FlaskStatus.class);
//                System.out.println(fullStatus.toString());
                // 适配原有业务字段（根据需要提取关键信息）
                // 1. 健康状态（将"healthy"转为boolean）
                fullStatus.setHealthy("healthy".equals(fullStatus.getStatus()));

                // 2. 提取系统资源（CPU、内存使用率）
                if (fullStatus.getSystem() != null) {
                    fullStatus.setCpuUsage(fullStatus.getSystem().getCpuUsage());
                    fullStatus.setMemoryUsage(fullStatus.getSystem().getMemoryUsage());
                }
                //检查gpu数据
                // 获取GPU信息对象
                Object gpuObj = null;
                if (fullStatus.getSystem() != null) {
                    gpuObj = fullStatus.getSystem().getGpu();
                }

// 第一步：检查是否为Map类型（排除String等错误类型）
                if (gpuObj instanceof Map) {
                    // 第二步：强制转换为Map，并检查泛型（通过unchecked转换抑制警告）
                    @SuppressWarnings("unchecked")
                    Map<String, Object> gpuMap = (Map<String, Object>) gpuObj;

                    // 第三步：检查关键字段的存在性和类型（以utilization为例）
                    if (gpuMap.containsKey("utilization")) {
                        Object utilObj = gpuMap.get("utilization");
                        // 确保字段值是数字类型（Integer、Double等）
                        if (utilObj instanceof Number) {
                           fullStatus.setGpuMap(gpuMap);
                        } else {
                            // 字段存在但类型错误（如字符串），记录警告
                            System.out.println("GPU utilization is not a number: " + utilObj);
                        }
                    } else {
                        // 缺少关键字段，记录警告
                        System.out.println("GPU info missing 'utilization' field");
                    }
                } else {
                    // 非Map类型（如错误字符串），无需处理
                    if (gpuObj != null) {
                        System.out.println("GPU info is not a Map: " + gpuObj.toString());
                    }
                }

                // 3. 提取模型池可用数
                if (fullStatus.getModels() != null) {
                    // VITS可用实例数
                    if (fullStatus.getModels().getVits() != null) {
                        fullStatus.setAvailableVitsModels(fullStatus.getModels().getVits().getAvailable());
                    }
                    // Wav2Lip可用实例数
                    if (fullStatus.getModels().getWav2lip() != null) {
                        fullStatus.setAvailableWav2LipModels(fullStatus.getModels().getWav2lip().getAvailable());
                    }
                }

                // 4. 提取活跃任务数
                if (fullStatus.getTasks() != null) {
                    fullStatus.setActiveTasks(fullStatus.getTasks().getActiveTasks());
                }
                return fullStatus;
            }
        } catch (Exception e) {
            throw new RuntimeException("获取Flask状态失败: " + e.getMessage());
        }
        // 异常时返回默认状态
        FlaskStatus defaultStatus = new FlaskStatus();
        defaultStatus.setHealthy(false);
        return defaultStatus;
    }

}
