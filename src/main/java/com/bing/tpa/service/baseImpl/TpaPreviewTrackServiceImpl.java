package com.bing.tpa.service.baseImpl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.bing.tpa.Thread.MarkAndGenerateMaterialsThread;
import com.bing.tpa.domain.VO.*;
import com.bing.tpa.domain.entity.*;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.*;
import com.bing.tpa.service.baseService.TpaPreviewTrackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.utils.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.swagger.models.auth.In;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaPreviewTrackServiceImpl extends ServiceImpl<TpaPreviewTrackMapper, TpaPreviewTrack> implements TpaPreviewTrackService {

    @Autowired
    private TpaHomeworkDetailsMapper detailsMapper;

    @Autowired
    private TpaPreviewTaskMapper taskMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TpaHomeworkTrackMapper trackMapper;

    @Autowired
    private MarkAndGenerateMaterialsThread generateMaterialsThread;

    @Autowired
    private TpaStudentMapper studentMapper;

    @Autowired
    private StudentClassMapper studentClassMapper;



    @Override
    public List<TpaHomeworkDetails> saveTrackToRedis(Integer id, Integer ptId) throws FormatException {
//        先将该预习任务下的预习题获取到，预习资料在点击开始预习的时候就获得了
        QueryWrapper<TpaHomeworkDetails> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("pt_id",ptId)
                .and(wrapper -> wrapper.isNull("sid"));//这个只需要匹配出预习题就行，不需要匹配个性化题目（个性化题目也是属于预习任务的，所以需要ptId，但是没有hid）
//        1.获取预习题
        List<TpaHomeworkDetails> details = detailsMapper.selectList(queryWrapper);
//        这里是点击进入预习题，在这之前点击开始预习任务时就已经将一条记录云溪任务完成情况的数据添加到preview_track表中了！！！

            int i=0;
//        4.不将预习题的追踪数据放到redis中，因为这里只是点击进入了预习任务，预习任务里面有两个选择：1、学习预习资料 2、做预习题
        for (TpaHomeworkDetails details1:details) {
//            1、答案置空和选项转数组
            details1.setCorrectAnswer(null);
            details1.setAnswerAnalysis(null);
            if(details1.getSelections()!=null)
             details1.setOptions(new Gson().fromJson(details1.getSelectOption(),new TypeToken<List<String>>() {}.getType()));//选项转数组
            //           2、包装储存到redis中的题目记录对象
            TpaHomeworkTrack qTrack = new TpaHomeworkTrack();
            qTrack.setPtId(ptId);//在将这道题的完成记录写进redis前就设置该题目所属的预习任务
            qTrack.setSid(id);//在将这道题的完成记录写进redis前就设置该题目所属的学生，即这条记录是哪个学生做的
            qTrack.setQid(details1.getQid());
            qTrack.setStatus(0);
            if(i==0) qTrack.setAttemptTime(CurrentTime.getTime());
//            ***************** 将数据写到redis中 ******************
            Map<String, String> stringMap = MapToString.convertValuesToString(BeanUtil.beanToMap(qTrack));//将对象中的字段转为string
            stringRedisTemplate.opsForHash().putAll(RedisConstants.PREVIEW_ID_KEY +id+":"+ptId+":"+details1.getQid(),stringMap);
            i++;
        }
        return details;
    }

    //    修复因为退出而丢失的答题数据
    @Override
    public List<TpaHomeworkDetails> recovery(Integer id, Integer ptId) throws FormatException {
//        String pattern= RedisConstants.PREVIEW_ID_KEY+id+":"+ptId+":"+"*";
//        Set<String> keys = stringRedisTemplate.keys(pattern);
        QueryWrapper<TpaHomeworkDetails> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("pt_id",ptId)
                .and(wrapper -> wrapper.isNull("sid"));
        List<TpaHomeworkDetails> details = detailsMapper.selectList(queryWrapper);
        for(TpaHomeworkDetails details1:details){
            String key=RedisConstants.PREVIEW_ID_KEY+id+":"+ptId+":"+details1.getQid();
            recoveryAnswer(details1, key, stringRedisTemplate, details);
        }
        return details;
    }

//    将之前做的答案恢复
    static void recoveryAnswer(TpaHomeworkDetails details1, String key, StringRedisTemplate stringRedisTemplate, List<TpaHomeworkDetails> details) {
        String answer = (String) stringRedisTemplate.opsForHash().get(key, "answer");//这里是将之前的回答拿出来了，不涉及将新的数据写入redis，因此不用处理时间
        if (answer==null) {
            details.remove(details1);
            return;
        }
        details1.setCorrectAnswer(answer);
        details1.setAnswerAnalysis(null);
        if(details1.getSelectOption()!=null)
            details1.setOptions(new Gson().fromJson(details1.getSelectOption(),new TypeToken<List<String>>() {}.getType()));
    }

    @Override
    public Integer submitTrack(Integer ptId, Integer uid, Integer complete) throws Exception {
//        将该学生的这歌预习任务的预习题的追踪数据从redis中都拿出来
        String pattern=RedisConstants.PREVIEW_ID_KEY+uid+":"+ptId+":"+"*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        int completeNum=0;
        assert keys != null;
        for (String key:keys){
            TpaHomeworkTrack track1 = TpaHomeworkTrackServiceImpl.getTrackFromRedis(key, stringRedisTemplate);
//            处理其他数据
            if (track1.getAnswer()!=null) completeNum++;
            if (track1.getHtId()==null){
                int insert = trackMapper.insert(track1);
                if(insert==0) return 0;
                    //这里如果是中途退出据会将数据写入数据库，并将生成的id保存到redis中，后续再提交或中途退出时就根据作业的id进行更新而不是插入
                else stringRedisTemplate.opsForHash().put(key,"htId",track1.getHtId().toString());
            }else {
                trackMapper.updateById(track1);
            }
//          如果complete等于1.就需要将redis中的数据删除掉
            if (complete==1)//如果是中途退出就只会将数据储存到数据库中，不会将redis中的数据删除！！！！，这样后续再进来的时候就可直接从redis中获取之前的数据
            stringRedisTemplate.delete(key);

        }
//        如果complete等于1表示用户做完提交了，而不是中途退出，所以要更新一下他这个预习任务的完成状态
        boolean flag=false;
        if (complete==1){
            LambdaUpdateChainWrapper<TpaPreviewTrack> set = lambdaUpdate()
                    .eq(TpaPreviewTrack::getPtId, ptId)
                    .eq(TpaPreviewTrack::getSid, uid)
                    .set(TpaPreviewTrack::getQuestionFinish, 1)
                    .set(TpaPreviewTrack::getCompleteNum, completeNum);
            flag=isComplete(ptId,uid,1);
            if (flag){
//                设置预习任务完成时间
                set.set(TpaPreviewTrack::getFinishTime,CurrentTime.getTime());
//                预习任务完成了在预习任务表中完成人数加1！！！！！！
                taskMapper.updateCompleteNum(ptId);
            }
            boolean update = set.update();
            if (!update) return 0;
//     同时这表示两个都完成了，那么预习任务就算完成了，所以需要先批改预习题然后根据完成情况推荐预习资源
//      所以这个线程池需要三个功能：1、批改预习资料题目 2、批改预习题  3、生成个性化资料，前面两个根据传入的第三个参数来判断，1表示批改预习资料题目，2表示批改预习题
//         这里传2，批改预习题
            //如果预习资料题完成了，那么预习资料题目也一定批改了，这里需要批改预习题+生成个性化资料
            //预习资料题没做完，这里就只批改预习题就行，不删除个性化资料
            generateMaterialsThread.gradeAndGenerateDate(uid,ptId,2, flag);
        }
        return 1;
    }

    //        根据PreviewTextVo中的ptId和uid来对附加题进行更新
    @Override
    public Integer submitExtraQuestionAnswer(PreviewTextVo previewTextVo) throws Exception {
        boolean flag=false;
        LambdaUpdateChainWrapper<TpaPreviewTrack> set = lambdaUpdate()
                .eq(TpaPreviewTrack::getPtId, previewTextVo.getPtId())
                .eq(TpaPreviewTrack::getSid, previewTextVo.getUid())
                .set(TpaPreviewTrack::getDataInquiry, new Gson().toJson(previewTextVo.getDataInquiry()))
                .set(TpaPreviewTrack::getTextAnswer, new Gson().toJson(previewTextVo.getAnswer()))
                .set(TpaPreviewTrack::getTextFinish, 1);//最重要的是将预习资料的完成状态改为已完成1
        flag=isComplete(previewTextVo.getPtId(),previewTextVo.getUid(),2);
        if (flag){
//            预习题也完成了就设置预习任务完成时间
            set.set(TpaPreviewTrack::getFinishTime,CurrentTime.getTime());//2025-04-04 19:55:48
            taskMapper.updateCompleteNum(previewTextVo.getPtId());
        }
        boolean update = set.update();
        if (!update) return 0;

//        调用批改预习题的线程池
        generateMaterialsThread.gradeAndGenerateDate(previewTextVo.getUid(),previewTextVo.getPtId(),1,flag);
        return 1;
    }

    @Override
    public PreviewCompleteVo selectAllInfo(Integer ptId, Integer uid) {
//        先获取预习任务整体完成情况
        TpaPreviewTrack track = lambdaQuery().eq(TpaPreviewTrack::getPtId, ptId).eq(TpaPreviewTrack::getSid, uid).one();
        PreviewCompleteVo completeVo = new PreviewCompleteVo();
        completeVo.setTrack(track);
        TpaPreviewTask task = taskMapper.selectById(ptId);
        List<TrackWithDetails> questionTrack=new ArrayList<>();
//        获取该预习任务的题目
        List<TpaHomeworkDetails> details = detailsMapper.selectPtQuestion(ptId);
        details.forEach(d->{
            d.setQcontent(d.getQcontent().replace("\\",""));
            LambdaQueryWrapper<TpaHomeworkTrack> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(TpaHomeworkTrack::getQid,d.getQid()).eq(TpaHomeworkTrack::getSid,uid);
            TpaHomeworkTrack track1 = trackMapper.selectOne(queryWrapper);
            TrackWithDetails track2 = new TrackWithDetails();
            BeanUtil.copyProperties(track1,track2);//这里不能让TpaHomeworkDetails放在TpaHomeworkTrack，不然redis储存不了
            track2.setPoint(d.getQtitle());//设置题目的知识点
            if (d.getSelectOption()!=null){
                List<String> list = new Gson().fromJson(d.getSelectOption(), new TypeToken<List<String>>() {}.getType());
                List<String> newOption=new ArrayList<>();
                list.forEach(l-> newOption.add(l.replace("\\", "")));
                d.setOptions(newOption);
                d.setSelections(d.getSelections().replace("\\",""));
            }
            track2.setHomeworkDetails(d);
            questionTrack.add(track2);
        });
        AtomicInteger trueNum= new AtomicInteger();
        Map<String, Integer> knowledgePointErrors = new HashMap<>();
        questionTrack.forEach(q->{
            if (q.getIsCorrect()==1) trueNum.getAndIncrement();
            else {
                String knowledgePoint = q.getPoint();
                if (knowledgePoint != null && !knowledgePoint.isEmpty()) {
                    knowledgePointErrors.put(knowledgePoint, knowledgePointErrors.getOrDefault(knowledgePoint, 0) + 1);
                }
            }
        });
        // 将Map转换为List，以便排序
        List<Map.Entry<String, Integer>> sortedKnowledgePoints = new ArrayList<>(knowledgePointErrors.entrySet());
        // 按错误次数从高到低排序
        sortedKnowledgePoints.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        completeVo.setMistakePoint(sortedKnowledgePoints);
        completeVo.setTrueNum(trueNum.get());
        completeVo.setFalseNum(track.getQuestionNum()- trueNum.get());
        BigDecimal bd = getRete(task.getQuestionsNum(), trueNum);
        completeVo.setTrueRate(bd.doubleValue()+"%");
        completeVo.setCompleteTime(TimeUtils.getTimeDeff(track.getStartTime(),track.getFinishTime()));
        completeVo.setQuestionTrackList(questionTrack);
        completeVo.setPreviewName(task.getPreviewName());
        completeVo.setPtitle(task.getPtitle());
        completeVo.setSecondaryTitle(task.getSecondaryTitle());
        completeVo.setScore(task.getQuestionsGrade());
        TpaStudent student = studentMapper.selectById(track.getSid());
        completeVo.setStuName(student.getStuName());
        return completeVo;
    }

    @Override
    public SpecialDataVo specialData(Integer uid, Integer ptId) {
//        先根据ptid获取预习任务和预习任务完成情况
        SpecialDataVo dataVo = new SpecialDataVo();
        TpaPreviewTask task = taskMapper.selectById(ptId);
        dataVo.setPreviewName(task.getPreviewName());
        dataVo.setPtitle(task.getPtitle());
        dataVo.setSecondaryTitle(task.getSecondaryTitle());
        TpaPreviewTrack previewTrack = lambdaQuery().eq(TpaPreviewTrack::getPtId, ptId)
                .eq(TpaPreviewTrack::getSid, uid).one();
        if (previewTrack==null) return null;//都没有开始预习就不可能有个性化资源
        dataVo.setQNum(previewTrack.getAddQuestion());
        dataVo.setSupplement(previewTrack.getSupplement());
//        获取个性化推荐题
        List<TpaHomeworkDetails> details = detailsMapper.selectSpecial(uid, ptId);
        AtomicInteger qScore= new AtomicInteger();
        details.forEach(d->{
            d.setQcontent(d.getQcontent().replace("\\",""));
            if (d.getSelectOption()!=null){
                List<String> optionList= new Gson().fromJson(d.getSelectOption(), new TypeToken<List<String>>() {}.getType());
                d.setOptions(optionList.stream()
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList()));
            }

            qScore.addAndGet(d.getDefaultScore());//记录个性化题目的总分
        });
        dataVo.setScore(qScore.get());
        dataVo.setSpecialQuestion(details);
        return dataVo;
    }

    @Override
    public List<TpaPreviewTrack> getStudentListAndPreviewSituationVo(Integer ptId, Integer cid) {
//        根据班级id获取所有学生
        List<StudentListVo> studentList = studentClassMapper.getList(cid);
//        获取本次预习任务的学生完成数据
        List<TpaPreviewTrack> previewTracks = lambdaQuery().eq(TpaPreviewTrack::getPtId, ptId).list();
        previewTracks.forEach(p->{
            TpaStudent student = studentMapper.selectById(p.getSid());
            p.setStuCode(student.getStuNum());
            if (p.getFinishTime()!=null&&p.getStartTime()!=null)
            p.setCompleteTime(TimeUtils.getTimeDeff(p.getStartTime(),p.getFinishTime()));
        });
        for (StudentListVo vo:studentList){
            List<TpaPreviewTrack> tracks = previewTracks.stream()
                    .filter(track -> {
                        if (track.getSid()!=null&&track.getQuestionFinish()!=0&&track.getTextFinish()!=0)
                        return track.getSid().equals(vo.getSid());
                        return false;
                    }).collect(Collectors.toList());
            if (tracks.size()==0){
                TpaPreviewTrack track = new TpaPreviewTrack();
                TpaStudent student = studentMapper.selectById(vo.getSid());
                track.setStuCode(student.getStuNum());
                track.setPtId(ptId);
                track.setSid(vo.getSid());
                track.setStuName(vo.getStuName());
                track.setCompleteNum(0);
                track.setCompleteTime("--");
                track.setTextFinish(0);
                track.setQuestionFinish(0);
                previewTracks.add(track);
            }else tracks.get(0).setStuName(vo.getStuName());
        }
        return previewTracks;
    }

    @NotNull
    public static BigDecimal getRete(Integer questionNum, AtomicInteger trueNum) {
        assert questionNum!=0;
        double v = Double.parseDouble(String.valueOf(trueNum.get()));
        return new BigDecimal(v / questionNum * 100.0).setScale(2, RoundingMode.HALF_UP);
    }

    //    检查预习任务和预习题是否完成
    private boolean isComplete(Integer ptId,Integer uid,Integer select){
        TpaPreviewTrack oneTrack = lambdaQuery()
                .eq(TpaPreviewTrack::getPtId, ptId)
                .eq(TpaPreviewTrack::getSid, uid)
                .one();
        if (select==1) return oneTrack.getTextFinish()==1;//只想知道预习资料的题目是否完成
        else if(select==2) return oneTrack.getQuestionFinish()==1;//只想知道预习题是否完成
        else return oneTrack.getQuestionFinish()==1&&oneTrack.getTextFinish()==1;//两个是否都完成
    }


}
