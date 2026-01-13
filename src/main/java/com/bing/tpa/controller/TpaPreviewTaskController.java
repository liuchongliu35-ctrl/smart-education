package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.PreviewTaskReleaseVo;
import com.bing.tpa.domain.VO.PreviewTaskVo;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.domain.entity.TpaPreviewTask;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.TpaClassMapper;
import com.bing.tpa.mapper.TpaHomeworkDetailsMapper;
import com.bing.tpa.mapper.TpaPreviewTaskMapper;
import com.bing.tpa.service.baseService.TpaPreviewTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Api(tags = "预习任务接口")
@CrossOrigin
@RestController
@RequestMapping("previewTask")
public class TpaPreviewTaskController<T> {

    @Resource
    private Result<T> result;

    @Autowired
    private TpaPreviewTaskService previewTaskService;

    @Autowired
    private TpaPreviewTaskMapper previewTaskMapper;

    @Autowired
    private TpaHomeworkDetailsMapper detailsMapper;

    @Autowired
    private TpaClassMapper classMapper;

    /**
     * 创建一个预习任务
     * 将该任务和老师进行绑定
     * 并根据老师设置的条件获取预习任务的预习题和预习资料
     * 给用户进行审核
     * 给用户进行审核
     */
    @ApiOperation("创建预习任务")
    @PostMapping("generate/{tid}")
    public Result<PreviewTaskVo> generateFromAi(@RequestBody TpaPreviewTask previewTask, @PathVariable @NotNull Integer tid){
        PreviewTaskVo previewTaskVo = previewTaskService.generateTaskResources(previewTask, tid);
        if (previewTask==null||previewTaskVo.getTaskList()==null||previewTaskVo.getPreviewText()==null) return result.build(null,"405","生成预习资料失败，请重新尝试");
        previewTaskVo.setPstate("nocreate");//表示数据还没有入库，需要审核后入库
        return result.build(previewTaskVo, ResultCodeEnum.SUCCESS);
    }



    /**
     * 对预习任务的资源和题目进行保存
     * 并将预习题和这个预习任务联系起来
     */
    @ApiOperation("保存预习任务的资源")
    @PostMapping("save")
    public Result<T> saveTask(@RequestBody PreviewTaskVo previewTaskVo){
        Integer save = previewTaskService.saveTaskResource(previewTaskVo);
        if(save==0) return result.build(null,"405","题目保存失败，请重新再试");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }


    /**
     * 获取本班的预习任务列表
     * 用户可以先点击进入班级会获得班级的id
     * 然后根据班级的id获取
     */
    @ApiOperation("根据班级id获取预习任务列表")
    @GetMapping("taskList/{cid}")
    public Result<List<TpaPreviewTask>> taskList(@PathVariable @NotNull Integer cid){
        List<TpaPreviewTask> taskList = previewTaskService.getTaskList(cid);
        if (taskList.size()==0) return result.build(null,"405","该班级还没有发布预习任务");
        return result.build(taskList,ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据老师的id查询该老师发布过的预习任务
     * @param tid
     * @return
     */
    @ApiOperation("查找老师发布过的历史预习任务")
    @GetMapping("taskByTid/{tid}")
    public Result<List<TpaPreviewTask>> taskByTid(@PathVariable @NotNull Integer tid){
        List<TpaPreviewTask> previewList = previewTaskService.lambdaQuery()
                .eq(TpaPreviewTask::getAuthorId, tid)
                .list();
        Integer personNum=0;
        if (!previewList.isEmpty() &&previewList.get(0).getCid()!=null){
            personNum= classMapper.getClassPersonNum(previewList.get(0).getCid(), null);
        }
        for (TpaPreviewTask task:previewList){
            if (personNum!=0)
             task.setUnComplete(personNum-task.getComplete());
            if(task.getCid()==null){
                task.setIsSend(0);
            }else {
                task.setIsOpen(1);
                task.setClassName(classMapper.selectById(task.getCid()).getCName());
            }
        }
        if(previewList.isEmpty()) return result.build(null,"405","该老师还没有发布预习任务");
        return result.build(previewList,ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据预习任务的id查找预习任务的具体内容
     * 需要将预习题也拉取过来
     * 这里已经可以根据预习任务id将预习任务的具体内容：预习资料，预习题给拉取出来，所以在tpaPreviewTrack中只用将预习题的详情拉取出来就可以了，预习资料和题目已经在这里拉取出来了
     * 这里相当于点击开始预习，然后获得两种数据：预习资料和预习题，然后可以选择预习资料还是预习题
     * 所以这里就表明用户已经开始进行预习了，需要再preview_track表中创建一条数据
     */
    @ApiOperation("根据id获取预习任务内容id不为0时开始预习")//id为0时表示只获取预习资料内容
    @GetMapping("taskDetailById/{ptId}/{id}")
    public Result<PreviewTaskVo> getTaskDetailById(@PathVariable @NotNull Integer ptId, @PathVariable Integer id) throws FormatException {
        PreviewTaskVo previewTaskVo = previewTaskService.taskByPtId(ptId,id);
        if(previewTaskVo==null) return result.build(null,"405","预习任务数据获取失败");
//        此时获取预习任务的资源表示已经审核通过了
        previewTaskVo.setPstate("create");
        return result.build(previewTaskVo,ResultCodeEnum.SUCCESS);
    }

    /**
     * 发布预习任务
     * 只可以发布到一个班级。实现不同班级资源个性化，反正生成新的预习任务也只是点击后自动生成的，不需要占用老师太多时间来编写
     */
    @ApiOperation("预习任务和班级关联")
    @PutMapping("release")
    public Result<T> releaseTask(@RequestBody PreviewTaskReleaseVo taskReleaseVo){
        boolean update = previewTaskService.releaseTask(taskReleaseVo);
        if (!update) return result.build(null,"404","预习任务发布失败，原因是数据更新失败");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }


    /**
     * 使用AI在原有题目的基础上再添加自己的数目的题目
     */
    @ApiOperation("AI再添加题目")
    @GetMapping("extra/{ptId}/{num}")
    public Result<PreviewTaskVo> extraQuestion(@PathVariable Integer num, @PathVariable Integer ptId){
        if (num>10) return result.build(null,"404","不可以一次性增加超过10个题目哦！");
        PreviewTaskVo previewTaskVo = previewTaskService.addQuestions(ptId, num);
        if (previewTaskVo==null||previewTaskVo.getTaskList().size()==0) return result.build(null,"405","新增题目失败");
        return result.build(previewTaskVo,ResultCodeEnum.SUCCESS);
    }

//    设置预习任务的隐藏状态
    @ApiOperation("预习任务设为隐私")
    @PutMapping("unOpen/{ptId}/{isOpen}")
    public Result<T> taskUnClock(@PathVariable Integer ptId, @PathVariable Integer isOpen){
        boolean update = previewTaskService.lambdaUpdate()
                .eq(TpaPreviewTask::getPtId, ptId)
                .set(TpaPreviewTask::getIsOpen, isOpen)
                .update();
        if(!update) return result.fail(null,"隐藏失败！");
        return result.build(null,ResultCodeEnum.SUCCESS);

    }

    /**
     * 根据预习题的id对预习题做修改
     */
    @ApiOperation("对一个预习题进行修改")
    @PutMapping("modify")
    public Result<T> modifyOneById(@RequestBody TpaHomeworkDetails homeworkDetails){
        if(homeworkDetails.getQid()==null) return result.build(null,"404","题目的id不可以为null");
        int update = detailsMapper.updateById(homeworkDetails);
        if(update==0) return result.build(null,"404","题目修改失败或不准修改");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    @ApiOperation("根据知识点和教师id获取预习资料资源")
    @GetMapping("point-preview")
    public Result<List<TpaPreviewTask>> previewByTidAndTitle(@RequestParam String title,@RequestParam Integer tid){
        List<TpaPreviewTask> previewTasks = previewTaskService.taskByTidAndTitle(title, tid);
        if (previewTasks.isEmpty()) return result.build(null,"405","未找到属于该知识点的预习任务");
        return result.build(previewTasks,ResultCodeEnum.SUCCESS);
    }


    @ApiOperation("导出预习资料")
    @GetMapping("export-z")
    public  void  exportZ(@RequestParam Integer ptId, HttpServletResponse response){
        try {
            // 1. 调用Service生成Word
            XWPFDocument doc = previewTaskService.generatePreviewWord(ptId);

            // 2. 设置响应头（支持下载）
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            response.setHeader("Content-Disposition",
                    "attachment;filename=preview_task_" + ptId + ".docx");

            // 3. 写入响应流
            doc.write(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            try {
                response.getWriter().write("生成失败：" + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


}
