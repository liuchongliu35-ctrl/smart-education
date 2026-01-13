package com.bing.tpa.controller;

import com.bing.tpa.Thread.pptVideoThread.DigitalHuman.CommonTask;
import com.bing.tpa.Thread.pptVideoThread.PPTProcess;
import com.bing.tpa.Thread.pptVideoThread.PPTProcess2;
import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.common.ResourceType;
import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.digital.*;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.DigitalException;
import com.bing.tpa.service.baseImpl.ResourceService;
import com.bing.tpa.service.baseService.DigitalService;
import com.bing.tpa.service.baseService.TpaTeachDesignService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.RemarkCheckUtil;
import com.bing.tpa.utils.SplitPPTUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Api(tags = "微课视频生成接口")
@CrossOrigin
@RestController
@RequestMapping("video")
public class DigitalHumanController<T> {

//    private final String flaskServerUrl="http://127.0.0.1:6006";
//    private final String teacherVideoPath="src/main/resources/teacherImageVideo";//保存数字人形象
//    private final String basePPTFilePath="src/main/resources/pptFile";//ppt文件存放根目录
//    String splitPPTRootDir="src/main/resources/splitPPTFile";  //src/main/resources/splitPPTFile

    @Autowired
    private Result<T> result;
    @Autowired
    private TpaTeacherService tpaTeacherService;
    @Autowired
    private CommonTask commonTask;
    @Autowired
    private TpaTeachDesignService tpaTeachDesignService;

    @Autowired
    private PPTProcess pptProcess;
    @Autowired
    private SplitPPTUtil splitPPTUtil;

    @Autowired
    private RemarkCheckUtil remarkCheck;

    @Autowired
    private DigitalService digitalService;

    @Autowired
    private ResourceService  resource;
    @Autowired
    private InMemoryDataStore globalConfig;

    @Autowired
    private PPTProcess2 process;

// @RequestPart("config") Config config
    //    TODO 数字人教学视频制作接口，pptFile为ppt文件，avatar为数字人形象文件
    @ApiOperation("视频制作")
    @PostMapping("make")
    public Result<String> makeVideo(@RequestPart("pptFile") MultipartFile pptFile,
                                    @RequestPart("avatar") MultipartFile avatar,
                                    @RequestPart("config") Config config) throws Exception {
//        Config config = new Config();
//        config.setHasAction(true);
//        config.setVoice("1");
//        config.setTdId(56);
//        config.setFaceEnhance(false);
//        config.setAiAnnotation(false);
//        config.setActionEnhance(true);
//        根据tdId获取对应的教学设计
        globalConfig.put("progress", 0.0);//初始化进度条为0
        globalConfig.put("status","processing");

        if (config.getTdId() <= 0) {
            return result.fail(null, "关键参数不能无效");
        }
        TpaTeachDesign design = tpaTeachDesignService.getById(config.getTdId());
//        获取用户信息
        TpaTeacher user = tpaTeacherService.getCurrentUser();
//      todo 开始合成视频
//        记录下状态
        globalConfig.put(user.getAccount(), true);//标记该用户正在生成视频！！！，true表示该用户正在制作视频，false表示没有制作视频
//       todo 1、先处理ppt文件，因为需要检查是否所有页面都有批注，如果传过来的ppt有页面没有批注就返回false

//        todo 根据tdId获取本地的ppt文件


        Path pptRootPath = resource.getResourcePath(ResourceType.PPT, user.getAccount() + "-ppt"); //使用资源管理器获取ppt的根路径
        String pptName = user.getUid() + "--" + design.getTdId() + "--" + design.getDesignName() + ".pptx";
        Path pptPath = pptRootPath.resolve(pptName);
        // 确保PPT目录存在
        if (!Files.exists(pptRootPath)) {//确保该用户储存ppt的目录存在
            Files.createDirectories(pptRootPath);
        }

//        todo 1.1处理PPT覆盖逻辑，即更新原有ppt
        if (!config.isAiAnnotation()) {
            // 覆盖原PPT
            try {
                Files.copy(pptFile.getInputStream(), pptPath, StandardCopyOption.REPLACE_EXISTING);
//                resource.saveResource(ResourceType.PPT, user.getAccount() + "-ppt/" + pptName, pptFile.getInputStream());
                System.out.println("已使用上传PPT覆盖原PPT文件");
            } catch (IOException e) {
                globalConfig.put(user.getAccount(), false);//不该你是什么原因导致的，都需要将状态重置
                globalConfig.put("status","fail");
                return result.fail(null, "PPT文件覆盖失败: " + e.getMessage());
            }
        } else {
            // 检查原PPT是否存在
            if (!resource.existsResource(ResourceType.PPT, user.getAccount() + "-ppt/" + pptName)) {
                globalConfig.put(user.getAccount(), false);
                globalConfig.put("status","fail");
                return result.fail(null, "原PPT文件不存在，请上传PPT文件或禁用AI批注");
            }
            System.out.println("使用本地原PPT文件");
        }
//      最终确认PPT文件存在
        if (!Files.exists(pptPath)) {
            globalConfig.put(user.getAccount(), false);
            globalConfig.put("status","fail");
            return result.fail(null, "PPT文件不存在，处理过程中发生意外错误");
        }
//      todo 安全检查批注
        boolean check = remarkCheck.check(pptPath.toString());
        if (!check) {
            globalConfig.put(user.getAccount(), false);
            globalConfig.put("status","fail");
            return result.fail(null, "选择的PPT不具有完整的授课词");
        }


//       todo 2、保存数字人视频并调用登录数字人制作平台的方法
        Path avatarTempPath;
        try {
            Path uploadPath = resource.getResourcePath(ResourceType.TeacherVideo, "");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            // 生成唯一文件名，临时文件
            String originalFilename = avatar.getOriginalFilename();
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String timeName = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String uniqueFilename = timeName + "_avatar." + fileExtension;
//          数字人形象视频临时保存，使用完后就删掉
            avatarTempPath = uploadPath.resolve(uniqueFilename);
            // 保存文件
            Files.copy(avatar.getInputStream(), avatarTempPath, StandardCopyOption.REPLACE_EXISTING);

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("Test");
            loginRequest.setPassword("123000");
            BaseSetupInfo baseSetupInfo = new BaseSetupInfo();
            baseSetupInfo.setGender(config.getVoice());
            baseSetupInfo.setEnhancer(config.isFaceEnhance());
            int motion;
            if (config.isHasAction()) motion = 2;//如果需要动作就是true，就是2
            else motion = 1;//不需要动作就是false，就是1
            baseSetupInfo.setDigitalMotion(motion);
//        todo 登录并配置，后续不必登录
            commonTask.loginAndSetup(loginRequest, baseSetupInfo, avatarTempPath.toString());
        } catch (IOException e) {
            globalConfig.put(user.getAccount(), false);//出错就重置状态
            globalConfig.put("status","fail");
            throw new DigitalException("无法进行登录配置，视频制作失败");
        }


//        todo 3、处理已保存的ppt文件
//        todo 3.1 将ppt修改名字后保存到指定的临时路径
//        创建临时目录
        Path pptTempRoot = pptRootPath.resolve("temp");
//        在该用户的ppt文件夹中创建一个temp临时目录，存放临时命名的ppt
        if (!Files.exists(pptTempRoot)) {
            Files.createDirectory(pptTempRoot);
        }
        // 2. 获取原PPT文件名和扩展名
        File originalFile = new File(pptPath.getFileName().toString());
        String baseName = FilenameUtils.getBaseName(originalFile.getName());
        String extension = FilenameUtils.getExtension(originalFile.getName());
        // 3. 生成临时文件名（使用时间戳）
        String timeName = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String tempFileName = timeName + "_temp" + "." + extension;//如：20250713153045_temp.pptx
        Path tempFilePath = pptTempRoot.resolve(tempFileName);// todo 临时ppt的路径
        // 4. 复制原PPT到临时目录并重命名
        Files.copy(pptPath, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("临时ppt文件已创建：" + tempFilePath);

//        todo 3.2 调用ppt拆分的线程池，将临时ppt文件传过去
        CompletableFuture<Integer> future = splitPPTUtil.splitPptByPages(tempFilePath.toString(), resource.getResourcePath(ResourceType.SPLITPPT, "").toString(), user.getAccount());
        int pptCount = future.get(5, TimeUnit.MINUTES);
//        已完成ppt读取
//        已完成ppt上传
//        已完成ppt视频化
//        正在融合视频
//        todo 4、进行视频融合与重组
        process.batchConvertToVideo(
                resource.getResourcePath(ResourceType.SPLITPPT, "").toString(),//  ****/splitPPTFile/
                user.getAccount(),
                FilenameUtils.getBaseName(tempFilePath.getFileName().toString()),
                pptCount, baseName + ".mp4", avatarTempPath.toString());//将ppt的原名字传过去为后续拼接视频做准备
        return result.success("ok");
//         process.batchConvertToVideo(
//                resource.getResourcePath(ResourceType.SPLITPPT,"").toString(),//  ****/splitPPTFile/
//                "雪之下的猫",
//                FilenameUtils.getBaseName(tempFilePath.getFileName().toString()),
//                pptCount,baseName+".mp4","src/main/resources/videoFile/Video.mp4");
//    }
    }


    /**
     *测试视频生成接口
     */
//    @ApiOperation("视频制作")
//    @PostMapping("make")
//    public Result<String> makeVideo(@RequestPart("pptFile") MultipartFile pptFile,
//                                    @RequestPart("avatar") MultipartFile avatar) throws Exception {
//        globalConfig.put("progress", 0.0);
//        TpaTeacher user = tpaTeacherService.getCurrentUser();
////        globalConfig.put(user.getAccount(), true);
//        globalConfig.put("status","processing");
//        try {
//            System.out.println("开始视频生成");
//            Thread.sleep(5000);
//            globalConfig.put("progress", 0.22);
//            Thread.sleep(10000);
//            globalConfig.put("progress", 0.42);
//            Thread.sleep(5000);
//            globalConfig.put("progress", 0.52);
//            Thread.sleep(5000);
//            globalConfig.put("progress", 0.62);
//            Thread.sleep(5000);
//            globalConfig.put("progress", 0.72);
//            Thread.sleep(5000);
//            globalConfig.put("progress", 0.82);
//            Thread.sleep(10000);
//            globalConfig.put("progress", 0.92);
//            Thread.sleep(5000);
//            globalConfig.put("progress", 1.00);
//            globalConfig.put("status","success");
//        } catch (InterruptedException e) {
//            globalConfig.put("status","fail");
//        }
//        return result.success("ok");
//    }

    /**
     * @param tdId 教学设计id，因为一个教学设计对应一个ppt，对应一个教学视频
     * @return
     */
    @ApiOperation("AI生成ppt批注")
    @GetMapping("remark")
    public Result<String> generatePPTRemark(@RequestParam Integer tdId){

        return null;
    }

//    @ApiOperation("监控视频制作状态")
//    @GetMapping("status")
//    public Result<Double> getStatus(){
//        TpaTeacher user = tpaTeacherService.getCurrentUser();
//        if(globalConfig.get(user.getAccount())==null){
//            return result.build(0.0,ResultCodeEnum.SUCCESS);//如果是null表示还未初始化，制作状态为0%
//        }
////        globalConfig.put(user.getAccount(), true);
//        Boolean status = (Boolean) globalConfig.get(user.getAccount());
//        if(status){//状态是true表示正在制作，只有制作时才会返回进度条
////            获取进度条
//            Double progress = (Double)globalConfig.get("progress");//设置进度调
//            System.out.println("当前进度："+progress);
//            DecimalFormat df = new DecimalFormat("#.00");
//            String formattedProgress = df.format(progress);
//            double progressValue = Double.parseDouble(formattedProgress);
//            return result.build(progressValue*100,ResultCodeEnum.SUCCESS);
//        }
////        状态是false表示当前没有制作视频，则返回0
//        return result.build(null,ResultCodeEnum.FAIL);
//    }
@ApiOperation("监控视频制作状态")
@GetMapping("status")
public Result<StatusVo> getStatus() {
    StatusVo statusVo = new StatusVo();

    // 增加全局配置对象空值判断
    if (globalConfig == null) {
        statusVo.setStatus(null);
        statusVo.setProcess(0.0);
        return result.success(statusVo);
    }

    // 从全局配置中获取状态和进度
    String status = null;
    Double progress = 0.0;
    if (globalConfig.get("status") != null && globalConfig.get("progress") != null) {
        status = (String) globalConfig.get("status");
        progress = (Double) globalConfig.get("progress");
    }

    // 情况1：状态为空，返回null
    if (status == null) {
        statusVo.setStatus(null);
        statusVo.setProcess(0.0);
        return result.success(statusVo);
    }

    // 处理进度格式化
    DecimalFormat df = new DecimalFormat("#.00");

    // 情况2：制作失败，返回失败状态并清除配置
    if ("fail".equals(status)) {
        statusVo.setStatus("fail");
        statusVo.setProcess(null);

        // 清除全局配置中的相关参数
        return result.success(statusVo);
    }

    // 情况3：制作完成，返回成功状态并清除配置
    if ("success".equals(status)) {
        statusVo.setStatus("success");
        String formattedProgress = df.format(progress * 100);
        statusVo.setProcess(Double.parseDouble(formattedProgress));

        // 清除全局配置中的相关参数
        return result.success(statusVo);
    }

    // 情况4：正在制作中，返回当前进度
    statusVo.setStatus("processing");
    String formattedProgress = df.format(progress * 100);
    statusVo.setProcess(Double.parseDouble(formattedProgress));

    return result.success(statusVo);
}


    /**
     * 根据视频地址
     * @param videoUrl 视频储存位置
     */
//    @ApiOperation("根据地址获取视频文件")
//    @GetMapping("mp4")
//    public void getVideo(@RequestParam String videoUrl, HttpServletResponse response,HttpServletRequest request) throws DigitalException {
//      File videoFile =new File(videoUrl);
//      if(!videoFile.exists()){
//          throw new DigitalException("视频不存在");
//      }
//      String range=request.getHeader("Ranger");
//      long start=0;
//      long end=videoFile.length()-1;
//      if(range!=null&&range.contains("bytes=")&&range.contains("-")){
//          range=range.substring(range.lastIndexOf("=")+1).trim();
//          String [] ranges=range.split("-");
//          try {
//              if(ranges.length==1){
//                  if(range.startsWith("-")){
//                      end=Long.parseLong(ranges[0]);
//                  }else if(range.endsWith("-")){
//                      start=Long.parseLong(ranges[0]);
//                  }
//              }else if(ranges.length==2){
//                  start=Long.parseLong(ranges[0]);
//                  end=Long.parseLong(ranges[1]);
//              }
//          } catch (NumberFormatException e) {
//             start=0;
//             end=videoFile.length()-1;
//          }
//      }
//      /*要下载的长度*/
//        long contentLength=end-start+1;
//        String videoName=videoFile.getName();
////       文件类型
//        String videoType=request.getServletContext().getMimeType(videoName);
////        准备响应头
//        response.setHeader("Accept-Ranges", "bytes");
//        response.setStatus(206);
//        response.setContentType(videoType);
//        response.setHeader("Content-Type",videoType);
//        response.setHeader("Content-Disposition","inline;filename=digital.mp4");
//        response.setHeader("Content-Length",String.valueOf(contentLength));
////        设置Content-Range
//        response.setHeader("Content-Range","bytes "+start+"-"+end+"/"+videoFile.length());
//
////        开始发送文件
//        BufferedOutputStream bos=null;
//        RandomAccessFile raf=null;
//        long transmitted=0;
//
//        try {
//            raf=new RandomAccessFile(videoFile,"r");
//            bos=new BufferedOutputStream(response.getOutputStream());
//
////
//            byte[] buffer=new byte[1024];
//            int len=0;
//            raf.seek(start);
//            while ((transmitted + len) <= contentLength && (len=raf.read(buffer)) != -1){
//                bos.write(buffer,0,len);
//                transmitted+=len;
////                Thread.sleep(100);
//            }
//
//            if (transmitted<contentLength){
//                len=raf.read(buffer,0,(int) (contentLength-transmitted));
//                bos.write(buffer,0,len);
//                transmitted+=len;
//            }
//            bos.flush();
//            response.flushBuffer();
//            raf.close();
//
//        } catch (ClientAbortException e) {
//            System.out.println("用户停止下载"+start+"-"+end+":"+transmitted);
//        }catch (IOException e){
//            e.printStackTrace();
//        } finally {
//            try {
//                if(raf!=null){
//                    raf.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    @ApiOperation("根据地址获取视频文件")
    @GetMapping("mp4")
    public void getVideo(@RequestParam String videoUrl,
                         HttpServletResponse response,
                         HttpServletRequest request)
            throws DigitalException, IOException {

        // 安全校验路径
        if (videoUrl.contains("..") || videoUrl.startsWith("/")) {
            throw new DigitalException("非法路径");
        }

        // 获取资源路径
        Path videoPath = resource.getResourcePath(ResourceType.VIDEO, videoUrl);

        if (!Files.exists(videoPath)) {
            throw new DigitalException("视频不存在");
        }

        long fileSize = Files.size(videoPath);
        String fileName = videoPath.getFileName().toString();

        // 处理Range请求
        String rangeHeader = request.getHeader("Range");
        long start = 0;
        long end = fileSize - 1;
        long contentLength = fileSize;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String range = rangeHeader.substring(6);
            String[] ranges = range.split("-");
            try {
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Math.min(Long.parseLong(ranges[1]), fileSize - 1);
                }
                contentLength = end - start + 1;
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            } catch (NumberFormatException e) {
                // 忽略无效Range头
            }
        }

        // 使用RFC 5987标准设置文件名
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        String contentDisposition = "inline; filename*=UTF-8''" + encodedFileName;

        // 设置响应头
        response.setContentType("video/mp4");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", contentDisposition);
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);

        // 使用NIO进行高效文件传输
        try (OutputStream os = response.getOutputStream();
             FileChannel channel = FileChannel.open(videoPath, StandardOpenOption.READ)) {

            long transferred = channel.transferTo(start, contentLength, Channels.newChannel(os));
            System.out.println("已传输字节: " + transferred);
        } catch (ClientAbortException e) {
            System.out.println("用户中断下载");
        }
    }
//    @ApiOperation("根据地址获取视频文件")
//    @GetMapping("mp4")
//    public void getVideo(@RequestParam String videoUrl, HttpServletResponse response, HttpServletRequest request) throws DigitalException, IOException {
//        Path videoPath = resource.getResourcePath(videoUrl);
//        File videoFile = videoPath.toFile();
//        if (!videoFile.exists()) {
//            throw new DigitalException("视频不存在");
//        }
//
//        // 处理Range请求（解析start和end）
//        String range = request.getHeader("Range");
//        long start = 0;
//        long end = videoFile.length() - 1;
//        if (range != null && range.contains("bytes=") && range.contains("-")) {
//            range = range.substring(range.lastIndexOf("=") + 1).trim();
//            String[] ranges = range.split("-");
//            try {
//                if (ranges.length == 1) {
//                    if (range.startsWith("-")) {
//                        end = Long.parseLong(ranges[0]);
//                    } else if (range.endsWith("-")) {
//                        start = Long.parseLong(ranges[0]);
//                    }
//                } else if (ranges.length == 2) {
//                    start = Long.parseLong(ranges[0]);
//                    end = Long.parseLong(ranges[1]);
//                    // 防止end超过文件实际长度
//                    end = Math.min(end, videoFile.length() - 1);
//                }
//            } catch (NumberFormatException e) {
//                start = 0;
//                end = videoFile.length() - 1;
//            }
//        }
//
//        // 计算应传输的总字节数（必须严格等于Content-Length）
//        long contentLength = end - start + 1;
//        String videoName = videoFile.getName();
//        String videoType = request.getServletContext().getMimeType(videoName);
//
//        // 设置响应头（严格匹配传输的字节数）
//        response.setHeader("Accept-Ranges", "bytes");
//        response.setStatus(206); // 部分内容响应
//        response.setContentType(videoType);
//        response.setHeader("Content-Disposition", "inline;filename=digital.mp4");
//        response.setHeader("Content-Length", String.valueOf(contentLength));
//        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + videoFile.length());
//        response.setHeader("Content-Type", "video/mp4; codecs=\"avc1.42E01E, mp4a.40.2\"");
//
//        long transmitted = 0; // 已传输字节数
//        // 传输文件（核心修正部分）
//        try (RandomAccessFile raf = new RandomAccessFile(videoFile, "r");
//             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
//
//            raf.seek(start); // 定位到起始位置
//            byte[] buffer = new byte[1024];
//
//
//            // 循环传输：每次读取不超过剩余字节数，确保总传输量=contentLength
//            while (transmitted < contentLength) {
//                // 计算剩余需要传输的字节数（避免读取超过实际需要的内容）
//                long remaining = contentLength - transmitted;
//                // 每次最多读取buffer大小或剩余字节数（取较小值）
//                int readLength = (int) Math.min(buffer.length, remaining);
//                int len = raf.read(buffer, 0, readLength);
//
//                if (len == -1) {
//                    break; // 读取完毕
//                }
//                bos.write(buffer, 0, len);
//                transmitted += len;
//
//                // 强制刷新缓冲区（关键：确保数据及时发送，不残留）
//                bos.flush();
//            }
//
//            // 最终刷新，确保所有数据发送
//            bos.flush();
//            response.flushBuffer();
//            System.out.println("视频下载完毕"+start+"-"+end+":"+transmitted);
//        } catch (ClientAbortException e) {
//            System.out.println("用户中断下载：已传输" + transmitted + "字节");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//    @ApiOperation("根据地址获取视频文件")
//    @GetMapping("mp4")
//    public void getVideo(@RequestParam String videoUrl, HttpServletResponse response, HttpServletRequest request) throws DigitalException {
//        File videoFile = new File(videoUrl);
//        if (!videoFile.exists()) {
//            throw new DigitalException("视频不存在");
//        }
//
//        // 1. 正确处理标准Range头
//        String rangeHeader = request.getHeader("Range"); // 修正拼写
//        long fileLength = videoFile.length();
//        long start = 0;
//        long end = fileLength - 1;
//
//        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
//            String range = rangeHeader.substring(6).trim();
//            String[] ranges = range.split("-");
//            try {
//                start = Long.parseLong(ranges[0]);
//                if (ranges.length > 1 && !ranges[1].isEmpty()) {
//                    end = Long.parseLong(ranges[1]);
//                } else {
//                    end = fileLength - 1;
//                }
//            } catch (NumberFormatException e) {
//                start = 0;
//                end = fileLength - 1;
//            }
//        }
//
//        long contentLength = end - start + 1;
//        String videoName = videoFile.getName();
//        String contentType = request.getServletContext().getMimeType(videoName);
//
//        // 2. 设置响应头
//        response.setHeader("Accept-Ranges", "bytes");
//        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206
//        response.setContentType(contentType);
//        response.setHeader("Content-Length", String.valueOf(contentLength));
//        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
//
//        // 3. 使用大缓冲区并移除休眠
//        try (RandomAccessFile raf = new RandomAccessFile(videoFile, "r");
//             OutputStream os = response.getOutputStream()) {
//
//            raf.seek(start);
//            byte[] buffer = new byte[4096 * 16]; // 64KB缓冲区 ✅
//            long remaining = contentLength;
//
//            while (remaining > 0) {
//                int read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining));
//                if (read == -1) break;
//                os.write(buffer, 0, read);
//                remaining -= read;
//            }
//        } catch (ClientAbortException e) {
//            // 客户端主动断开连接，无需处理
//        } catch (IOException e) {
//            throw new DigitalException("视频传输失败");
//        }
//    }



    @ApiOperation("获取所有视频列表")
    @GetMapping("videoList")
    public Result<List<VideoListVo>> getVideoList() throws IOException {
        List<VideoListVo> videoList = digitalService.getVideoList();
        if (videoList.isEmpty()) return result.build(null,"405","暂无视频资源");
        return result.build(videoList,ResultCodeEnum.SUCCESS);
    }

    @ApiOperation("根据知识点获取相关的视频列表")
    @GetMapping("video-point")
    public Result<List<VideoListVo>> getVideoByKnowledge(@RequestParam String title) throws IOException {
        List<VideoListVo> videoByKnowledge = digitalService.getVideoByKnowledge(title);
        if (videoByKnowledge.isEmpty()) return result.build(null,"405","未找到相关视频资源");
        return result.build(videoByKnowledge,ResultCodeEnum.SUCCESS);
    }
//    pat_fJXS14Ols8zjwU5MSsE2QzjiLEA6VqFhicVgxqKc0NtXXcJBHkAE21moYWG8j0LE

}
