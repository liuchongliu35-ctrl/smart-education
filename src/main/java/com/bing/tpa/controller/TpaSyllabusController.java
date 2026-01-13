package com.bing.tpa.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.SyllabusResultVo;
import com.bing.tpa.domain.VO.TpaSyllabusWithNeed;
import com.bing.tpa.domain.entity.TpaSyllabus;
import com.bing.tpa.exception.RedisException;
import com.bing.tpa.mapper.TpaSyllabusMapper;
import com.bing.tpa.service.baseService.TpaSyllabusService;
import com.bing.tpa.utils.CurrentTime;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
//教学设计模块
@Api(tags = "教学设计大纲接口")
@CrossOrigin
@RestController
@RequestMapping("syllabus")
public class TpaSyllabusController<T> {

    @Resource
    private Result<T> result;

    @Resource
    private TpaSyllabusService tpaSyllabusService;

    @Resource
    private TpaSyllabusMapper syllabusMapper;


    /**
     * 根据教师的id来匹配相关的模版
     * @param tid
     * @return
     */
//    根据教师的id来获取该教师的教学设计大纲模版，也可以根据该教师的id找到该教师教授的课程，根据课程以及是否公开到大纲库中查找所有相关的大纲设计
//    1、经典的大纲，2、自己设计的（也包括Ai生成的），4、别人公开的
    @ApiOperation("根据教师id获取所有大纲")
    @GetMapping("/{tid}")
    public Result<List<TpaSyllabus>> getSyllabus(@PathVariable Integer tid){
        List<TpaSyllabus> tpaSyllabus = tpaSyllabusService.getTpaSyllabus(tid);
        if (tpaSyllabus==null) return result.build(null, "404","模版获取失败，请稍后再试");
        return result.build(tpaSyllabus,ResultCodeEnum.SUCCESS);
    }

//    从AI获取大纲,获取到后直接返回给用户进行检查，检查完后检点完成就触发save方法进行保存
    @ApiOperation("AI生成教学设计大纲")
    @PostMapping("fromAI")
    public Result<SyllabusResultVo> getFromAI(@RequestBody TpaSyllabusWithNeed withNeed){
        SyllabusResultVo syllabusFromAI = tpaSyllabusService.getTpaSyllabusFromAI(withNeed);
        if (syllabusFromAI==null) return result.build(null,"404","教学大纲生成失败，请稍后再试");
        return result.build(syllabusFromAI,ResultCodeEnum.SUCCESS);
    }


    /**
     * 保存AI生成的经过前端编辑过的模版，同时也可以保存自定义的模版
     * @param syllabus
     * @return
     */
//     save方法保存大纲（可以是来自Ai的，也可以是自己编写的大纲，也可以是在数据库中已经有的大纲，这里为了将大纲数据初始化到redis中
//     自己编写大纲不采用redis缓存，直接一次性编写完后再一次性储存，编写中间前端可以使用浏览器本地储存

    /**
     * 页面走到这里即将要将大纲数据写到教学设计前时，强制用户完成这个save方法，不准用户退出，不然就无法将初始化内容携带redis中，也就不会写到数据库中
     * 这样就会有初始化的教学设计是空的，这样不好
     * 这里有可能出现一下几种情况：
     * 1、新生成的大纲，没有和任何教学设计关联，此时这里就是第一次和刚才调用创建教学设计的接口获得的教学设计id进行关联
     * 2、选择的教学大纲是之前就已经有的，已经和其他教学设计关联了，这里就需要将新的教学设计的id加入到记录该大纲引用的教学设计id的字段tdId中，所以记录这个的字段应该是一个字符串，可以将一个List集合转为json串储存到里面
     * 这里tid为当前要关联的教学设计的id
     */
    @ApiOperation("tdId为教学设计id！，保存AI生成的教学设计大纲并初始化redis")
    @PostMapping("save/{tdId}")
    public Result<String> save(@RequestBody TpaSyllabus syllabus, @PathVariable Integer tdId) throws RedisException {
//        System.err.println("走了这个代码！！");
        Integer isSuccess = tpaSyllabusService.saveSyllabus(syllabus,tdId);
        if (isSuccess==0) return result.build(null,"404","该教学设计已在redis中，不可再创建");
        if(isSuccess==-1) return result.build(null,"404","教学设计的id不可为空");
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据模版的id获取模版的内容
     */
    @ApiOperation("根据大纲id获取内容")
    @GetMapping("byId/{sid}")
    public Result<TpaSyllabus> getById(@PathVariable Integer sid){
        TpaSyllabus syllabus = syllabusMapper.selectById(sid);
        if(syllabus==null) return result.build(null,"404","获取模版失败");
        return result.build(syllabus,ResultCodeEnum.SUCCESS);
    }

    @ApiOperation("删除大纲")
    @DeleteMapping("deleteById/{syllabusId}")
    public Result<T> deleteById(@PathVariable Integer syllabusId){
        boolean delete = tpaSyllabusService.removeById(syllabusId);
        if(!delete) return result.build(null,ResultCodeEnum.FAIL);
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

//    根据教学设计大纲id进行修改，主要用于在历史教学设计大纲列表提供修改功能

    /**
     *用户对该大纲修改是在历史记录上或者是在大纲列表上，只是单纯的修改就行，不涉及后续教学设计的编写，前端会在大纲列表特别的设置一个修改按钮，和创建教学设计不一样
     * 即大概进入大纲列表后有两种功能：
     *  1.创建教学设计，这就要点击某一个大纲，然后填写教学设计的基本内容
     *  2.修改大纲，只修改，如果要创建教学设计，那可以先点击修改，然后再点击创建
     *  所以不需要将这个大纲上传到redis中，只有确定了要创建教学设计，就会走上面的save方法，将大纲初始化到redis中！！！！
     *   将新的数据更新到数据库中
     * @param syllabus
     * @return
     */
    @ApiOperation("修改大纲,syId为大纲id")
    @PutMapping("modify/{syId}")
    public Result<T> updateSyllabus(@RequestBody TpaSyllabus syllabus, @PathVariable Integer syId){
// 创建更新条件
        LambdaUpdateWrapper<TpaSyllabus> updateWrapper = new LambdaUpdateWrapper<>();
// 设置更新条件：根据syllabus_id更新
        updateWrapper.eq(TpaSyllabus::getSyllabusId, syId);

// 只更新指定的字段，并判断字段是否为空
        if (!Objects.equals(syllabus.getContent(), "")) {
            updateWrapper.set(TpaSyllabus::getContent, syllabus.getContent());
        }
        if (!Objects.equals(syllabus.getType(), "")) {
            updateWrapper.set(TpaSyllabus::getType, syllabus.getType());
        }
        if (syllabus.getIsOpen()==1||syllabus.getIsOpen()==0){
            updateWrapper.set(TpaSyllabus::getIsOpen,syllabus.getIsOpen());
        }
        if (syllabus.getIsDelete()==1){
            updateWrapper.set(TpaSyllabus::getIsDelete,syllabus.getIsDelete());
        }
        if (syllabus.getIsUpdate()==1||syllabus.getIsUpdate()==0){
            updateWrapper.set(TpaSyllabus::getIsUpdate,syllabus.getIsUpdate());
        }
        updateWrapper.set(TpaSyllabus::getUpdateTime, CurrentTime.getTime());
        updateWrapper.eq(TpaSyllabus::getIsUpdate,1);
        // 执行更新
        boolean update = tpaSyllabusService.update(updateWrapper);
        if (!update) {
            return result.build(null, ResultCodeEnum.FAIL);
        }

        return result.build(null, ResultCodeEnum.SUCCESS);
    }


    /**

     */



}
