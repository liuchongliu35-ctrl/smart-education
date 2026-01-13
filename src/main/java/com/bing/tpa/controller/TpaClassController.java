package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.ClassInfoVo;
import com.bing.tpa.domain.VO.StudentClassVo;
import com.bing.tpa.domain.entity.TpaClass;
import com.bing.tpa.domain.query.PageDTO;
import com.bing.tpa.service.baseImpl.TpaClassServiceImpl;
import com.bing.tpa.service.baseService.TpaClassService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
//用户-班级模块
@Api(tags = "班级接口")
@CrossOrigin
@RestController
@RequestMapping("class")
public class TpaClassController<T> {

    private final static String excelPath="src/main/resources/excel/";
    @Resource
    private  TpaClassService tpaClassService;

    @Resource
    private Result<T> result;
    /**
     * 添加新的班级
     * @param tpaClass
     * @return
     */
    @ApiOperation("添加新的班级")
    @PostMapping("")
    public Result<TpaClassServiceImpl.ClassData> addOne(@RequestBody TpaClass tpaClass){
        TpaClassServiceImpl.ClassData classData = tpaClassService.addClass(tpaClass);
        if(classData==null) return result.fail(null,"班级创建失败");
        return result.build(classData, ResultCodeEnum.SUCCESS);
    }

    /**
     * 向班级添加一个学生
     * @param student
     * @return
     */
    @ApiOperation("向班级添加一个学生")
    @PostMapping("add")
    public Result<T> addOneStudent(@RequestBody StudentClassVo student){
        Integer integer = tpaClassService.addOne(student);
        if(integer==-1) return result.build(null,ResultCodeEnum.FAIL);
        return result.build(null,ResultCodeEnum.SUCCESS);
    }

    /**
     * 根据一张excel学生表向班级批量添加学生
     * @param file
     * @param classId
     * @return
     */
//    @ApiOperation("根据一张excel学生表向班级批量添加学生")
//    @PostMapping(value = "addList/{classId}",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    @ApiImplicitParam(name = "file",value = "批量签名文件导入",required = true,dataType="MultipartFile",allowMultiple = true,paramType = "query")
//    public Result<T> addListByExcel(@RequestParam("file") MultipartFile[] file, @PathVariable@NotNull Integer classId){
//        boolean flag = false;
//        String newName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//        String filename = file[0].getOriginalFilename();
//        String subffix = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
//        Path destPath = Paths.get(excelPath + newName + "." + subffix);
//        try {
//            // 使用 Files.copy() 将文件内容复制到目标路径
//            Files.copy(file[0].getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
//            // 保存成功后调用添加学生和学生-班级的方法
//            flag = tpaClassService.addList(excelPath + newName + "." + subffix, classId);
//        } catch (IOException e) {
//            return result.build(null, ResultCodeEnum.FAIL);
//        }
//
//        if (!flag) {
//            return result.fail(null, "404");
//        }
//        return result.build(null, ResultCodeEnum.SUCCESS);
//    }

    /**
     * 根据教师id获取该教师的班级列表
     */
    @ApiOperation("根据教师id获取该教师的班级列表")
    @GetMapping("classList/{tid}")
    public Result<PageDTO<TpaClass>> getClassList(@PathVariable Integer tid){
        PageDTO<TpaClass> classPage = tpaClassService.getListByTeacherId(tid);
        if(classPage==null) return result.build(null,ResultCodeEnum.FAIL);
        return result.build(classPage,ResultCodeEnum.SUCCESS);
    }

//    根据id删除班级
    @ApiOperation("根据id删除班级")
    @DeleteMapping("removeClass/{cid}")
    public Result<T> deleteClass(@PathVariable String cid){
        boolean b = tpaClassService.removeById(cid);
        if(!b) return result.build(null,"401","班级删除失败！");
        return  result.build(null,ResultCodeEnum.SUCCESS);
    }

//    根据班级id获取班级详情
    @ApiOperation("根据班级id获取班级基本信息")
    @GetMapping("classInfo/{cid}")
    public Result<ClassInfoVo> getClassInfo(@PathVariable Integer cid){
        ClassInfoVo classInfoVo = tpaClassService.classInfo(cid);
        return result.build(classInfoVo,ResultCodeEnum.SUCCESS);
    }


}
