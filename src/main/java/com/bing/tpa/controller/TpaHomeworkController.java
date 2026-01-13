package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.HomeworkReleaseVo;
import com.bing.tpa.domain.VO.HomeworkTotalSituation;
import com.bing.tpa.domain.VO.HomeworkVo;
import com.bing.tpa.domain.entity.TpaHomework;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.TpaClassMapper;
import com.bing.tpa.service.baseService.TpaHomeworkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Api(tags = "作业接口")
@CrossOrigin
@RestController
@RequestMapping("homework")
public class TpaHomeworkController<T> {

    @Resource
    private Result<T> result;

    @Resource
    private TpaHomeworkService homeworkService;

    @Autowired
    private TpaClassMapper classMapper;

    /**
     * AI根据老师设置的条件生成题目,并将老师的id与作业进行绑定
     * 同时创建一个作业数据,  homework包含了创建一个作业至少需要包含的数据！！
     */
    @ApiOperation("Ai根据条件生成题目")
    @PostMapping("generate/{tid}")
    public Result<HomeworkVo> generateFromAi(@RequestBody TpaHomework homework, @PathVariable @NotNull Integer tid) throws FormatException {
        HomeworkVo homeworkVo = homeworkService.generateQuestions(homework,tid);
        if(homeworkVo==null) return result.build(null, "404","作业创建失败");
        homeworkVo.setHstate("nocreate");
        return result.build(homeworkVo,ResultCodeEnum.SUCCESS);
    }

    /**
     * 审核过程中中途退出就会将创建的作业删除
     */
    @ApiOperation("审核中途退出删除本次作业接口")
    @DeleteMapping("exist/{hid}")
    public Result<T> existAndDelete(@PathVariable Integer hid){
        boolean b = homeworkService.removeById(hid);
        if (!b) return result.build(null,ResultCodeEnum.FAIL);
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据班级获取本班级的作业列表
     * 注意需要通过,cid,这种模式来匹配，因为这样才可以确定一个完整的班级id，作业记录所属班级的是使用字符串记录的
     * 因为一个作业可能被多个班级使用，所以记录班级的字段需要为字符串，因此根据班级匹配作业是就需要检查这个字符串中是包含该班级的id
     * @return
     */
    @ApiOperation("根据班级id获取作业列表")
    @GetMapping("homeworkList/{cid}")
    public Result<List<TpaHomework>> getHomeworkList(@PathVariable Integer cid){
        List<TpaHomework> homeworkList = homeworkService.lambdaQuery()
//            这里直接根据班级id来匹配该班级独一份的作业
                .eq(TpaHomework::getCid, cid)
                .list();
        if(homeworkList==null) return result.build(null,"402","该班级暂时没有发布作业");
        return result.build(homeworkList,ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据老师的id查找由该老师生成的所有作业
     */
    @ApiOperation("根据老师id查找该老师生成的所有作业")
    @GetMapping("homeworkByTid/{tid}")
    public Result<List<TpaHomework>> getHomeworkByTid(@PathVariable Integer tid){
        List<TpaHomework> homeworkList = homeworkService.lambdaQuery()
                .eq(TpaHomework::getAuthorId, tid)
                .list();
//        设置作业发布的班级以及是否已经发布
        for (TpaHomework homework:homeworkList){
            if (homework.getCid()==null) {
                homework.setIsSend(0);
            }else{
                homework.setIsSend(1);
                homework.setClassName(classMapper.selectById(homework.getCid()).getCName());
            }
        }
        if(homeworkList.isEmpty()) return result.build(null,"402","您暂时没有创建作业哦");
        return result.build(homeworkList,ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据作业的id查找作业
     */
    @ApiOperation("根据作业id获取作业详情")
    @GetMapping("homeworkByHid/{hid}")
    public Result<TpaHomework> getHomeworkByHid(@PathVariable Integer hid){
        TpaHomework homework = homeworkService.lambdaQuery()
                .eq(TpaHomework::getHid, hid)
                .one();
        if (homework==null) return result.build(null,"402","没找到这个作业哦");
        return result.build(homework,ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据作业id将作业发布到对应的班级
     */
    @ApiOperation("发布作业：将作业和班级关联")
    @PutMapping("release")
    public Result<T> releaseHomework(@RequestBody HomeworkReleaseVo releaseVo){
        boolean update = homeworkService.releaseHomework(releaseVo);
        if (!update) return result.build(null,"404","该班级存在这个作业");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    /**
     * 使用AI在原有的题目下自定义新添加10个一下的题目
     * num为自定义添加的题目数量
     */
    @ApiOperation("Ai再获得10个或以下题目")
    @GetMapping("extra/{hid}/{num}")
    public Result<HomeworkVo> extraQuestion(@PathVariable Integer hid, @PathVariable Integer num){
        if (num>10) return result.build(null,"404","不可以一次性增加超过10个题目哦！");
        HomeworkVo homeworkVo = homeworkService.addExtraQuestion(hid, num);
        if(homeworkVo==null) return result.build(null,"404","题目增加失败，请稍后重试");
        homeworkVo.setHstate("nocreate");
        return result.build(homeworkVo,ResultCodeEnum.SUCCESS);
    }

    /**
     * 作业创建默认是公开的，可以在这里设置为不公开
     * 默认公开是为了题库的题可以多一些
     */
    @ApiOperation("将作业设为隐私")
    @PutMapping("unOpen/{hid}/{isOpen}")
    public Result<T> homeworkOpen(@PathVariable Integer hid, @PathVariable Integer isOpen){
        boolean update = homeworkService.lambdaUpdate()
                .eq(TpaHomework::getHid, hid)
                .set(TpaHomework::getIsOpen, isOpen)
                .update();
        if(!update) return result.build(null,"404","隐藏失败");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    /**
     * 获取一次作业的整体情况
     */
    @ApiOperation("获取一次作业的总体情况")
    @GetMapping("homeworkSituation/{hid}/{cid}")
    public Result<HomeworkTotalSituation> getSituation(@PathVariable Integer hid, @PathVariable Integer cid){
        HomeworkTotalSituation totalSituation = homeworkService.totalSituation(hid, cid);
        if (totalSituation==null) return result.build(null,"405","还没有学生完成该作业！");
        return result.build(totalSituation,ResultCodeEnum.SUCCESS);
    }

    @ApiOperation("根据知识点和教师id获取作业资源")
    @GetMapping("point-homework")
    public Result<List<TpaHomework>> getHomeworkByPoint(@RequestParam String title,@RequestParam Integer uid){
        List<TpaHomework> homeworks = homeworkService.selectByPointAndCid(title, uid);
        if (homeworks.isEmpty())  return result.build(null,"405","没有找到与该知识点有关的作业");
        return result.build(homeworks,ResultCodeEnum.SUCCESS);
    }





}
