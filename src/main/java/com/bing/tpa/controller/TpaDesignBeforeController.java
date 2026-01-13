package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.entity.TpaDesignBefore;
import com.bing.tpa.service.baseService.TpaDesignBeforeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Api(tags = "教学设计前瞻数据接口")
@CrossOrigin
@RestController
@RequestMapping("/tpa_system/tpaDesignBefore")
public class TpaDesignBeforeController<T> {

    @Resource
    private Result<T> result;

    @Autowired
    private TpaDesignBeforeService designBeforeService;

    /**
     * 这个里面就只要写获取教学设计对应的数据分析情况就可以了
     * 教学设计前置数据分析在创建教学设计的时候就已经在生成了
     * 这里直接获取就可以了
     */
    @ApiOperation("根据教学设计id获取前瞻数据")
    @GetMapping("beforeData/{tdId}")
    public Result<TpaDesignBefore> beforeDataByTdId(@PathVariable Integer tdId){
        TpaDesignBefore beforeData = designBeforeService.lambdaQuery()
                .eq(TpaDesignBefore::getTdId, tdId).one();
        if (beforeData==null) return result.build(null,"405","该教学设计的前瞻数据生成失败！");
        return result.build(beforeData, ResultCodeEnum.SUCCESS);
    }
}
