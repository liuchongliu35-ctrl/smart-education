package com.bing.tpa.service.baseImpl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.Thread.MarkAndGenerateMaterialsThread;
import com.bing.tpa.domain.VO.HomeworkCompleteVo;
import com.bing.tpa.domain.VO.TrackUpdateVo;
import com.bing.tpa.domain.VO.TrackWithDetails;
import com.bing.tpa.domain.entity.*;
import com.bing.tpa.exception.FormatException;
import com.bing.tpa.mapper.*;
import com.bing.tpa.service.baseService.TpaHomeworkTrackService;
import com.bing.tpa.service.baseService.TpaPreviewTrackService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaHomeworkTrackServiceImpl extends ServiceImpl<TpaHomeworkTrackMapper, TpaHomeworkTrack> implements TpaHomeworkTrackService {

    @Autowired
    private TpaHomeworkDetailsMapper detailsMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TpaHomeworkSummaryMapper summaryMapper;

    @Autowired
    private TpaStudentMapper studentMapper;

    @Autowired
    private MarkAndGenerateMaterialsThread markThread;

    @Autowired
    private TpaHomeworkMapper homeworkMapper;

    @Autowired
    private TpaHomeworkTrackMapper trackMapper;
    @Autowired
    private TpaTeacherService tpaTeacherService;

    //    注意：这个返回的题目是用来做的，而不是用来预览的，是没有答案和解析的！！！！！！！！！
    @Override
    public List<TpaHomeworkDetails> saveTrackToRedis(Integer id, Integer hid) throws FormatException {
//      1、先将该作业的所有题目的id获取到
        QueryWrapper<TpaHomeworkDetails> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("hid",hid);
        List<TpaHomeworkDetails> details = detailsMapper.selectList(queryWrapper);

//        2、用户点击开始答题后触发这个接口，在将数据放到redis中的同时，也需要在作业完成情况表中创建一条记录该学生完成该作业的情况
        QueryWrapper<TpaHomeworkSummary> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.eq("hid",hid).eq("uid",id);
        boolean exists = summaryMapper.exists(queryWrapper1);
        int insert = 0;
        if (!exists){//检查作业个学生的记录数据是否是一一对应的!!!!
            TpaHomeworkSummary tpaHomeworkSummary = new TpaHomeworkSummary();
            tpaHomeworkSummary.setHid(hid);
            tpaHomeworkSummary.setUid(id);
            tpaHomeworkSummary.setName(studentMapper.selectById(id).getStuName());
            tpaHomeworkSummary.setQuestionNum(details.size());
            tpaHomeworkSummary.setIsComplete(2);//2表示用户已经开始做了
             insert=summaryMapper.insert(tpaHomeworkSummary);
        }
//       3、组装记录做题记录的对象，后续将完整对象插入到redis中
        int i=0;
        for (TpaHomeworkDetails qid:details){
//           3.1、顺便将题目的答案和解析设置为null,前端照样将这两个字段放到页面中，只不过是空的，后续恢复答题情况的时候将CorrectAnswer这个设为存在redis中的回答
            qid.setCorrectAnswer(null);
            qid.setAnswerAnalysis(null);
//           3.2、包装储存到redis中的题目记录对象
            TpaHomeworkTrack track = new TpaHomeworkTrack();
            track.setHid(hid);//设置这条题目完成数据属于哪一个作业
            track.setSid(id);//设置这条题目完成数据属于哪一个学生，即是哪一个学生做的
            track.setQid(qid.getQid());
            track.setStatus(1);//给每道题的完成状态设为正在完成
//           3.3设置第一个题目开始的时间
            if (i==0)
             track.setAttemptTime(CurrentTime.getTime());
//            3.4、将这条数据插入到redis中,tid:hid:qid,且不设置定时删除
            Map<String, Object> trackInRedis = BeanUtil.beanToMap(track);
            Map<String, String> stringMap = MapToString.convertValuesToString(trackInRedis);//将map中所有的value转为字符串!!!
//            key:  homework:1:23:12  表示作业题目id为1的学生,作业23,第12题的答题状况对应的key
            stringRedisTemplate.opsForHash().putAll(RedisConstants.HOMEWORK_ID_KEY+id+":"+hid+":"+qid.getQid(),stringMap);//题目不设置redis的限时删除也没有定期更新到数据库中，后续提交在一起更新到数据库中
//            3.5将选择题的选项转为数组的形式!!!!!!!!!!!不是选择题就不改
            if (qid.getSelections()!=null)
             qid.setOptions(new Gson().fromJson(qid.getSelectOption(),new TypeToken<List<String>>() {}.getType()));
            i++;
        }
        return details;
    }
//      将map的value转为String

//  需要更新的字段：status改为1表示正在完成  answer completionTime取当前的时间 timeSpent取开始时间和完成时间的差值 以及下一道题的开始解题时间：attempt_time
//    需要将要更改的这个题目完成记录对象这一整个对象从redis中拿出来，在进行更改后再插入进去，因为key一样，所以redis会将其认为是对这个数据的更新
    @Override
    public Integer updateQuestionAnswer(TrackUpdateVo trackUpdateVo,String keyStr) {
//        TpaTeacher user = tpaTeacherService.getCurrentUser();
//      1、先根据key将整个对象从redis中拿出来
//        本题记录对象,根据不同的地方调用传过来不同的key前缀ketStr，如果是作业题调用就是homework:学生id:学习任务id:题目id 如果是预习题就是preview:
//        todo trackUpdateVo.getId()这是学生id！！！不是教师id
        String TRACK_KEY1=keyStr+trackUpdateVo.getId()+":"+trackUpdateVo.getTid()+":"+trackUpdateVo.getQid();
        String TRACK_KEY2=keyStr+trackUpdateVo.getId()+":"+trackUpdateVo.getTid()+":"+trackUpdateVo.getNextQid();

        Map<Object, Object> trackMap = stringRedisTemplate.opsForHash().entries(TRACK_KEY1);

        if (trackMap.isEmpty()) return 0;
//
        TpaHomeworkTrack track = BeanUtil.fillBeanWithMap(trackMap, new TpaHomeworkTrack(), false);
        track.setAttemptTime(TimeUtils.stringToLocalDateTime((String) trackMap.get("attemptTime")));//将字符串的时间转为正常时间
        log.error("这道题的开始时间："+track.getAttemptTime()+"\n");
//       2、修改对象的字段
        track.setAnswer(trackUpdateVo.getAnswer());//①学生答案！！！
        track.setStatus(2);//②题目完成状态!!!，设为已完成，2表示已完成
        LocalDateTime now = CurrentTime.getTime();
        log.error("当前时间："+now);
        track.setCompletionTime(now);//③题目完成时间!!!
        log.error("题目完成时间："+now);
//        获取时间差来确定做题耗时
        String timeDifference = TimeUtils.getTimeDeff(track.getAttemptTime(), now);
        track.setTimeSpent(timeDifference);//④题目完成用时!!!
        log.error("做题耗时："+timeDifference);
//       3、将这个更新过的key-value更新到redis中
//        将track的属性转为字符串!!!!!!!  不然redis就会报错
        Map<String, String> stringMap = MapToString.convertValuesToString(BeanUtil.beanToMap(track));
        stringRedisTemplate.opsForHash().putAll(TRACK_KEY1,stringMap);
//       4、修改下一个题的开始时间
        log.error("下一道题开始时间："+CurrentTime.getTime().toString()+"\n");
        if(trackUpdateVo.getNextQid()!=0&&trackUpdateVo.getQid()<trackUpdateVo.getNextQid())//如果是做完后反过来检查更改的话就不需要将下一题的id传过来，不需要记录下一题的开始时间
         stringRedisTemplate.opsForHash().put(TRACK_KEY2,"attemptTime",CurrentTime.getTime().toString());
        return 1;
    }



    //    修复因为退出而丢失的答题数据
    @Override
    public List<TpaHomeworkDetails> recovery(Integer hid, Integer id) throws FormatException {
//       先将原来保存到redis中的答题数据拿出来1:12:*    redis对key的格式没有限制所以这样有id数字组成的是可以的
//        再将题目数据拿出来
        QueryWrapper<TpaHomeworkDetails> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("hid",hid);
        List<TpaHomeworkDetails> details = detailsMapper.selectList(queryWrapper);
//        直接遍历题目完成记录，然后根据题目的
        for (TpaHomeworkDetails details1:details){
//        将答案从redis中拿出来
            String key= RedisConstants.HOMEWORK_ID_KEY+id+":"+hid+":"+details1.getQid();
            TpaPreviewTrackServiceImpl.recoveryAnswer(details1, key, stringRedisTemplate, details);
        }
        return details;
    }

//    提交作业：将redis中的互数据写如数据库中，删除redis中过的数据，同时将记录作业完成情况的数据进行更改
//    中途退出：也写入数据库中，但不删除redis中的数据
//    0表示中途退出没有完成，1表示提交作业已经完成,        并更改tpa_homework_summary表中记录该用户完成作业的情况！！！
    @Override
    public Integer submit(Integer complete, Integer hid, Integer uid) throws Exception {
//        根据模式匹配key将数据从redis中拿出来
//        key的格式为homework:1:12:23
        String pattern= RedisConstants.HOMEWORK_ID_KEY+uid+":"+hid+":"+"*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        int completeNum=0;
        int i=0;
        LocalDateTime time=null;
        int update=0;
        assert keys != null;
        for (String key:keys){
            TpaHomeworkTrack track1 = getTrackFromRedis(key, stringRedisTemplate);
//            获取第一个题目答题时间作为开始做作业的时间
            if (i==0) time=track1.getAttemptTime();
            if (track1.getAnswer()!=null) completeNum++;
//            将这条数据写到数据库中,要先判断是否在数据库中
//            TpaHomeworkTrack one = lambdaQuery().eq(TpaHomeworkTrack::getHid, hid)
//                    .eq(TpaHomeworkTrack::getSid, uid)
//                    .one();
//            由于开始将TpaHomeworkTrack这个hash插入redis的时候没有将这个放到数据库中，所以htId一定为null
//            如果这个等于null，就说明这个条题目的记录第一次插入数据库，插入后将这条数据的htId也更新到redis中，再次到这里时就根据redis中的这个字段不为null来更新而不是插入
            if(track1.getHtId()==null){
                boolean save = save(track1);
                if (!save) return 0;
//                将这条题目完成记录的id存到redis中,后续根据这个是否为空来判断这条数据是否已经保存在了数据库中
                else stringRedisTemplate.opsForHash().put(key,"htId",track1.getHtId().toString());
            }else {
//                直接根据htId更新就行，因为只要到了这里就说明redis中储存的这条数据track1是有htId的，即数据库中有这条数据，所以这里只需要对这条数据进行更新就行
                updateById(track1);
            }

            if (complete==1){
//            将redis中的数据删除
            stringRedisTemplate.delete(key);
            }
            i++;
        }
        if (complete==1){
            if (time!=null){
                //          表示已经做完提交了，就更改作业记录表中过的数据
                LambdaUpdateWrapper<TpaHomeworkSummary> query=new LambdaUpdateWrapper<>();
                query.eq(TpaHomeworkSummary::getHid,hid)
                        .eq(TpaHomeworkSummary::getUid,uid)
                        .set(TpaHomeworkSummary::getIsComplete,1)//1表示已完成
                        .set(TpaHomeworkSummary::getCompleteQuestion,completeNum)
                        .set(TpaHomeworkSummary::getCompleteTime, TimeUtils.getTimeDeff(time,CurrentTime.getTime()));
                update = summaryMapper.update(null, query);
            }
//            注意要给这个作业完成人数加1,未完成人数减1
            homeworkMapper.updateCompleteNum(hid);
//        开启后台匹配作业的线程池,注意只有做完了才进行批改
            markThread.markHomework(uid,hid);
        }

        return update;
    }

    static TpaHomeworkTrack getTrackFromRedis(String key, StringRedisTemplate stringRedisTemplate) {
        Map<Object, Object> track = stringRedisTemplate.opsForHash().entries(key);
        TpaHomeworkTrack track1 = BeanUtil.fillBeanWithMap(track, new TpaHomeworkTrack(), false);
        if (track.get("attemptTime")!=null&&!((String) track.get("attemptTime")).isEmpty()){
            track1.setAttemptTime(TimeUtils.stringToLocalDateTime((String) track.get("attemptTime")));
        }
        if (track.get("completionTime")!=null&&!((String) track.get("completionTime")).isEmpty()){
            track1.setCompletionTime(TimeUtils.stringToLocalDateTime((String) track.get("completionTime")));
        }
        return track1;
    }

    @Override
    public HomeworkCompleteVo selectAllInfo(Integer hid, Integer uid) {
        HomeworkCompleteVo completeVo = null;
        try {
//        先获取整体完成情况
            LambdaQueryWrapper<TpaHomeworkSummary> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(TpaHomeworkSummary::getHid,hid).eq(TpaHomeworkSummary::getUid,uid);
            TpaHomeworkSummary summary = summaryMapper.selectOne(queryWrapper);
            completeVo = new HomeworkCompleteVo();
            completeVo.setSummary(summary);
            TpaHomework homework = homeworkMapper.selectById(hid);

            AtomicInteger trueNum= new AtomicInteger();
            Map<String, Integer> knowledgePointErrors = new HashMap<>();
//        获取每道题的完成情况,连表查询
            List<TpaHomeworkTrack> tracksWithQuestion = trackMapper.selectOneAndQuestion(hid, uid);
            List<TrackWithDetails> track=new ArrayList<>();
            tracksWithQuestion.forEach(t->{
                TrackWithDetails t1 = new TrackWithDetails();
                BeanUtil.copyProperties(t,t1);
                TpaHomeworkDetails details = detailsMapper.selectById(t1.getQid());
                TpaPreviewTaskServiceImpl.dealFormation(details);
                System.out.println(details);
                t1.setHomeworkDetails(details);
                if (t1.getIsCorrect()!=null){
                    if (t1.getIsCorrect()==1)
                        trueNum.getAndIncrement();
                    else {
                        String knowledgePoint = details.getQtitle();
                        if (knowledgePoint != null && !knowledgePoint.isEmpty()) {
                            knowledgePointErrors.put(knowledgePoint, knowledgePointErrors.getOrDefault(knowledgePoint, 0) + 1);
                        }
                    }
                }
                    track.add(t1);
            });
            // 将Map转换为List，以便排序
            List<Map.Entry<String, Integer>> sortedKnowledgePoints = new ArrayList<>(knowledgePointErrors.entrySet());
            // 按错误次数从高到低排序
            sortedKnowledgePoints.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
            completeVo.setTrueNum(trueNum.get());
            completeVo.setMistakePoint(sortedKnowledgePoints);
            completeVo.setFalseNum(summary.getQuestionNum()-trueNum.get());
            BigDecimal bd = TpaPreviewTrackServiceImpl.getRete(summary.getQuestionNum(), trueNum);
            completeVo.setTrueRate(bd.doubleValue()+"%");
            completeVo.setTrackList(track);
            completeVo.setHName(homework.getHName());
            completeVo.setHtitle(homework.getHTitle());
            completeVo.setSecondaryTitle(homework.getSecondaryTitle());
            completeVo.setScore(homework.getScore());
        } catch (Exception e) {
            System.out.println( e.getMessage());
            e.printStackTrace();
        }
        return completeVo;
    }
}
