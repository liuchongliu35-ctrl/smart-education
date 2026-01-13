package com.bing.tpa.controller;

import com.bing.tpa.common.ResourceType;
import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.HomeworkDTO;
import com.bing.tpa.domain.entity.TpaHomework;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.TpaHomeworkDetailsMapper;
import com.bing.tpa.mapper.TpaHomeworkMapper;
import com.bing.tpa.service.baseImpl.ResourceService;
import com.bing.tpa.service.baseService.TpaHomeworkDetailsService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Api(tags = "题目接口")
@CrossOrigin
@RestController
@RequestMapping("homeworkDetails")
public class TpaHomeworkDetailsController<T> {

    @Resource
    private Result<T> result;

    @Resource
    private TpaHomeworkDetailsService detailsService;

    @Autowired
    private TpaHomeworkDetailsMapper detailsMapper;

    @Autowired
    private TpaHomeworkMapper homeworkMapper;
    @Autowired
    private TpaHomeworkDetailsService tpaHomeworkDetailsService;

    @Autowired
    private ResourceService resource;

    /**
     * 将老师审核并修改完的题目进行保存
     * 将题目和作业的hid进行关联，然后修改后的题目保存到题目详情表中
     * 注意：这里的homeworkDetails可能不只是AI生成的题目，用户该可以从题库中将题目添加到题目预选表homeworkDetails中，然后传到这里，进行保存
     */
    @ApiOperation("保存老师审核过的题目,hid为作业id")
    @PostMapping("add/{hid}")
    public Result<T> addQuestionToHomework(@RequestBody @NotNull List<TpaHomeworkDetails> homeworkDetails, @PathVariable @NotNull Integer hid) {
        boolean save = detailsService.addNewQuestion(homeworkDetails, hid);
        if (!save) return result.build(null,"401","题目保存失败");
        return result.build(null, ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据作业的id匹配属于这个作业的题目
     * 先根据hid这个字段来匹配
     * 再根据OtherHid这个记录该题目被其他作业引用情况的字符串来匹配，看这个字符串里面有没有这个作业的id
     * 如果有就表明这个作业还引用了其他作业的题目
     * 注意：这里的获取作业所有题目的接口是获取题目的预览的，而不可以做，这个是由答案和解析的[" 棱柱的侧棱都相等"," 棱锥的侧棱都相等"," 圆柱的母线不互相平行"," 棱台的侧棱延长后不一定交于一点"]
     */
    @ApiOperation("根据作业id获取作业题目")//这里获取的题目有些可能不是属于该作业的
    @GetMapping("list/{hid}")
    public Result<HomeworkDTO> getQuestionList(@PathVariable Integer hid) throws FormatException {
        List<TpaHomeworkDetails> questions = detailsService.lambdaQuery()
                .eq(hid != null, TpaHomeworkDetails::getHid, hid)
//                可能有一些其他作业的题目也被这个作业引用了，所以模糊查询记录该题被其他作业应用的记录id的字符串
//                在模糊匹配中加入逗号是为了确定这个id是一个完整的id，而不是一个id的一部分数字
                /*根据,id,这个才可以匹配一个完整的id，不会匹配到包含id这个字符串的另一个id*/
                .or()
                .like(TpaHomeworkDetails::getOtherHid, "\""+hid+"\"")
                .list();
//                处理作业的选项
                for (TpaHomeworkDetails details:questions){
                    details.setQcontent(details.getQcontent().replace("\\",""));
                    if (details.getSelectOption()!=null){
                        List<String> list = new Gson().fromJson(details.getSelectOption(), new TypeToken<List<String>>() {}.getType());
                        List<String> newOption=new ArrayList<>();
                        list.forEach(l-> newOption.add(l.replace("\\", "")));
                        details.setOptions(newOption);
                        details.setSelections(details.getSelections().replace("\\",""));
                    }
                }
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setHstate("create");
        homeworkDTO.setDetails(questions);
//        根据作业id获取作业
        TpaHomework homework = homeworkMapper.selectById(hid);
        homeworkDTO.setHomeworkName(homework.getHName());
        homeworkDTO.setHTitle(homework.getHTitle());
        homeworkDTO.setSecondaryTitle(homework.getSecondaryTitle());
        homeworkDTO.setTotalScore(homework.getScore());
        if (questions.size()==0) return result.build(null,"400","该作业没有题目");
        return result.build(homeworkDTO,ResultCodeEnum.SUCCESS);
    }

    @ApiOperation("根据题目id修改一道题目的数据")
    @PutMapping("modifyOne/{qid}")
    public Result<T> modifyOneById(@RequestBody TpaHomeworkDetails detail){
        if(detail.getQid()==null) return result.build(null,"404","题目的id不可以为null");
        boolean update = detailsService.lambdaUpdate()
                .update(detail);
        if(!update) return result.build(null,"404","题目修改失败或不准修改");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    /**
     * 题库操作：
     * 1、根据关键字进行搜索，关键字包括题目的知识点、题目类型
     * 搜索的同时还需要检查这个题目所属作业是否是公开的
     * 如果是公开的就可以拿到里面的题，如果先公开了，然后这个作业应用了，后面又关闭了权限，在关闭之前应用了该题目的作业可以继续使用，但是后续的作业就不可以用了
     */

//    根据关键字搜索题库，并根据该题目被引用的次数来排名,
//    且题的是所有信息必须都传过去，这样用户在保存作业和题目的关系时，就可以根据qid和hid是否为空区分出AI现场生成的和从题库引用的
    @ApiOperation("关键字搜索题库")
    @GetMapping("byKey/{key}")
    public Result<List<TpaHomeworkDetails>> fromQuestionBankByKey(@PathVariable String key){
//        sql根据usage_count这个记录被引用次数的字段来进行降序排序
        List<TpaHomeworkDetails> questions = detailsMapper.selectByKeyword(key);
        if (questions.size()==0) return result.build(null,"405","题库暂时没有和关键字匹配的题目哦");
        return  result.build(questions,ResultCodeEnum.SUCCESS);
    }

//    根据教师教授的课程来匹配相关题目，这个不用关键字搜索就可以自动推荐，并根据该题目被引用的次数来排名
//    这个是匹配题库中过的所有开放的题，包括预习题和作业题，且这个只有匹配题库的功能只能在编写作业的时候使用，编写预习题的时候不可以使用题库
//    免得又要增加预习题插入时的是否重复判断以及使用其他字段记录预习题被预习任务引用的数据
    @ApiOperation("根据老师的id自动推荐相关的题目")
    @GetMapping("recommend/{tid}")
    public Result<List<TpaHomeworkDetails>> recommendFromQuestionBank(@PathVariable Integer tid){
        //        sql根据usage_count这个记录被引用次数的字段来进行降序排序
        List<TpaHomeworkDetails> questions = detailsService.automaticMatchByTid(tid);
        if (questions.size()==0) return result.build(null,"405","题库暂时没有与该课程相关的推荐题目哦");
        return result.build(questions,ResultCodeEnum.SUCCESS);
    }


    @ApiOperation("导出题目")
    @GetMapping("export")
    public void exportHomework(
            @RequestParam(required = false) Integer hid,
            @RequestParam(required = false) Integer ptId,
            HttpServletResponse response, HttpServletRequest request) throws IOException {

        // 验证参数
        if (hid == null && ptId == null) {
            throw new IllegalArgumentException("必须提供 hid 或 ptId 参数");
        }

        // 生成唯一文件名（带时间戳避免冲突）
        String fileName = "homework_" + System.currentTimeMillis() + ".docx";
        Path storagePath = null;
        String downloadFileName = "作业导出.docx"; // 用户下载时看到的文件名

        try {
            // 1. 生成 Word 文档并通过 ResourceService 保存
            try (XWPFDocument doc = tpaHomeworkDetailsService.exportToWord(hid, ptId)) {
                // 转换文档为输入流
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                doc.write(baos);
                // 调用服务保存到对应路径（自动处理内外存储）
                resource.saveResource(ResourceType.WORD, "homework/" + fileName,
                        new ByteArrayInputStream(baos.toByteArray()));
            }

            // 2. 获取实际存储路径（兼容内外存储场景）
            storagePath = resource.getResourcePath(ResourceType.WORD, "homework/" + fileName);
            if (!Files.exists(storagePath)) {
                throw new IOException("文件保存失败，未找到存储的文档");
            }

            // 3. 获取文件基本信息
            long fileSize = Files.size(storagePath);
            String rangeHeader = request.getHeader("Range");
            long start = 0;
            long end = fileSize - 1;
            long contentLength = fileSize;
            boolean partial = false;

            // 4. 处理断点续传请求 (改进处理逻辑)
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String range = rangeHeader.substring(6);
                String[] ranges = range.split("-");
                try {
                    start = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        end = Math.min(Long.parseLong(ranges[1]), fileSize - 1);
                    } else {
                        end = fileSize - 1; // 确保end值有效
                    }
                    // 验证范围有效性
                    if (start > end || start >= fileSize || end >= fileSize) {
                        start = 0;
                        end = fileSize - 1;
                    } else {
                        contentLength = end - start + 1;
                        partial = true;
                    }
                } catch (NumberFormatException e) {
                    // 无效范围头，使用完整文件
                }
            }

            // 5. 设置响应头（符合 RFC 规范的文件名编码）
            String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;

            // 6. 获取正确的MIME类型（使用下载文件名）
            String contentType = Optional.ofNullable(request.getServletContext().getMimeType(downloadFileName))
                    .orElse("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            // 7. 设置响应头
            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Disposition", contentDisposition);

            if (partial) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
            }
            response.setHeader("Content-Length", String.valueOf(contentLength));

            // 8. 高效传输文件（使用 NIO 通道）
            try (OutputStream os = response.getOutputStream();
                 FileChannel channel = FileChannel.open(storagePath, StandardOpenOption.READ)) {

                long transferred = channel.transferTo(start, contentLength, Channels.newChannel(os));
                System.out.println("作业导出文件传输完成: " + transferred + " 字节");

            } catch (ClientAbortException e) {
                System.out.println("用户中断下载: " + downloadFileName);
                // 客户端中断不需要特殊处理
            }

        } catch (Exception e) {
            // 更健壮的异常处理
            response.reset();
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.write("{\"error\": \"" + e.getMessage() + "\"}");
                writer.flush();
            }
        }
    }

}
