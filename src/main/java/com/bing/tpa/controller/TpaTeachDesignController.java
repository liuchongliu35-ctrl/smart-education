package com.bing.tpa.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.common.ResourceType;
import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.*;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.RedisException;
import com.bing.tpa.service.baseImpl.ResourceService;
import com.bing.tpa.service.baseService.TpaTeachDesignService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.FileSizeUtil;
import com.bing.tpa.utils.RedisConstants;
import io.github.lnyocly.ai4j.service.factor.AiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
//教学设计模块
@Api(tags = "教学设计接口")
@CrossOrigin
@RestController
@RequestMapping("teachDesign")
public class TpaTeachDesignController<T> {
    @Resource
    private Result<T> result;

    @Resource
    private TpaTeachDesignService teachDesignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AiService aiService;
    @Autowired
    private ResourceService resource;
    @Autowired
    private TpaTeacherService tpaTeacherService;

    @Autowired
    private InMemoryDataStore globalConfig;
    /**
     * 异步相应数据，先获得教学设计的两个主题，教学科目三个信息，以此来预处理参考数据
     * 同时创建一条教学设计数据，并将本教学设计的id作为key，储存到redis中
     * 将教学设计的id进行返回
     */
    @ApiOperation("新建教学设计")
    @PostMapping("")
    public Result<Integer> addAndPrepareData(@RequestBody TpaTeachDesign teachDesign) throws RedisException {
//        返回教学设计的id
        Integer tdId = teachDesignService.addAndPrepare(teachDesign);
        if(tdId==null) return result.build(null, ResultCodeEnum.FAIL);
        if(tdId==-1) return result.build(null,"403","该教学设计已在redis中，不可以进行添加！");
        return result.build(tdId,ResultCodeEnum.SUCCESS);
    }

    /**
     * 从redis中获取及时的文本数据
     */
    @ApiOperation("从redis中获取教学设计内容")
    @GetMapping("textFromRedis/{tdId}")
    public Result<String> textFromRedis(@PathVariable Integer tdId){
//        根据key从redis中获取数据
        Map<Object, Object> designMap = stringRedisTemplate.opsForHash().entries(RedisConstants.DESIGN_ID_KEY + tdId.toString());
        RedisDesign redisDesign = BeanUtil.fillBeanWithMap(designMap, new RedisDesign(), false);
        return result.build(redisDesign.getContent(),ResultCodeEnum.SUCCESS);
    }

//    根据教学设计Id删除教学设计
    @ApiOperation("根据td_id删除教学设计")
    @DeleteMapping("remove/{tdId}")
    public Result<T> deleteDesign(@PathVariable String tdId){
        boolean remove = teachDesignService.removeById(tdId);
        if(!remove) return result.build(null,ResultCodeEnum.FAIL);
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

//    根据教学设计id获取历史教学设计

    /**
     * 根据教学设计的id获取一个教学设计的详细数据，进入编辑页面后就会有一个历史互动环节的按钮，点击就会获得该教学设计使用的互动数据，
     * 这里互动数据调用TpaInteraction接口获得
     * 此外还有一个生成新互动环节的按钮
     * @param designId
     * @return
     */
    @ApiOperation("根据id获取历史教学设计")
    @GetMapping("byId/{designId}")
    public Result<TpaTeachDesign> getById(@PathVariable String designId){
//        以redis中过的数据为主
        TpaTeachDesign oneDesign=new TpaTeachDesign();
        Map<Object, Object> design = stringRedisTemplate.opsForHash().entries(RedisConstants.DESIGN_ID_KEY + designId);
        System.out.println(design.get("content")=="");
        if (!design.isEmpty()&&design.get("content") != null && !design.get("content").equals("")) {
            RedisDesign redisDesign = BeanUtil.fillBeanWithMap(design, new RedisDesign(), false);
            oneDesign.setContent(redisDesign.getContent());
            oneDesign.setDesignTitle(redisDesign.getDesignTitle());
            oneDesign.setDesignName(redisDesign.getDesignName());
            oneDesign.setSecondaryTitle(redisDesign.getSecondaryTitle());
            System.out.println("从redis拿数据");
        }else {
//            如果redis中最新的数据为空什么已经写到数据库中了，就到数据库中拿
            System.out.println("从数据库拿数据");
            QueryWrapper<TpaTeachDesign> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("td_id",designId);
            oneDesign = teachDesignService.getOne(queryWrapper);
        }
        if (oneDesign.getContent()==null) return result.fail(null,"教学设计获取失败");
        return result.build(oneDesign,ResultCodeEnum.SUCCESS);
    }

    /**
     * 1、先获取所有教学设计
     * @param tid
     * @return
     */
//    获取所有的教学设计的列表，
    @ApiOperation("获取教学设计列表")
    @GetMapping("list/{tid}")
    public Result<List<TpaTeachDesign>> getDesignList(@PathVariable String tid){
        TpaTeacher user = tpaTeacherService.getCurrentUser();
        QueryWrapper<TpaTeachDesign> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("author_id",tid)
                .select("td_id","design_name","design_title","secondary_title","create_time","target","is_open","class_time","last_modify")
                .orderByDesc("create_time");
        List<TpaTeachDesign> designs = teachDesignService.list(queryWrapper);
//        遍历教学设计检查教学设计是否有对应的视频
        for (TpaTeachDesign teachDesign : designs) {
//            组装视频路径
            String videoName=user.getUid()+"--"+ teachDesign.getTdId()+"--"+teachDesign.getDesignName()+".mp4";
            Path videoRootPath = Paths.get(user.getAccount());//  /雪之下的猫
            Path videoPath = videoRootPath.resolve(videoName);
            teachDesign.setIsHaveVideo(resource.existsResource(ResourceType.VIDEO, videoPath.toString()));
        }
        if (designs.isEmpty()) return result.build(null,"404","教学设计列表获取失败");
        return result.build(designs,ResultCodeEnum.SUCCESS);
    }


    /**
     * redis缓存接口，在拦截器中对该数据重置定时
     * 在这里对数据进行更新
     * 1、为正在编辑的教学设计提供更新的接口
     * 2、为历史教学设计提供更改的接口
     */
    @ApiOperation("缓存教学设计内容")
    @PostMapping("cache")
    public Result<T> cacheByRedis(@RequestBody ContentVo contentVo) throws InterruptedException, RedisException {
        teachDesignService.updateDesignContent(contentVo);
        return result.build(null,ResultCodeEnum.SUCCESS);
    }


    /**
     * 写作提示接口,分为三种类型来处理：
     * 1、结构提示：根据提示进行结构的提示，如果是走这个方案，那么大概率是标题，需要根据全文内容和这个标题来进行结构化的提示
     * 2、内容提示：从零开始写
     * 3、续写：已经有一段内容了，然后进行扩写
     * other：同时还有用户的其他需求，需要将用户的其他需求放在优先的位置
     * ①选择了1/2/3,主要一1/2/3的需求为主，以用户的需求为辅
     * ②没有选择1/2/3，则根据额外需求来生成
     */
    @ApiOperation("AI写作提示")
    @PostMapping("prompt")
    public Result<String> getPromptFromAI(@RequestBody PromptVo prompt){
        String promptResult = teachDesignService.promptFromAI(prompt);
        if(promptResult==null) return result.build(null,"405","请填写你对文本的需求！");
        return result.build(promptResult,ResultCodeEnum.SUCCESS);
    }

        /**
        *  根据用户先选择的文本来生成视频资源
         *  10个字以上就是长文本
        */
     @ApiOperation("根据长文本生成视频资源")
     @PostMapping("video")
    public Result<List<VideoVo>> getVideoByText(@RequestBody LongTextVo text) throws InterruptedException {
         List<VideoVo> video = teachDesignService.getVideo(text);
         if (video==null) return result.build(null,ResultCodeEnum.FAIL);
         return result.build(video,ResultCodeEnum.SUCCESS);
     }

    /**
     * 根据关键词获取图片资源
     */
    @ApiOperation("根据长文本生成图片资源")
    @PostMapping("photo")
    public Result<List<PhotoVo>> getPhotoByText(@RequestBody LongTextVo text){
        List<PhotoVo> photo = teachDesignService.getPhoto(text);
        if (photo==null) return result.build(null,ResultCodeEnum.FAIL);
        return  result.build(photo,ResultCodeEnum.SUCCESS);
    }

    /**
     * 获取最近一次修改的教学设计
     */
    @ApiOperation("获取最近一次修改的教学设计和最近创建的教学设计")
    @GetMapping("last/{tid}")
    public Result<List<TpaTeachDesign>> getLast(@PathVariable Integer tid){
        List<TpaTeachDesign> designs = teachDesignService.lambdaQuery()
                .eq(TpaTeachDesign::getAuthorId, tid)
                .orderByDesc(TpaTeachDesign::getLastModify)
                .last("limit 1")
                .or()
                .eq(TpaTeachDesign::getAuthorId, tid)
                .orderByDesc(TpaTeachDesign::getCreateTime)
                .last("limit 1")
                .list();
        if (designs.isEmpty()) return result.build(null,"405","没有符合要求的教学设计");
        return result.build(designs,ResultCodeEnum.SUCCESS);
    }


//    TODO 在知识点图谱中通过点击知识点来获取与该知识点相关的教学设计，用户传过来的知识点只有一个，
//    TODO 使用这个来匹配design_title和secondary_title这个两个字段，实现查询与该知识点有关的所有教学设计
    @ApiOperation("根据知识点匹配教学设计")
    @GetMapping("matchDesign")
    public Result<List<TpaTeachDesign>> matchDesignByKnowledge(@RequestParam String title){
        List<TpaTeachDesign> tpaTeachDesigns = teachDesignService.matchDesignByTitle(title);
        if(tpaTeachDesigns.isEmpty()) return result.build(null,"405","未匹配到与该知识点相关的教学设计");
        return result.build(tpaTeachDesigns,ResultCodeEnum.SUCCESS);
    }

//    TODO 为指定的教学设计生成ppt，指定页数，生成完后将ppt返回给前端
    @ApiOperation("根据教学设计id生成ppt(返回ppt链接)")
    @GetMapping("pptApi")
    public Result<PPTVo> generatePPT(@RequestParam Integer tdId) throws InterruptedException {
        globalConfig.put("tdId",tdId);//设置正在制作ppt的教学设计的id
        globalConfig.put("progress",0.0);
        globalConfig.put("status","processing");
        String savePath = teachDesignService.PPTFromDesign(tdId);
        if (savePath==null) {
            globalConfig.put("status","fail");
            throw new RuntimeException("ppt生成失败");
        }
//        将ppt文件传给前端让前端可以下载
        File PPTFile=new File(savePath);
////        返回ppt的信息
        PPTVo pptVo = new PPTVo();
        pptVo.setPptName(PPTFile.getName());
        String pptUrl =savePath.replace(File.separator, "/");
        pptVo.setPptUrl(pptUrl);
        pptVo.setPptSize(FileSizeUtil.getSize(Paths.get(savePath)));
        globalConfig.put("status","success");
        globalConfig.put("progress",100.0);//ppt完全制作成功
        return result.build(pptVo,ResultCodeEnum.SUCCESS);
    }

//    todo ppt制作监控
@ApiOperation("ppt制作状态监控")
@GetMapping("pptStatus")
public Result<PPTStatusVo> getPPTStatus(@RequestParam Integer tdId) {
    // 从全局配置中获取当前正在处理的教学设计ID
    Integer processingTdId = null;
    if (globalConfig.get("tdId") != null) {
        processingTdId = (Integer) globalConfig.get("tdId");
    }


    // 情况1：没有正在制作的PPT或当前查询的不是正在制作的PPT
    if (processingTdId != null && !processingTdId.equals(tdId)) {
        return result.build(null, ResultCodeEnum.SUCCESS);
    }

    // 从全局配置中获取状态和进度
    String status = null;
    Double progress = 0.0;
    if (globalConfig.get("status") != null && globalConfig.get("progress") != null) {
        status = (String) globalConfig.get("status");
        progress = (Double) globalConfig.get("progress");
    }


    // 情况2：状态为空，返回null
    if (status == null) {
        return result.build(null, ResultCodeEnum.SUCCESS);
    }

    // 情况3：制作失败，返回null
    if ("fail".equals(status)) {
        // 清除全局配置中的相关参数
        clearPPTConfig();
        return result.build(null, ResultCodeEnum.SUCCESS);
    }

    // 情况4：制作完成，返回结果后清除配置
    if ("success".equals(status)) {
        PPTStatusVo statusVo = new PPTStatusVo();
        statusVo.setTdId(tdId);
        statusVo.setProgress(progress);
        statusVo.setStatus(status);

        // 清除全局配置中的相关参数
        clearPPTConfig();

        return result.build(statusVo, ResultCodeEnum.SUCCESS);
    }

    // 情况5：正在制作中，返回当前进度
    PPTStatusVo statusVo = new PPTStatusVo();
    statusVo.setTdId(tdId);
    statusVo.setProgress(progress);
    statusVo.setStatus(status);
    if (globalConfig.get("pptName") != null) {
        statusVo.setPptName((String) globalConfig.get("pptName"));
    } else statusVo.setPptName("未命名.pptx");

    return result.build(statusVo, ResultCodeEnum.SUCCESS);
}
    /**
     * 清除PPT制作相关的全局配置参数
     */
    private void clearPPTConfig() {
        globalConfig.put("tdId", null);
        globalConfig.put("progress", null);
        globalConfig.put("status", null);
        globalConfig.put("pptName", null);
    }


    /**
     * 根据知识点获取与该知识点相关的ppt
     */
    @ApiOperation("根据知识点获取相关ppt")
    @GetMapping("getPpt")
    public Result<List<PPTVo>> getPPTInfo(@RequestParam String title) throws IOException {
       List<PPTVo> pptByKnowledge = teachDesignService.getPPtByKnowledge(title);
       if (pptByKnowledge.isEmpty()) return result.build(null,"405","未找到相关ppt");
       return result.build(pptByKnowledge,ResultCodeEnum.SUCCESS);
    }

    /**
     * 获取该用户的所有教学设计+对应的ppt
     */
    @ApiOperation("获取所有教学设计+ppt的列表")
    @GetMapping("getAll")
    public Result<List<DesignAndPPTVo>> getDesignAndPPT() throws IOException {
        List<DesignAndPPTVo> designAndPPT = teachDesignService.getAllDesignAndPPT();
        if (designAndPPT.isEmpty()) return result.build(null,"405","该用户未创建教学设计");
        return result.build(designAndPPT,ResultCodeEnum.SUCCESS);
    }

//    @ApiOperation("根据ppt路径获取ppt文件")
//    @GetMapping("ppt")
//    public void getPPTByUrl(@RequestParam String pptUrl,HttpServletResponse response){
////        根据ppt相对路径获取ppt的文件对象
//        File PPTFile=new File(pptUrl);
//        if(!PPTFile.exists()) throw new RuntimeException("路径无效");
////        TODO 将ppt以流的形式返回
////        清空Response
//        response.reset();
////        设置响应头等一些参数
//        response.setCharacterEncoding("UTF-8");
//        response.setHeader("Content-Disposition", "attachment; filename="+ URLEncoder.encode(PPTFile.getName(), StandardCharsets.UTF_8));
//        response.setContentType("application/octet-stream");
////        开始使用Response的文件流将文件返回
//        try(InputStream is=new BufferedInputStream(Files.newInputStream(PPTFile.toPath()))) {
//            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
//            byte[] bytes = new byte[1024];
//            int len;
//
////            从输入流中官渡区一定数量的字节，将其储存在缓冲区字节数组中
//            while ((len = is.read(bytes)) >0) outputStream.write(bytes,0,len);
//            outputStream.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

//@ApiOperation("根据ppt路径获取ppt文件")
//@GetMapping("ppt")
//public void getPPTByUrl(@RequestParam String pptUrl, HttpServletResponse response, HttpServletRequest request) {
//    //        判断该文件是否存在
//    // 安全校验路径
//    if (pptUrl.contains("..") || pptUrl.startsWith("/")) {
//        throw new RuntimeException("非法路径");
//    }
//    File pptFile=new File(pptUrl);
//    if(!pptFile.exists()){
////            return result.fail(null,"该单词的音频文件不存在");
//        throw new RuntimeException(pptUrl+"文件不存在，请求重新操作！");
//    }
////        确定要下载的长度，end-start
//    String range = request.getHeader("Range");
//    long start=0;
//    long end=pptFile.length()-1;
//    if(range!=null&&range.contains("bytes=")&&range.contains("-")){
//        range = range.substring(range.lastIndexOf("=") + 1).trim();
//        String[] ranges = range.split("-");
//        try {
//            if(ranges.length==1){
//                if(range.startsWith("-")){
//                    end= Long.parseLong(ranges[0]);
//                }else if(range.endsWith("-")){
//                    start= Long.parseLong(ranges[0]);
//                }
//            }else if(ranges.length==2){
//                start= Long.parseLong(ranges[0]);
//                end= Long.parseLong(ranges[1]);
//            }
//        } catch (NumberFormatException e) {
//            start=0;
//            end=pptFile.length()-1;
//        }
//    }
////要下载的长度
//    long contentLength=end-start+1;
////        文件名
//    String fileName = pptFile.getName();
////        文件类型
//    String fileType = request.getServletContext().getMimeType(fileName);
//    response.setHeader("Accept-Ranges","bytes");
//    response.setStatus(206);
//    response.setContentType(fileType);
//    response.setHeader("Content-Type",fileType);
//    response.setHeader("Content-Disposition","inline;filename=eduPPT.pptx");
//    response.setHeader("Content-Length",String.valueOf(contentLength));
//    //坑爹地方三：Content-Range，格式为
//    // [要下载的开始位置]-[结束位置]/[文件总大小]
//    response.setHeader("Content-Range","bytes "+start+"-"+end+"/"+pptFile.length());
//
////      开始发送文件
//    BufferedOutputStream bos=null;
////        RandomAccessFile该类允许你读取(read，mode是“r”)或写入（write，mode是“w”）数据
//    RandomAccessFile randomAccessFile=null;
//    long transmitted=0;
//    try {
////
////          r表示读取的意思
//        randomAccessFile=new RandomAccessFile(pptFile,"r");
//        bos=new BufferedOutputStream(response.getOutputStream());
//        byte[] bytes=new byte[4096];
//        int len=0;
////            seek表示将文件的读取位置移到start的位置
//        randomAccessFile.seek(start);
////            transmitted+len表示已下载的进度加上本次即将要下载的长度len，如果没有该判断会超出
//        while ((transmitted+len)<=contentLength && (len=randomAccessFile.read(bytes))!=-1){
//            bos.write(bytes,0,len);
//            transmitted+=len;
//            Thread.sleep(100);
//        }
////            用来处理不满足transmitted+len<=contentLength条件时，未下载的部分
//        if(transmitted<contentLength){
//            len=randomAccessFile.read(bytes,0,(int) (contentLength-transmitted));
//            bos.write(bytes,0,len);
//            transmitted+=len;
//        }
//        bos.flush();
//        response.flushBuffer();
//        randomAccessFile.close();
//        System.out.println("下载完毕"+start+"-"+end+":"+transmitted);
//    } catch (ClientAbortException e) {
//        System.out.println("用户停止下载"+start+"-"+end+":"+transmitted);
//    }catch (IOException e){
//        e.printStackTrace();
//    }catch (InterruptedException e){
//        e.printStackTrace();
//    }finally {
//        try {
//            if(randomAccessFile!=null){
//                randomAccessFile.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
@ApiOperation("根据ppt路径获取ppt文件")
@GetMapping("ppt")
public void getPPTByUrl(@RequestParam String pptUrl, HttpServletResponse response, HttpServletRequest request)
        throws IOException {

    // 安全校验路径
    if (pptUrl.contains("..") || pptUrl.startsWith("/")) {
        throw new RuntimeException("非法路径");
    }

    // 使用ResourceService获取资源路径
    Path pptPath = resource.getResourcePath(ResourceType.PPT, pptUrl);

    if (!Files.exists(pptPath)) {
        throw new RuntimeException(pptUrl + "文件不存在，请求重新操作！");
    }

    long fileSize = Files.size(pptPath);
    String fileName = pptPath.getFileName().toString();

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
    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");
    String contentDisposition = "inline; filename*=UTF-8''" + encodedFileName;

    // 获取正确的MIME类型
    String contentType = Optional.ofNullable(request.getServletContext().getMimeType(fileName))
            .orElse("application/vnd.ms-powerpoint");

    // 设置响应头
    response.setContentType(contentType);
    response.setHeader("Accept-Ranges", "bytes");
    response.setHeader("Content-Disposition", contentDisposition);
    response.setHeader("Content-Length", String.valueOf(contentLength));
    response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);

    // 使用NIO进行高效文件传输
    try (OutputStream os = response.getOutputStream();
         FileChannel channel = FileChannel.open(pptPath, StandardOpenOption.READ)) {

        long transferred = channel.transferTo(start, contentLength, Channels.newChannel(os));
        System.out.println("PPT文件传输完成: " + transferred + " 字节");
    } catch (ClientAbortException e) {
        System.out.println("用户中断下载: " + pptUrl);
    }
}


}

