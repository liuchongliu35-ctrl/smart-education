package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.HomeworkCompleteVo;
import com.bing.tpa.domain.VO.TrackUpdateVo;
import com.bing.tpa.domain.entity.TpaHomeworkDetails;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.service.baseService.TpaHomeworkTrackService;
import com.bing.tpa.utils.RedisConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Api(tags = "作业完成情况接口")
@CrossOrigin
@RestController
@RequestMapping("homeworkTrack")
public class TpaHomeworkTrackController<T> {
    @Resource
    private Result<T> result;

    @Autowired
    private TpaHomeworkTrackService trackService;

    /**
     * 当学生点击开始答题，就会触发这个接口，该接口根据作业的id查找到所有的题目，然后将当前时间作为该题目开始时间
     * redis使用：tid:hid:qid或者sid:hid:qid的形式当做一条数据的id
     * 还要将题目的题干返回
     * //    注意：这个返回的题目是用来做的，而不是用来预览的，是没有答案和解析的！！！！！！！！！
     * 注意：还要在Tpa_homwork_summary表中新建一条记录该用户完成情况的数据！！！！！！！！！！！！！！！
     */
    @ApiOperation("开始记录答题数据")
    @PostMapping("trackToRedis/{id}/{hid}")
    public Result<List<TpaHomeworkDetails>> putTrackToRedis(@PathVariable @NotNull Integer id, @PathVariable @NotNull Integer hid) throws FormatException {
//        点击开始答题就会根据该作业下的所有题目生成一条答题记录对象，然后将答题记录对象插入redis
        List<TpaHomeworkDetails> details = trackService.saveTrackToRedis(id, hid);
        if (details==null) return result.build(null,"405","习题获取失败，请重新再试哦");
        return result.build(details, ResultCodeEnum.SUCCESS);
    }


    /**
     * 这个接口用于用户更改题目的作答，更改redis中对应的数据
     * 比如更改完成情况，开始解题时间，完成题目的时间，用户的作答
     * 更新作答是在答题区触发了点击事件或者失去焦点事件就表明需要保存当先的答案，就走这个接口
     * 这里将当前时间作为这道题的完成时间，然后作为下一道题的开始时间
     * status  answer completionTime timeSpent
     */
    @ApiOperation("redis记录学生答案")
    @PutMapping("updateAnswer")
    public Result<T> updateAnswer(@RequestBody TrackUpdateVo trackUpdateVo){
        Integer update = trackService.updateQuestionAnswer(trackUpdateVo, RedisConstants.HOMEWORK_ID_KEY);
        if (update==0) return  result.build(null,"405","redis中没有该题目");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }


    /**
     * 获取redis中用户保存的做题数据
     * 用户可能退出，就需要从redis重新获取到用户的数据，比如用户原来做的题目的答案
     * 同时还需要将习题也一起返回，即习题和原来的答题情况一起返回
     * 这里就将原来的答案放到之前TpaHomeworkDetails对象置空的CorrectAnswer这个字段上
     *   qid.setCorrectAnswer(null);
     *   qid.setAnswerAnalysis(null);
     */
    @ApiOperation("redis恢复原答题数据")
    @GetMapping("trackFromRedis/{id}/{hid}")
    public Result<List<TpaHomeworkDetails>> recoveryQuestion(@PathVariable Integer hid, @PathVariable Integer id) throws FormatException {
        List<TpaHomeworkDetails> recovery = trackService.recovery(hid, id);
        if (recovery==null) return result.build(null,"405","答题记录获取失败!");
        return result.build(recovery,ResultCodeEnum.SUCCESS);
    }


    /**
     * 这个接口用于将redis中的数据都写到数据库记录作业完成情况的tpa_homework_track表中
     * 当用户退出作业界面也会走这个接口将数据同步到数据库中
     * 点击提交作业也触发这个方法将redis的题目完成数据写入数据库，并将redis中过的数据清除
     * 0表示中途退出没有完成，1表示提交作业已经完成,        并更改tpa_homework_summary表中记录该用户完成作业的情况！！！
     * 0的时候就不将redis中的缓存删除，1的时候就将redis中的缓存删除
     */
    @ApiOperation("提交作业")
    @PutMapping("submit/{hid}/{uid}/{complete}")
    public Result<T> submitFromRedisToDatabase(@PathVariable Integer complete, @PathVariable  Integer hid, @PathVariable  Integer uid) throws Exception {
        Integer submit = trackService.submit(complete, hid, uid);
        if (submit==0) return result.build(null,"200","数据库保存成功");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }


    /**
     * 根据用户的id以及作业id查找某一次作业该学生每一道题的完成情况以及每道题的数据
     */
    @ApiOperation("获取一个用户一次作业的完成数据")
    @GetMapping("detail/{uid}/{hid}")
    public Result<HomeworkCompleteVo> homeworkTrackDetail(@PathVariable Integer hid, @PathVariable Integer uid){
        HomeworkCompleteVo completeVo = trackService.selectAllInfo(hid, uid);
        if(completeVo==null) return result.build(null,"405","未查询到该学生的作业完成数据");
        return result.build(completeVo,ResultCodeEnum.SUCCESS);
    }

}
