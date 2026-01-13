package com.bing.tpa.service.baseImpl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.VO.ClassInfoVo;
import com.bing.tpa.domain.VO.StudentClassVo;
import com.bing.tpa.domain.VO.StudentListVo;
import com.bing.tpa.domain.dto.StudentExcel;
import com.bing.tpa.domain.entity.StudentClass;
import com.bing.tpa.domain.entity.TpaHomeworkSummary;
import com.bing.tpa.domain.entity.TpaStudent;
import com.bing.tpa.domain.query.PageDTO;
import com.bing.tpa.mapper.StudentClassMapper;
import com.bing.tpa.mapper.TpaClassMapper;
import com.bing.tpa.domain.entity.TpaClass;
import com.bing.tpa.mapper.TpaHomeworkSummaryMapper;
import com.bing.tpa.mapper.TpaPreviewTrackMapper;
import com.bing.tpa.service.baseService.StudentClassService;
import com.bing.tpa.service.baseService.TpaClassService;
import com.bing.tpa.service.baseService.TpaStudentService;
//import com.bing.tpa.utils.ExcelUtil;
import io.swagger.models.auth.In;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaClassServiceImpl extends ServiceImpl<TpaClassMapper, TpaClass> implements TpaClassService {

    @Autowired
    private TpaStudentService studentService;

    @Autowired
    private StudentClassService studentClassService;

//    @Autowired
//    private ExcelUtil excelUtil;

    @Autowired
    private TpaClassMapper classMapper;

    @Autowired
    private StudentClassMapper studentClassMapper;

    @Autowired
    private TpaHomeworkSummaryMapper summaryMapper;

    @Autowired
    private TpaPreviewTrackMapper trackMapper;

    private static Integer studentId;
    @Data
    static
   public class ClassData{
        private Integer cid;
        private String classCode;
    }

//    添加班级
    @Override
    public ClassData addClass(TpaClass tpaClass) {
//        为班级生成一个唯一的编码
//         使用uuid生成一个长码，截去前六位为加课码
        UUID uuid = UUID.randomUUID();
        tpaClass.setClassCode(uuid.toString());
        tpaClass.setShortCode(uuid.toString().substring(0,6));
        ClassData info = new ClassData();
        boolean save = save(tpaClass);
        info.setCid(tpaClass.getCid());
        info.setClassCode(tpaClass.getShortCode());
        if(save) return info;
        return null;
    }

//    @Override
//    public boolean addList(String path, Integer classId) {
////先将数据从excel表中取出来
//        List<Object> students = excelUtil.readExcel(path, StudentExcel.class);
////        List<Integer> studentId=new ArrayList<>();
//        List <StudentClass> studentClassList=new ArrayList<>();
//        int addNum=0;
//        for(Object se:students){
////            检查Object是否为对应类型
//            if(se instanceof StudentExcel){
//                StudentExcel student = (StudentExcel) se;
////                先将StudentExcel的数据转到TpaStudent中，分别后续的插入操作
//                TpaStudent tpaStudent = new TpaStudent();
//                tpaStudent.setStuName(student.getName());
//                tpaStudent.setStuNum(student.getStuNum().replaceAll("\\.00$", ""));
//                tpaStudent.setStuStage(student.getStuStage());
////              新增一个StudentClass关系，方便后续将学生太那几到班级中
//                StudentClass studentClass = new StudentClass();
//                studentClass.setCid(classId);
////               检查该学生是否在学生表中
//                Integer  sid= studentService.isExit(tpaStudent);
////                exit=null说明没找到，就需要新增学生到学生表中,不等于null就说明有这个学生，就将这个学生的id加入到List中
//                if(sid==null){
//                    boolean save = studentService.save(tpaStudent);
//                    if(save) {
//                        studentClass.setSid(tpaStudent.getSid());//设置班级和学生的关系
//                        addNum++;
//                    }
//                    else throw new NullPointerException("学生添加失败");
//                }else studentClass.setSid(sid);//设置班级和学生的关系
//                studentClassList.add(studentClass);
//            }
//        }
////           添加学生与班级的关系
//        boolean save = studentClassService.saveBatch(studentClassList);
////          修改该班级的人数
//        Integer addOnePerson = classMapper.updatePersonNUm(classId, addNum);
//        if (addOnePerson==0) return false;
//        return save;
//    }

    @Override
    public Integer addOne(StudentClassVo student) {
//        先将学生添加到数据库中，如果添加成功就获得该学生的id
//        先查看学生表中是否有该学生，有就返回学生id，没有就是null，就需要先添加学生到学生表，在添加学生与班级的关系
        Integer exit = studentService.isExit(student);
        if(exit==null)
            studentId = studentService.addOneStu(student);
        else studentId=exit;
        List<StudentClass> studentClasses=new ArrayList<>();
        if(studentId != -1){
//            将学生和班级的关系
            StudentClass aClass = new StudentClass();
            aClass.setSid(studentId);
            aClass.setCid(student.getCid());
            studentClasses.add(aClass);
            boolean b = studentClassService.addConnection(studentClasses);
            if(!b) return -1;
//            向班级新添加一个人
            classMapper.updatePersonNUm(student.getCid(),1);
        }
        return 1;
    }

//    根据老师的id获取班级列表
    @Override
    public PageDTO<TpaClass> getListByTeacherId(Integer tid) {
        QueryWrapper<TpaClass> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("tid",tid);
        IPage<TpaClass> page=new Page<>(1,5);
        IPage<TpaClass> tpaClassIPage = classMapper.selectPage(page, queryWrapper);
        PageDTO<TpaClass> pageDTO=new PageDTO<TpaClass>();
        pageDTO.setList(tpaClassIPage.getRecords());
        pageDTO.setPages(tpaClassIPage.getPages());
        pageDTO.setTotal(tpaClassIPage.getTotal());
        return pageDTO;
    }

    @Override
    public ClassInfoVo classInfo(Integer cid) {
        TpaClass classInfo = getById(cid);
        ClassInfoVo classInfoVo = new ClassInfoVo();
        BeanUtil.copyProperties(classInfo,classInfoVo);
//      根据学生历来每一次作业和预习任务的完成情况综合分数进行一个排名，每次进入班级都计算一次
//        根据班级id，将所有学生都找出来
        List<StudentListVo> stuList = studentClassMapper.getList(cid);
        if (stuList.size()==0) return classInfoVo;//如果该班级没有人就不需要进行下面的计算了
        int totalSize = stuList.size();
        int oneThird = (int) Math.ceil(totalSize / 3.0);
        int twoThirds = oneThird * 2;
        Map<Integer,Double> scoreMap=new HashMap<>();
        for (StudentListVo stu:stuList){
//            根据学生的id获取学生做的作业分数
            Integer homeworkScores = summaryMapper.totalScore(stu.getSid());//如果没分数的就不会被查找出来。即只要被查出来的都是有分数的
//            预习任务分数
            Integer previewScores = trackMapper.selectScore(stu.getSid());//如果没分数的就不会被查找出来。即只要被查出来的都是有分数的
            if (previewScores==null) previewScores=0;
            if (homeworkScores==null) homeworkScores=0;
            if (previewScores==0&&homeworkScores==0) continue;
            scoreMap.put(stu.getSid(),homeworkScores * 0.5 + previewScores * 0.5);
        }
        if (scoreMap.size()==0) return classInfoVo;//如果该班级没有学习任务的任务完成的人就直接返回
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(scoreMap.entrySet());
        // 对 List 进行排序
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        // 将排序后的 List 转换回 Map
        Map<Integer, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        stuList.forEach(s->{
            int rank = getRank(sortedMap, s.getSid());
            s.setRanking(rank);//这里设置该学生的排名
            if (rank<=oneThird) s.setEvaluation("优秀，请继续保持");
            else if(rank<=twoThirds) s.setEvaluation("表现良好");
            else s.setEvaluation("表现比较差，请继续加油");
//            将排名更新到数据库中
            UpdateWrapper<StudentClass> queryWrapper=new UpdateWrapper<>();
            queryWrapper.eq("sid",s.getSid()).eq("cid",cid).set("ranking",rank)
                            .set("evaluation",s.getEvaluation());
            studentClassMapper.update(null,queryWrapper);
        });
        stuList.sort(Comparator.comparingInt(StudentListVo::getRanking));
        classInfoVo.setStuRanking(stuList);//设置班级的排名
        return classInfoVo;
    }

    public static int getRank(Map<Integer, Double> sortedMap, int targetKey) {
        int rank = 1; // 初始化排名为 1
        for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
            if (entry.getKey().equals(targetKey)) {
                return rank; // 找到目标键，返回当前排名
            }
            rank++; // 没找到目标键，排名加 1
        }
        return -1; // 如果目标键不存在，返回 -1
    }
}
