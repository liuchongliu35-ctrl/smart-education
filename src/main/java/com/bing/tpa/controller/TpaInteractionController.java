package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.InteractionRequireVo;
import com.bing.tpa.domain.VO.InteractionStatsVO;
import com.bing.tpa.domain.entity.TpaInteraction;
import com.bing.tpa.service.baseService.TpaInteractionService;
import com.bing.tpa.utils.CurrentTime;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "互动接口")
@CrossOrigin
@RestController
@RequestMapping("interaction")
public class TpaInteractionController<T> {

    @Resource
    private Result<T> result;

    @Autowired
    private TpaInteractionService interactionService;

    /**
     * 保存互动数据
     * 用户在课堂上记录互动情况，并将互动情况进行记录
     */
    @ApiOperation("更新互动数据")
    @PutMapping("update")
    public Result<T> updateInteractionData(@RequestBody TpaInteraction interaction){
        boolean update = interactionService.updateById(interaction);
        if (!update) return result.build(null,"405","互动数据保存失败，请重试");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    /**
     * 保存互动环节
    */
    @ApiOperation("保存Ai生成的互动环节")
    @PostMapping("save/{tdId}")
    public Result<T> saveInteraction(@RequestBody List<TpaInteraction> interaction, @PathVariable Integer tdId){
        interaction.forEach(i->{
            i.setTdId(tdId);
            i.setCreateTime(CurrentTime.getTime());//设置创建时间
        });
        boolean insert = interactionService.saveBatch(interaction);
        if (!insert) return result.build(null,"405","互动环节保存失败，请重试");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    @ApiOperation("根据教学设计的id获取互动环节数据")
    @GetMapping("get/{tdId}")
    public Result<List<TpaInteraction>> getByTdId(@PathVariable Integer tdId){
        List<TpaInteraction> interactionList = interactionService.interactionList(tdId);
        if (interactionList.size()==0) return result.build(null,"405","该教学设计还没有互动环节");
        return result.build(interactionList,ResultCodeEnum.SUCCESS);
    }



    /**
     *根据需求以及教学设计的主题生成新互动环节
     * 根据用户的需求生成互动环节
     * 游戏式、问答式、讨论式等互动
     * 并将互动环节返回
     */
    @ApiOperation("获取新的互动环节")
    @PostMapping("new")
    public Result<List<TpaInteraction>> newInteraction(@RequestBody InteractionRequireVo requireVo){
        if (requireVo.getNum()<3||requireVo.getNum()>5) return result.build(null,"405","互动环节数量不符合规范！");
        List<TpaInteraction> interactions = interactionService.getInteractions(requireVo);
        return result.build(interactions,ResultCodeEnum.SUCCESS);
    }




    /**
     * 根据教学设计的id获得属于这个教学设计的互动数据列表
     */
    @ApiOperation("获取某一个教学设计的互动数据")
    @GetMapping("statistic/{tdId}")
    public Result<List<InteractionStatsVO>> countInteraction(@PathVariable Integer tdId){
        List<InteractionStatsVO> interactionsByTdId = interactionService.getInteractionsByTeachingDesign(tdId);
        if (interactionsByTdId.size()==0) return result.build(null,"405","获取互动统计数据失败");
        return result.build(interactionsByTdId, ResultCodeEnum.SUCCESS);
    }





}
