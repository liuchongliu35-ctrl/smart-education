package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.entity.TpaHomeworkSummary;
import com.bing.tpa.service.baseService.TpaHomeworkSummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "作业完成情况接口")
@CrossOrigin
@RestController
@RequestMapping("summary")
public class TpaHomeworkSummaryController<T> {

    @Autowired
    private Result<T> result;

    @Autowired
    private TpaHomeworkSummaryService summaryService;


    /**
     * 判断用户某一个作业的答题状态并将作业完成情况数据返回
     * 同时将状态返回：1表示正在进行，0表示已完成
     */


    /**
     * 根据作业id在tpa_homework_summary表中获取这次作业完成情况
     * 由于这个表只会记录点击开始答题的用户的完成情况，所以没有点击的就算是未开始，这个就先根据班级id获取所有学生
     * 然后对比班级的所有学生，将没有完成的学生和已完成的学生区分开
     */
    @ApiOperation("根据作业id和班级id获取该班级某一次作业的所有学生大致完成情况")
    @GetMapping("list/{cid}/{hid}")
    public Result<List<TpaHomeworkSummary>> getSummaryList(@PathVariable Integer hid, @PathVariable Integer cid){
        List<TpaHomeworkSummary> summarys = summaryService.getSummarys(hid, cid);
        if (summarys.size()==0) return result.fail(null,"获取作业完成情况列表失败");
        return result.build(summarys, ResultCodeEnum.SUCCESS);
    }


}
