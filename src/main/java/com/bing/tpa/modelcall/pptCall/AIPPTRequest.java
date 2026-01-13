package com.bing.tpa.modelcall.pptCall;

import com.alibaba.fastjson.JSONObject;
import com.bing.tpa.common.ResourceType;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.service.baseImpl.ResourceService;
import com.bing.tpa.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class AIPPTRequest {
   // 填写你的API-KEY  ak_s_ABnW53ssr36Jehya
//   可使用的：todo
//    ak_RMcI3pTr5ssrsP2kOH
//    ak_RKcGT5E53r33p7TTAu
//    ak_R_c0E3E3s55TsPsqhZ
   private final String apiKey = "ak_R_c0E3E3s55TsPsqhZ";
   private final String uid = "pptGenerate";
//   private final String baseSavePath = "pptFile";
   @Autowired
   private ResourceService resource;
//   TODO src/main/resources/pptFile/用户名-ppt/教师id+“--”+教学设计id+教学设计名字ppt.pptx
//   TODO String pptFilePath=basePPTFilePath + user.getAccount()+ "-ppt/"+user.getUid()+"--"+tdId+design.getDesignName()+"ppt.pptx";

   /**
    * 生成ppt的工具
    *
    * @param teacher
    * @param design
    * @param subject
    * @return
    * @throws Exception
    */
   public String generatePPT(TpaTeacher teacher, TpaTeachDesign design, String subject) throws Exception {
       subject += ",内容只能与指定的主题相关";
//      String folder = baseSavePath + teacher.getAccount() + "-ppt/";//src/main/resources/pptFile/雪之下的猫-ppt
//      Path folderPath = Paths.get(baseSavePath,teacher.getAccount() + "-ppt/");
////      如果这个目录不存在就创建
//      if (!Files.exists(folderPath)) {
//         Files.createDirectory(folderPath);
//      }
//      String pptSavePath = folder + teacher.getUid() + "--" + design.getTdId() + "--" + design.getDesignName() + ".pptx";
       // 构建相对路径
       String relativePath = teacher.getAccount() + "-ppt/" +
               teacher.getUid() + "--" +
               design.getTdId() + "--" +
               design.getDesignName() + ".pptx";
       //src/main/resources/pptFile/雪之下的猫-ppt/4--11--认识人工智能.pptx
//      uid+"--"+tdId+"--"+designName+".pptx";


       // 创建 api token (有效期2小时，建议缓存到redis，同一个 uid 创建时之前的 token 会在10秒内失效)
       String apiToken = API.createApiToken(apiKey, uid, null);
       System.out.println("api token: " + apiToken);

       // 生成大纲
       System.out.println("\n\n========== 正在生成大纲 ==========");
       String outline = API.generateOutline(apiToken, subject, null, null);

       // 生成大纲内容
       System.out.println("\n\n========== 正在生成大纲内容 ==========");
       String markdown = API.generateContent(apiToken, outline, null, null);

       // 随机一个模板
       System.out.println("\n\n========== 随机选择模板 ==========");
       String templateId = API.selectOneTemplateId(apiToken);//获取商务科技风的模版
       System.out.println(templateId);

       // 生成PPT
       System.out.println("\n\n========== 正在生成PPT ==========");
       JSONObject pptInfo = API.generatePptx(apiToken, templateId, markdown, false);
       String pptId = pptInfo.getString("id");
       System.out.println("pptId: " + pptId);
       System.out.println("ppt主题：" + pptInfo.getString("subject"));
       System.out.println("ppt封面：" + pptInfo.getString("coverUrl") + "?token=" + apiToken);

       // 下载PPT到桌面
       System.out.println("\n\n========== 正在下载PPT ==========");
       JSONObject result = API.downloadPptx(apiToken, pptId);
       String url = result.getString("fileUrl");
       System.out.println("ppt链接：" + url);
//      String savePath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + pptId + ".pptx";
       Path tempFile = Files.createTempFile("ppt", ".pptx");
       HttpUtils.download(url, tempFile.toFile());
       // 使用ResourceService保存到统一存储
       Path pptSavePath;
       try (InputStream in = Files.newInputStream(tempFile)) {
           Path pptRootPath = resource.getResourcePath(ResourceType.PPT, "");
           pptSavePath = pptRootPath.resolve(relativePath);
           resource.saveResource(pptSavePath.toString(), in);
       } finally {
           Files.deleteIfExists(tempFile);
       }
       System.out.println("资源保存路径：" + pptSavePath.toString());
       return pptSavePath.toString();
   }
}

