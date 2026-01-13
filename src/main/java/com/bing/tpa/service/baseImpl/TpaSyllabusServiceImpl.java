package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bing.tpa.domain.VO.SyllabusResultVo;
import com.bing.tpa.domain.VO.TpaSyllabusWithNeed;
import com.bing.tpa.domain.entity.TpaSyllabus;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.RedisException;
import com.bing.tpa.mapper.TpaSyllabusMapper;
import com.bing.tpa.mapper.TpaTeachDesignMapper;
import com.bing.tpa.modelcall.designCall.ChatWithModel;
import com.bing.tpa.service.baseService.TpaSyllabusService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.service.baseService.TpaTeachDesignService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.CurrentTime;
import com.bing.tpa.utils.RedisConstants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 *  教学大纲的业务：获取经典教学大纲模版，自定义并添加新的教学大纲，AI生成教学大纲模版
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaSyllabusServiceImpl extends ServiceImpl<TpaSyllabusMapper, TpaSyllabus> implements TpaSyllabusService {

    @Autowired
    private TpaTeacherService teacherService;

    @Autowired
    private TpaSyllabusMapper syllabusMapper;

    @Autowired
    private ChatWithModel chatWithModel;

    @Autowired
    private TpaTeachDesignMapper designMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String botId="7473462018583134247";
    @Autowired
    private TpaTeachDesignService tpaTeachDesignService;


    //    tid表示老师的id
    @Override
    public List<TpaSyllabus> getTpaSyllabus(Integer tid) {
//        包装查找条件：经典模版、属于自己的、别人公开的（这个需要使用模糊匹配课程名来查找）
//        先查找该教师教授的课程
        TpaTeacher teacher = teacherService.getCurrentUser();

// 先根据tid字段查找属于该教师的数据
        QueryWrapper<TpaSyllabus> query1 = new QueryWrapper<>();
        query1.eq("author_id", tid.toString());
        List<TpaSyllabus> result = syllabusMapper.selectList(query1);

// 再根据模糊查询查找不属于该教师且is_open为1的数据，限制返回10条
        QueryWrapper<TpaSyllabus> query2 = new QueryWrapper<>();
        query2.like("type", teacher.getTeachLesson())
                .ne("author_id", tid.toString())
                .eq("is_open", 1)
                .last("LIMIT 10");
        List<TpaSyllabus> otherResults = syllabusMapper.selectList(query2);

// 将两部分结果合并
        result.addAll(otherResults);
        result.forEach(r->r.setNum(new Random().nextInt(6)));
        return new ArrayList<>(result);
    }

    @Override
    public SyllabusResultVo getTpaSyllabusFromAI(TpaSyllabusWithNeed withNeed) {
        TpaTeacher teacher = teacherService.getCurrentUser();//获取当前用户信息
//        组装条件
        StringBuilder requirement=new StringBuilder("生成教案");
        requirement.append("年级：").append(teacher.getTeachStage()).append(" ").append(teacher.getStageNum()).
                append("年级\n教授课程：").append(teacher.getTeachLesson()).append("  教材版本：清华大学出版社，第2版\n");
//                append(teacher.getVolume()).append("\n");
        requirement.append("本次人工智能通识课备课主题：\n").append("备课章节：").append(withNeed.getDesignTitle()).
        append("\n章节小节：").append(withNeed.getSecondaryTitle()).append("\n");
        if (withNeed.getExtraRequirements()!=null)
            requirement.append("教案额外需求：").append(withNeed.getExtraRequirements()).append("\n");
        if(withNeed.getExtraRestrictions()!=null)
            requirement.append("教案额外条件限制：").append(withNeed.getExtraRestrictions()).append("\n");
//        教学设计内容和形式的要求
        requirement.append("教学设计内容要求(重点需求！！)：教学设计的每一个教学点都需要足够详细，尤其是这堂课程涉及的每一个知识点需要有丰富的知识点内容分析和教学方案设计，" +
                "生成的教学设计的教学过程部分需要非常详细，！！尤其是知识点的讲解，将每一个知识点的具体内容都详细的进行解释！！\n");
        requirement.append("\n输出格式限制：\n1、各级标题的大小需要有区别让大纲显得层次分明，且每一行都需要一个序号，" +
                "序号的规则如下：一级标题：使用“一、”、“二、”、“三、”等;" +
                "二级标题：使用“1、”、“2、”、“3、”等;" +
                "三级标题：使用“（1）”、“（2）”、“（3）”等;" +
                "四级标题：使用“1>”、“2>”、“3>”等。五级标题：使用“①”、“②”、“③”等; " +
                "\n2、生成的内容要多加一些小标题和内容与之相关的内容" +
                "\n注意：不同的标题之间一定要换行，不要让不同标题之间连在一起了，尤其是教学内容部分的不同序号之间一定要有换行" +
                "\n4、给教学基本信息、教学流程安排这两个部分添加markdown表格的样式。此外其他部分不要添加任何markdown符号！！！" +
                "\n5、注意需要各级标题的颜色深浅都要不一样\n6、注意：一定要按照用户指定的主题和知识点生成相关的内容，不要生成其他与用户指定的知识点不相关的内容!" +
                "\n7、不要生成```这样的markdown符号");//要想让AI听话就需要分点告诉他
        try {
            SyllabusResultVo resultVo = new SyllabusResultVo();
            resultVo.setContent(chatWithModel.chatClient(requirement.toString(), teacher.getUid().toString(),botId));
            resultVo.setDesignTitle(withNeed.getTopTitle());
            resultVo.setSecondaryTitle(withNeed.getSecondaryTitle());
            return resultVo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    syllabus这个里面的教学设计的id默认就是刚刚创建的那一条教学设计的id
//    authorId也是前端会保存该用户的id，然后将这个id赋给TpaSyllabus这个里面authorId

//    这里的TpaSyllabus需要包含的数据：1、大纲文本数据content   2、教学设计设计的id：tdId   3、教师的id 4、如果是选择的已有的模版就需要大纲id：syllabusId
    @Override
    public Integer saveSyllabus(TpaSyllabus syllabus, Integer tdId) throws RedisException {
//        直接将大纲文本数据初始化到redis中,并进行定时,相当于tid这个教学设计引用了这个大纲
        if(tdId!=0&&syllabus.getSyllabusId()!=null) {//这里检查大纲id不为null表示如果这个只是单纯地想要保存大纲，就不需要将这个初始化到redis中
                //  这里不需要判断redis中是否有该数据，因为在流程走到这里之前，没有给该数据添加时间限制，但是可能会被自动写到数据库后删除，
//              所以在历史记录里面修改教学设计时需判断是否该数据，没有就需要添加，有就直接往里面写
//            SyllabusId()!=null表示该大纲已经存在了，这个是从创建教学设计的页面猴子那个选择的大纲，这里就不是报错大纲了，而是将大纲和教学设计关联

//            更新该大纲与教学设计之间的应用关系字段tdId
            List<Integer> tdIds;
            if (syllabus.getTdId()==null){
                tdIds = new ArrayList<>();
                tdIds.add(tdId);
                syllabus.setTdId(new Gson().toJson(tdIds));
            }else {
                tdIds = new Gson().fromJson(syllabus.getTdId(), new TypeToken<List<Integer>>() {
                }.getType());
                tdIds.add(tdId);
            }
            boolean update = lambdaUpdate().eq(TpaSyllabus::getSyllabusId,syllabus.getSyllabusId())
                    .set(TpaSyllabus::getTdId, new Gson().toJson(tdIds))
                    .update();
            if (!update) return -1;
//            将大纲数据存到redis中
            try {
                stringRedisTemplate.opsForHash().put(RedisConstants.DESIGN_ID_KEY + tdId, "content", syllabus.getContent());
                stringRedisTemplate.expire(RedisConstants.DESIGN_ID_KEY + tdId,RedisConstants.DESIGN_ID_TTL, TimeUnit.MINUTES);
                log.error(CurrentTime.getTime()+"已初始化redis中对应的hash的值");
//                todo 同时也将初始化大纲同步到数据库中
                tpaTeachDesignService.lambdaUpdate()
                        .eq(TpaTeachDesign::getTdId,tdId)
                        .set(TpaTeachDesign::getContent,syllabus.getContent())
                        .update();
                log.error(CurrentTime.getTime()+"同时也初始化了数据库");
            } catch (Exception e) {
                throw new RedisException("redis中教学设计初始化失败,可能该教学设计正在写入redis中");
            }
        } else {
//            单纯地进行大纲的保存,不和任何的教学设计进行关联！！！！
//            TpaTeacher teacherById = getTeacherById(Integer.parseInt(syllabus.getAuthorId()));
            TpaTeacher teacherById = teacherService.getCurrentUser();
//            设置大纲的名字
            syllabus.setType(teacherById.getTeachLesson());
            if (syllabus.getName()==null)
                syllabus.setName(teacherById.getTeachLesson()+"模版--万能模版"+teacherById.getUid()+"-"+new Random().nextInt(100));

            syllabus.setCreateTime(CurrentTime.getTime());
            syllabus.setAuthorId(teacherById.getUid().toString());
            boolean save = save(syllabus);
            if (!save) return -1;
        }

        return 1;
    }

    private TpaTeacher getTeacherById(Integer id){
        return teacherService.getById(id);
    }

    private TpaTeachDesign getDesignById(Integer tdId) {
        return designMapper.selectById(tdId);
    }
}
