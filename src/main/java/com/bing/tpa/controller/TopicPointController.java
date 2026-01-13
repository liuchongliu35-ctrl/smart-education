package com.bing.tpa.controller;

import com.bing.tpa.common.Result;
import com.bing.tpa.common.ResultCodeEnum;
import com.bing.tpa.domain.VO.PointLink;
import com.bing.tpa.domain.VO.TopicPointRelationDTO;
import com.bing.tpa.domain.dto.TopicPointDto2;
import com.bing.tpa.domain.dto.UnifiedKnowledgePoint;
import com.bing.tpa.domain.entity.TopicPoint;
import com.bing.tpa.domain.entity.TopicRelation;
import com.bing.tpa.exception.RepeatException;
import com.bing.tpa.service.baseService.TopicPointService;
import com.bing.tpa.service.baseService.TpaSubjectService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.KnowledgeSortUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "自定义知识点接口(仅可对自家学校的知识点网进行修改)")
@RestController
@RequestMapping("/topic/point")
public class TopicPointController<T> {

    @Autowired
    private TopicPointService topicPointService;

    @Autowired
    private TpaSubjectService subjectService;

    @Autowired
    private TpaTeacherService userService;

    @Autowired
    private Result<T> result;


    /**
     * TODO 对学校知识点网的修改采用按步处理机制，
     * 修改分为：1、增：添加新节点，添加新关系
     *         2、改：修改一个节点的信息，修改一个关系
     *         3、删除：删除一个节点(子节点采取级联删除的模式，但是关系需要删除)，删除一个关系
     * **/

//   增加：1：这里使用TopicPoint来接收，可能会有子节点传过来，因此这个除了要将父节点、子节点插入数据库外，还需要处理父子之间的关系relation
//    前端可以通过两种形式来为新的节点添加子节点：1、直接新建子节点，一个参数一个参数的填写。
//    2、通过下面的getAllLevelUnderTow接口获取已有的2级以下的知识点作为新知识点的子节点，这个与谴责的不同在于这个有id，前者没有id
//    所以前者是直接插入topic_points，后者是根据id对原来的进行更新
//    71，52，42
//    增加单个知识点：{
//  "source": "custom",
//  "topTitle": "深度学习优化技术",
//  "secondaryTitle": "第四讲",
//  "content": "模型剪枝、量化、知识蒸馏等轻量化技术",
//  "level": 3,
//  "schoolId": 0,
//  "tsId": 0,
//  "children": []
//}
//    todo 增加多个知识点或者单个知识点（包含和已知父知识节点的关系）
//    todo 需要添加权限检查
    @ApiOperation("新增局部知识点树")
    @PostMapping("add")
    public Result<String> addTopicPoint(@RequestParam Integer schoolId,
                                            @RequestParam Integer tsId,
                                            @RequestParam Integer parentId,
                                            @RequestBody UnifiedKnowledgePoint rootNode) throws RepeatException {
        topicPointService.saveKnowledge(schoolId,tsId,rootNode,parentId);
        return result.build(null,"200","知识点树添加成功");
    }
//  增加：2 TODO 暂时不用
    @ApiOperation("新增一个知识点关系")
    @PostMapping("addRelation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<TopicRelation> addTopicPointRelation(@RequestBody @Valid TopicPointRelationDTO topicPointRelationDTO) {
        return result.success(null);
    }

    @ApiOperation("修改一个知识点内容")
    @PostMapping("modifyKnow")
    public Result<TopicPoint> modifyKnowledge(@RequestBody @Valid TopicPoint point){
        if(point.getTssId()==null)
            return result.build(null,"405","知识点id不能为空");
        TopicPoint topicPoint = topicPointService.modifyNode(point);
        if(topicPoint==null) return result.build(null,"405","知识点不存在");
        return result.build(topicPoint,"200","修改成功");
    }


    @ApiOperation("获取所有的知识点的简要信息")
    @GetMapping("allLevelTow")
    public Result<List<TopicPointDto2>> getAllLevelUnderTow(@RequestParam @Valid Integer schoolId,
                                                            @RequestParam @Valid Integer tsId){
        List<TopicPointDto2> underTow = topicPointService.getUnderTow(schoolId, tsId);
        if (underTow.isEmpty())  return result.build(null,"405","知识点获取失败");
        return result.build(underTow, ResultCodeEnum.SUCCESS);
    }



//    TODO 删除节点：注意level为1的不可以删除，且采取不删除子节点的
    @ApiOperation("根据知识点id删除一个节点")
    @PostMapping("delete")
    public Result<String> deleteKnowledge(  @PathVariable Integer pointId,
                                            @RequestParam Integer schoolId,
                                            @RequestParam Integer tsId){
        boolean delete = topicPointService.deleteKnowledgePoint(pointId, schoolId, tsId);
        if (!delete) return result.fail(null,"删除失败");
        return result.success("删除成功");
    }

//    TODO 创建教学设计和作业、预习任务时可以调用该接口获取知识点提示
    @ApiOperation("知识点选择提示(下拉框结构)")
    @GetMapping("choose/{tsId}/{schoolId}")
    public Result<List<PointLink>> choosePointsPrompt(@PathVariable Integer tsId, @PathVariable Integer schoolId){
        List<PointLink> pointsList = topicPointService.getPointsList(schoolId, tsId);
        if(pointsList.isEmpty()) return result.build(null,"405","获取知识点提示失败！");
        KnowledgeSortUtil.sortByChapterTitle(pointsList);//对知识点进行排序
        return result.build(pointsList, ResultCodeEnum.SUCCESS);
    }


//    TODO 获取目录
    @ApiOperation("获取知识点目录")
    @GetMapping("getContents/{tsId}/{schoolId}")
    public Result<List<PointLink>> getContents(@PathVariable Integer schoolId, @PathVariable Integer tsId){
        List<PointLink> pointsList = topicPointService.getPointsList(schoolId, tsId);
//        去掉多余的，并对章节进行排序
        pointsList.removeIf(pointLink -> pointLink.getRelationType() == 3);
        KnowledgeSortUtil.sortByChapterTitle(pointsList);
        return result.build(pointsList, ResultCodeEnum.SUCCESS);
    }

}
//@PostMapping("/{tsId}")
//    @ApiOperation("新建知识点(不使用)")
//    public Result<TopicPoint> createTopicPoint(@PathVariable Integer tsId,
//                                               @RequestBody @Valid TopicPointDTO topicPointDTO) {
//        // 验证课程是否存在
//        TpaSubject subject = subjectService.getSubjectById(tsId);
//        if (subject == null) {
//            return result.fail(null, "课程不存在");
//        }
//
//        // 获取当前用户
//        TpaTeacher user = userService.getCurrentUser();
//        if (user == null || !user.getSchoolId().equals(subject.getSchoolId())) {
//            return result.fail(null, "无权操作该课程");
//        }
//
//        TopicPoint topicPoint = topicPointService.createTopicPoint(topicPointDTO, tsId, user.getSchoolId());
//        return result.success(topicPoint);
//    }
//
//    @PutMapping
//    @ApiOperation("更新知识点(不使用)")
//    public Result<TopicPoint> updateTopicPoint(@RequestBody @Valid TopicPointDTO topicPointDTO) {
//        TopicPoint topicPoint = topicPointService.updateTopicPoint(topicPointDTO);
//        return result.success(topicPoint);
//    }
//
//    @DeleteMapping("/{tssId}")
//    @ApiOperation("删除知识点(不使用)")
//    public Result<Boolean> deleteTopicPoint(@PathVariable Integer tssId) {
//        boolean result1 = topicPointService.deleteTopicPoint(tssId);
//        return result.success(result1);
//    }
//
//    @ApiOperation("根据课程id获取知识点(不使用)")
//    @GetMapping("/{tsId}")
//    public Result<List<TopicPoint>> getTopicPointsByTsId(@PathVariable Integer tsId) {
//        List<TopicPoint> points = topicPointService.getTopicPointsByTsId(tsId);
//        return result.success(points);
//    }
//    @ApiOperation("根据知识点id获取知识点(不使用)")
//    @GetMapping("/detail/{tssId}")
//    public Result<TopicPoint> getTopicPointById(@PathVariable Integer tssId) {
//        // 获取当前用户
//        TpaTeacher teacher = userService.getCurrentUser();
//        if (teacher == null) {
//            return result.fail(null, "用户未登录");
//        }
//
//        // 查询知识点详情
//        TopicPoint point = topicPointService.getTopicPointById(tssId);
//        if (point == null) {
//            return result.fail(null, "知识点不存在");
//        }
//
//        // 验证权限：确保用户属于同一学校
//        if (!teacher.getSchoolId().equals(point.getSchoolId())) {
//            return result.fail(null, "无权访问该知识点");
//        }
//
//        return result.success(point);
//    }
//
//    // 获取知识点树结构
//    @ApiOperation("根据课程id获取该课程的知识点树结构(不使用)")
//    @GetMapping("/tree/{tsId}")
//    public Result<List<TopicPoint>> getTopicTree(@PathVariable Integer tsId) {
//        TpaTeacher teacher = userService.getCurrentUser();
//        if (teacher == null) {
//            return result.fail(null, "用户未登录");
//        }
//
//        // 验证课程存在且用户有权限
//        TpaSubject subject = subjectService.getSubjectById(tsId);
//        if (subject == null || !teacher.getSchoolId().equals(subject.getSchoolId())) {
//            return result.fail(null, "课程不存在或无权访问");
//        }
//
//        // 获取知识点树
//        List<TopicPoint> tree = topicPointService.getTopicTree(tsId);
//        return result.success(tree);
//    }
//
//    // 复制知识点到其他课程
//    @ApiOperation("复制知识点到其他课程(不使用)")
//    @PostMapping("/copy/{sourceTsId}/{targetTsId}")
//    public Result<Boolean> copyTopicPoints(@PathVariable Integer sourceTsId,
//                                           @PathVariable Integer targetTsId,
//                                           @RequestBody List<Integer> topicIds) {
//        TpaTeacher teacher = userService.getCurrentUser();
//        if (teacher == null) {
//            return result.fail(null, "用户未登录");
//        }
//
//        // 验证源课程和目标课程
//        TpaSubject sourceSubject = subjectService.getSubjectById(sourceTsId);
//        TpaSubject targetSubject = subjectService.getSubjectById(targetTsId);
//
//        if (sourceSubject == null || targetSubject == null) {
//            return result.fail(null, "课程不存在");
//        }
//
//        if (!teacher.getSchoolId().equals(sourceSubject.getSchoolId()) ||
//                !teacher.getSchoolId().equals(targetSubject.getSchoolId())) {
//            return result.fail(null, "无权操作该课程");
//        }
//
//        // 执行复制操作
//        boolean success = topicPointService.copyTopicPoints(sourceTsId, targetTsId, topicIds);
//        return result.success(success);
//    }