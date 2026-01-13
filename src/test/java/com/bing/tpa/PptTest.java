//package com.bing.tpa;
//import com.bing.tpa.Thread.pptVideoThread.DigitalHuman.CommonTask;
//import com.bing.tpa.Thread.pptVideoThread.PPTProcess;
//import com.bing.tpa.Thread.pptVideoThread.PPTProcess2;
//import com.bing.tpa.domain.digital.BaseSetupInfo;
//import com.bing.tpa.domain.digital.LoginRequest;
//import com.bing.tpa.utils.*;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FilenameUtils;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@SpringBootTest
//public class PptTest {
//    @Autowired
//    private PPTProcess pptProcess;
//    @Autowired
//    private SplitPPTUtil splitPPTUtil;
//
//    @Autowired
//    private CommonTask commonTask;
//
//    @Test
//    public void convertToVideoTest() throws Exception {
//        String pptPath = "src/main/resources/pptFile/17--5512ppt.pptx";
//        String splitPPTRootDir = "src/main/resources/splitPPTFile";
//        String name = "liuc";
//        String pptRootPath = "src/main/resources/pptFile";
//
////        TODO 在该用户下创建临时ppt目录,将ppt改名字后存放在这个临时目录下
//        Path pptTempRoot = Paths.get(pptRootPath, name + "-ppt");
////        在该用户的ppt文件夹中创建一个临时目录，存放临时命名的ppt
//        if (!Files.exists(pptTempRoot)) {
//            Files.createDirectory(pptTempRoot);
//        }
//        // 2. 获取原PPT文件名和扩展名
//        File originalFile = new File(pptPath);
//        String baseName = FilenameUtils.getBaseName(originalFile.getName());
//        String extension = FilenameUtils.getExtension(originalFile.getName());
//        // 3. 生成临时文件名（添加时间戳避免冲突）
//        String timeName = LocalDateTime.now()
//                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//        String tempFileName = timeName + "_temp" + "." + extension;//如：20250713153045_temp.pptx
////        String tempFileName = baseName + "_temp"+ "." + extension;//如：17--55--认识人工智能_temp.pptx
//        Path tempFilePath = pptTempRoot.resolve(tempFileName);// todo 临时ppt的路径
//
//        // 4. 复制原PPT到临时目录并重命名
//        Files.copy(originalFile.toPath(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
//        System.out.println("临时文件已创建：" + tempFilePath);
//
////        TODO 拆分ppt
//        CompletableFuture<Integer> future = splitPPTUtil.splitPptByPages(tempFilePath.toString(), splitPPTRootDir, name);
//        int pptCount = future.get(5, TimeUnit.MINUTES);
////        Thread.sleep(100000);
////        多线程转ppt为视频的线程
//        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setUsername("Test");
//        loginRequest.setPassword("123000");
//        BaseSetupInfo baseSetupInfo = new BaseSetupInfo();
//        baseSetupInfo.setGender("1");
//        baseSetupInfo.setEnhancer(false);
//        baseSetupInfo.setDigitalMotion(2);
////        TODO 先登录和配置数字人
//        commonTask.loginAndSetup(loginRequest, baseSetupInfo, "src/main/resources/teacherImageVideo/Video1.mp4");
////        TODO 视频生成
//        pptProcess.batchConvertToVideo(
//                splitPPTRootDir,
//                name,
//                FilenameUtils.getBaseName(new File(tempFilePath.toString()).getName()),//17--5512ppt_temp
//                pptCount, originalFile.getName()
//        );
////        TODO 视频合成线程池
//
////        future1.get(10, TimeUnit.MINUTES);
//    }
//}
//////    public static void main(String[] args) throws Exception {
////////        TODO ppt转视频测试,使用该方案
////////        String FilePath = "src/main/resources/pptFile/17--55testppt.pptx";
////////        File pptFile = new File(FilePath);
////////        String videoPath = "src/main/resources/videoFile/"+pptFile.getName();
////////        ConvertToVideoThread convertToVideoThread = new ConvertToVideoThread();
////////        convertToVideoThread.convertToVideo(FilePath,videoPath);
////////        String pptPath="src/main/resources/pptFile/17--55认识人工智能11ppt.pptx";
////////        String rootDir="src/main/resources/SplitPPTFile";
////////        String name="liuc";
////////        Integer pptCount = SplitPPTUtil.splitPptByPages(pptPath, rootDir, name);
////////
////////
//////////        多线程转ppt为视频
////////        PPTProcess.batchConvertToVideo(rootDir,name, FilenameUtils.getBaseName(new File(pptPath).getName()),pptCount);
////////        PptToVideoUtil util = new PptToVideoUtil();
////////        String jsonFilePath = "src/main/resources/json/认识人工智能11-json.json"; // JSON文件路径
////////
////////        try {
////////            // 1. 从文件中读取JSON数据
////////            String durationsJson = JsonFileReader.readJsonFile(jsonFilePath);
////////            System.out.println("从文件加载的JSON数据：");
////////            System.out.println(durationsJson);
////////
////////            // 2. 调用PPT转视频方法
////////            util.pptUtils("src/main/resources/pptFile/雪之下的猫-ppt/17--55认识人工智能112ppt.pptx", "src/main/resources/videoFile/认识人工智能2.mp4", durationsJson);
////////
////////        } catch (IOException e) {
////////            System.err.println("读取JSON文件失败: " + e.getMessage());
////////            e.printStackTrace();
////////        }
//////
////////        TODO 读取ppt批注和添加批注
////////        ReadPPTRemarkUtil readPPTRemarkUtil = new ReadPPTRemarkUtil();
////////        Map<Integer, List<CommentInfo>> map = readPPTRemarkUtil.readAllComments("src/main/resources/pptFile/reportbz.pptx");
//////////        for (Map.Entry<Integer, List<CommentInfo>> entry : map.entrySet()) {
//////////            System.out.println(entry.getKey());
//////////            for (CommentInfo commentInfo : entry.getValue()) {
//////////                System.out.println(commentInfo.getText());
//////////            }
////////        String remarkJson = ReadPPTRemarkUtil.convertToSortedJson(map);
////////        ObjectMapper mapper = new ObjectMapper();
////////        ObjectNode remarkNode = mapper.readValue(remarkJson, ObjectNode.class);
////////
////////        ObjectNode rootNode = mapper.createObjectNode();
////////        rootNode.put("User", "Test");
////////        rootNode.set("PPT_Remakes", remarkNode);
////////        String pptRemarkJson = mapper.writeValueAsString(rootNode);
////////        System.out.println(pptRemarkJson);
//////////        readPPTRemarkUtil.readPPTRemark();
//////////        readPPTRemarkUtil.addPPTRemark();
////////        PptToVideoUtil util = new PptToVideoUtil();
////////        String jsonFilePath = "src/main/resources/Data/认识人工智能11-json.json"; // JSON文件路径
////////
////////        try {
////////            // 1. 从文件中读取JSON数据
////////            String durationsJson = JsonFileReader.readJsonFile(jsonFilePath);
////////            System.out.println("从文件加载的JSON数据：");
////////            System.out.println(durationsJson);
////////           JsonUtils.sortAndFormatJson(durationsJson);
//////////            System.out.println(string);
////////        }catch (Exception e){
////////        e.printStackTrace();}
//////    }
////}
