package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.*;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.domain.entity.TpaPreviewTrack;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.service.baseService.TpaHomeworkTrackService;
import com.bing.tpa.service.baseService.TpaPreviewTrackService;
import com.bing.tpa.utils.RedisConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  这个接口文件主要是对预习题完成情况的记录以及预习任务的完成情况记录
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Api(tags = "预习任务完成接口")
@CrossOrigin
@RestController
@RequestMapping("previewTrack")
public class TpaPreviewTrackController<T> {
    @Resource
    private Result<T> result;

    @Autowired
    private TpaPreviewTrackService trackService;

    @Autowired
    private TpaHomeworkTrackService homeworkTrackService;
    /**
     * 需要明确的是：！！！！！！！！！！！！
     * 1、预习题和作业题放到了一个表中，根据作业id和预习任务id区分
     * 2、每个学生题目完成情况数据和预习题的每道题完成数据都放到一张表中，通过预习任务id和迆id以及学生id进行区分,所以进行作业题跟踪的思路和预习题跟踪的思路一样
     * 3、学生作业的完成情况放到summary表中
     * 4、学生预习任务完成情况放在preview_track表中
     */

    /**
     * 点击开始做预习题，在这里之前，点击开始预习的时候就已经在preview_track表中新建一条数据了
     * ①将预习题的追踪数据放到redis中
     * 只有点击了做预习题才可以将题目追踪数据放redis中
     * ②返回预习题数据，即List<TpaHomeworkDetails>
     */
    @ApiOperation("开始记录预习题的答题情况")
    @PostMapping("newTrack/{id}/{ptId}")
    public Result<List<TpaHomeworkDetails>> addNewTrackToRedis(@PathVariable Integer id, @PathVariable Integer ptId) throws FormatException {
        List<TpaHomeworkDetails> details = trackService.saveTrackToRedis(id, ptId);
        if(details.size()==0) return  result.build(null,"405","获取预习题失败！");
        return result.build(details, ResultCodeEnum.SUCCESS);
    }



    /**
     * 点击提交预习资料附加题答案的接口  完成预习题的标志是将附加题都做完，没做完但是点击了提交就会走这个接口，也会看作做完了
     * 当用户完成题目并提交后就将text_finish设置为已完成，默认是0未完成
     */
    @ApiOperation("提交预习资料完成情况")
    @PostMapping("submitExtra")
    public Result<T> extraQuestionCheck(@RequestBody PreviewTextVo previewTextVo) throws Exception {
        Integer save = trackService.submitExtraQuestionAnswer(previewTextVo);
        if (save==0) return result.build(null,"405","任务提交失败!");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }


    /**
     * 更新预习题目的答案
     * 需要更新的字段：status  answer completionTime timeSpent
     *这个记录预习题完成情况的也使用记录作业题目完成详情的
     */
    @ApiOperation("记录学生预习题答案")
    @PutMapping("updateAnswer")
    public Result<T> updateRedisData(@RequestBody TrackUpdateVo trackUpdateVo){
//    直接调用作业题目更新答案的逻辑，因为使用的是同一个参数TrackUpdateVo，只不过其中的tid不是作业id而是预习题id
//      但是由于作业题目和预习题目的追踪数据在redis中的key都是：用户id:任务id:题目id，所以可以共用一个这种更新的逻辑
//        预习题：preview:id+":"+ptId+":"+details1.getQid().toString()
//        作业题：homework:id+":"+hid+":"+qid.getQid().toString()
//        在这里传入key的前缀，可以借借此来区分作业题目的key和预习题的key
        Integer update = homeworkTrackService.updateQuestionAnswer(trackUpdateVo, RedisConstants.PREVIEW_ID_KEY);
        if (update==0) return  result.build(null,"405","redis中没有该题目");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    /**
     * 预习题中途退出再进去后恢复题目数据
     */
    @ApiOperation("恢复原来预习题完成数据")
    @GetMapping("trackRecovery/{id}/{ptId}")
    public Result<List<TpaHomeworkDetails>> recoveryTrack(@PathVariable Integer id, @PathVariable Integer ptId) throws FormatException {
        List<TpaHomeworkDetails> recovery = trackService.recovery(id, ptId);
        if (recovery.size()==0) return result.build(null,"405","历史答题数据获取失败");
        return result.build(recovery,ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存题目完成详情数据到数据库
     * 分为中途退出保存和做回购提交保存
     */
    @ApiOperation("提交预习题")
    @PutMapping("submit/{ptId}/{uid}/{complete}")
    public Result<T> submitTrack(@PathVariable  Integer ptId, @PathVariable  Integer uid,@PathVariable Integer complete) throws Exception {
        Integer save = trackService.submitTrack(ptId, uid, complete);
        if (save==0) return result.build(null,ResultCodeEnum.FAIL);
        return result.build(null,ResultCodeEnum.SUCCESS);
    }


    /**
     * 根据用户id和预习任务id，获取完成情况
     */
    @ApiOperation("获取一个用户一个预习任务完成情况")
    @GetMapping("detail/{uid}/{ptId}")
    public Result<PreviewCompleteVo> previewComplete(@PathVariable Integer ptId, @PathVariable Integer uid){
        PreviewCompleteVo completeVo = trackService.selectAllInfo(ptId, uid);
        if (completeVo==null) return result.build(null,"405","未查询到该学生的预习任务完成数据");
        return result.build(completeVo,ResultCodeEnum.SUCCESS);
    }

    /**
     * 获取个性化推荐资源
     */
    @ApiOperation("获取一个学生个性化推荐题目")
    @GetMapping("specialData/{uid}/{ptId}")
    private Result<SpecialDataVo> getSpecialData(@PathVariable Integer ptId, @PathVariable Integer uid){
        SpecialDataVo dataVo = trackService.specialData(uid, ptId);
        if (dataVo==null) return result.build(null,"405","该学生还没有个性化资源");
        return result.build(dataVo,ResultCodeEnum.SUCCESS);
    }

    /**
     * 获取一次预习任务的学生整体完成数据
     */
    @ApiOperation("一次预习任务的学生完成信息列表")
    @GetMapping("situation/{ptId}/{cid}")
    public Result<List<TpaPreviewTrack>> getPreviewSituation(@PathVariable Integer ptId, @PathVariable Integer cid){
        List<TpaPreviewTrack> previewSituationVo = trackService.getStudentListAndPreviewSituationVo(ptId, cid);
        if (previewSituationVo==null) return result.build(null,"405","该班级暂时没有学生完成该预习任务");
        return result.build(previewSituationVo,ResultCodeEnum.SUCCESS);
    }



}
