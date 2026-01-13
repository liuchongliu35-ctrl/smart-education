package com.bing.tpa.service.baseImpl;

import cn.hutool.core.bean.BeanUtil;
import com.bing.tpa.Thread.AnalysisDataThread;
import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.common.ResourceType;
import com.bing.tpa.domain.VO.*;
import com.bing.tpa.domain.digital.CommentInfo;
import com.bing.tpa.domain.entity.TpaTeachDesign;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.RedisException;
import com.bing.tpa.mapper.TeachingPlanMapper;
import com.bing.tpa.mapper.TpaTeachDesignMapper;
import com.bing.tpa.mapper.TpaTeacherMapper;
import com.bing.tpa.modelcall.chatWithImageCall.ChatWithImage;
import com.bing.tpa.modelcall.designCall.ChatWithModel;
import com.bing.tpa.modelcall.pptCall.AIPPTRequest;
import com.bing.tpa.service.baseService.TpaTeachDesignService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.bing.tpa.utils.*;
import com.bing.tpa.utils.word.PPTToImageUtil;
import io.swagger.models.auth.In;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;


import java.awt.*;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * <p>
 *  服务实现类
 *  以教学设计的id作为key
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaTeachDesignServiceImpl extends ServiceImpl<TpaTeachDesignMapper, TpaTeachDesign> implements TpaTeachDesignService {

    @Autowired
    private AnalysisDataThread analysisDataThread;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TpaTeachDesignMapper tpaTeachDesignMapper;

    @Autowired
    private ChatWithModel chatWithModel;

    @Autowired
    private TextSummaryUtil textSummaryUtil;

    @Autowired
    private TpaTeacherService teacherService;

    @Autowired
    private AIPPTRequest aipptRequest;
    @Autowired
    private InMemoryDataStore globalConfig;

    private static final String botId="7473462018583134247";

    private static final String CRON_EXPRESSION = "0 0/1 * * * ?";

    private volatile boolean taskExecuted = true;

    private static Integer globalKey=null;
    private static String newContents=null;
    private final String pptBasePath="pptFile";

    @Autowired
    private  SplitPPTUtil pptUtil;
    @Autowired
    private TpaTeacherService tpaTeacherService;

    @Autowired
    private  ResourceService resource;

    @Autowired
    private  ChatWithImage chatWithImage;

    @Autowired
    private  ReadPPTRemarkUtil remarkUtil;

    static Logger logger = LoggerFactory.getLogger(TpaTeachDesignServiceImpl.class);


    // tid为该教师的id
    @Override
    public Integer addAndPrepare(TpaTeachDesign teachDesign) throws RedisException {
//        先创建一条教学设计,创建时就需要设定好教师的id
        TpaTeacher user = tpaTeacherService.getCurrentUser();
        teachDesign.setSubject("人工智能通识课");//设置好教学设计关联的课程：人工智能通识课，后续与IA交互需要使用这个字段
        if(teachDesign.getDesignTitle()!=null&&teachDesign.getSecondaryTitle()!=null){
           teachDesign.setAuthorId(user.getUid());
           teachDesign.setCreateTime(CurrentTime.getTime());
           boolean save = save(teachDesign);

            //  添加成功就调用相关作业收集分析的接口，生成该教学设计可能用到的作业/预习任务的分析数据，同时将上节课的互动数据分析放入预备数据表中
           if(save&&teachDesign.getTdId()!=null){
//           在redis上创建一条数据,只需要将教学设计的文本部分储存进去，这个里初始化是一个空字符串，且先不定删除时间
//               key的格式为：design:content:,value是长字符串
               RedisDesign redisDesign = new RedisDesign();
               redisDesign.setDesignTitle(teachDesign.getDesignTitle());
               redisDesign.setSecondaryTitle(teachDesign.getSecondaryTitle());
               redisDesign.setDesignName(teachDesign.getDesignName());//设置教学设计的名字！！
               redisDesign.setContent("");//这个时候还啥也没有
               Map<String, Object> designMap = BeanUtil.beanToMap(redisDesign);
               Map<Object, Object> design = stringRedisTemplate.opsForHash().entries(RedisConstants.DESIGN_ID_KEY + teachDesign.getTdId().toString());
//             先判断redis中有没有这个数据，只有没有才可以进行添加,同时不设置时间，这样就可以在save大纲当做初始化内容时，可以无限等不用怕这个数据消失
               if(design.isEmpty()){
                   try {
                       stringRedisTemplate.opsForHash().putAll(RedisConstants.DESIGN_ID_KEY+teachDesign.getTdId().toString(),designMap);
                   } catch (Exception e) {
                       throw new RedisException("redis储存教学设计失败");
                   }
               }
               else return -1;

//               调用数据分析的线程，异步执行
//               analysisDataThread.analysisDataAndSave(teachDesign);

//               将教学设计的id返回
               return teachDesign.getTdId();
           }
        }
        return null;
    }

    /**
     * 定时将所有匹配模式的键对应的值携带数据库中
     */
    @Scheduled(cron = CRON_EXPRESSION)
    public void syncRedisToMysql() {
        if (!taskExecuted) {
            log.error("任务正在被手动调用，跳过定时触发的执行");
            return;
        }
        if (globalKey==null){
//            log.error("定时任务已被触发并将新的数据写入redis，不用将数据从redis中删除且不用再次执行定时任务了");
//            将newContent置为null
            newContents=null;
//            但是globalKey需要付一个值，不然后续的地址任务都不会被触发
            globalKey=0;
            return;
        }
        taskExecuted=false;
        try {
            // 定时任务逻辑
            String pattern = RedisConstants.DESIGN_ID_KEY + "*";
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    Map<Object, Object> designMap = stringRedisTemplate.opsForHash().entries(key);
                    RedisDesign redisDesign = BeanUtil.fillBeanWithMap(designMap, new RedisDesign(), false);
                    String designId = key.replace(RedisConstants.DESIGN_ID_KEY, "");
                    if (!Objects.equals(redisDesign.getContent(), "")) {
                        boolean exists = lambdaQuery().eq(TpaTeachDesign::getTdId, designId).exists();
                        if (exists) {
                            lambdaUpdate()
                                    .eq(TpaTeachDesign::getTdId, designId)
                                    .set(TpaTeachDesign::getContent, redisDesign.getContent())
                                    .set(TpaTeachDesign::getLastModify,CurrentTime.getTime())
                                    .update();
                            log.error(CurrentTime.getTime() + "已更新"+designId+"这个教学设计");
                        }
                        if (globalKey!=null&&newContents!=null){
                            if (!key.equals(RedisConstants.DESIGN_ID_KEY + globalKey)){//如果当前的这个key等于我正要更新的key，就不要进行删除，因为用户正在修改，不能删掉
                                stringRedisTemplate.delete(key);
                                log.error(CurrentTime.getTime() + "已删除"+designId+"这个教学设计");
                            }
                        }else{
                            stringRedisTemplate.delete(key);//但是如果为null，说明用户可能长时间没有修改了，就没有必要在将数据存在redis中了
                            log.error(CurrentTime.getTime() + "已删除"+designId+"这个教学设计");
                        }
                    }
                }
//                循环出来后再将需要修改的写到redis中
                if (globalKey!=null&&newContents!=null){
                    updateDesign(globalKey,newContents);
                    globalKey=null;//将这个置为null，这样接下来可能执行的定时任务就不用执行了
                }
                log.error(CurrentTime.getTime() + "已经成功将redis中的数据写到数据库中，这里是springboot定时执行的任务");
            } else {
                log.error(CurrentTime.getTime() + "redis中没有和教学设计相关的数据，不执行更新数据库操作");
            }
        } finally {
            taskExecuted = true; // 重置标志位，以便下次需要时可以再次执行
        }
    }
//    获取上面这个定时任务距离下一次执行的时间
    public Date getNextExecutionTime() {
        return CronExpressionUtil.getNextExecutionTime(CRON_EXPRESSION, new Date());
    }

    @Override
    public String promptFromAI(PromptVo prompt) {
//        根据教学设计的id从redis中获取教学设计的内容以及该教学设计的标题
        Map<Object, Object> design = stringRedisTemplate.opsForHash().entries(RedisConstants.DESIGN_ID_KEY + prompt.getTdId());
        RedisDesign redisDesign = BeanUtil.fillBeanWithMap(design, new RedisDesign(), false);
//        如果没有了，就先从数据库中重新获取数据，然后将新的数据放到redis中,并将数据程序赋给RedisDesign
        if (design.isEmpty()) {
            TpaTeachDesign teachDesign = getById(prompt.getTdId());
            redisDesign.setDesignTitle(teachDesign.getDesignTitle());
            redisDesign.setDesignTitle(teachDesign.getSecondaryTitle());
            redisDesign.setContent(teachDesign.getContent());
            updateDesign(prompt.getTdId(), teachDesign.getContent());
        }
        String result = null;
        switch (prompt.getSelect()) {
//            1表示需要根据框选的内容生成提示
            case 1:
               result= generateStruct(redisDesign, prompt);
                break;
//            2表示需要根据框选的内容生成内容
            case 2:
                result=generateContent(redisDesign, prompt);
                break;
//            3表示需要对框选的内容进行扩写

            case 3:
                result=generateExpand(redisDesign, prompt);
                break;
            default:
//                如果select不是以上那个任意一个值，就表明这个用户想根据自己的需求获取内容
                if (Objects.equals(prompt.getOther(), "")) return null;
                String question = "教学设计的主题：\n一级主题：" + redisDesign.getDesignTitle() + "\n二级主题：" + redisDesign.getSecondaryTitle() + "\n" +
                        "具体文章结构和内容如下：" + redisDesign.getContent() + "\n现在我正在写”" + prompt.getText() + "“这部分，" +
                        "我现在的需求是：" + prompt.getOther() + "，请根据我的需求来为我生成写作提示";
                try {
                    result = chatWithModel.chatClient(question, prompt.getTdId().toString(),botId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
//        先根据用户的选择进行分类
        return result;
    }
    /**
     *      生成结构
     */
    private String generateStruct(RedisDesign redisDesign, PromptVo prompt) {
        String question = "教学设计的主题：\n一级主题：" + redisDesign.getDesignTitle() + "\n二级主题：" + redisDesign.getSecondaryTitle() + "\n" +
                "该教学设计的具体文章结构和内容如下：" + redisDesign.getContent() + "\n现在我正在写”" + prompt.getText() + "“这部分，" +
                "请根据该教学设计的主题和原有的结构和内容，来帮我生成我正在写的这部分的一个写作思路和结构，注意不要给我生成全文的结构和内容，" +
                "请聚焦到我正在编写的这个位置，我只要我正在写的这部分的写作思路和结构布局，不需要生成具体的正文内容\n"+"用户的额外需求"+prompt.getOther()+"" +
                "\n生成格式限制：1、不要生成- **这样的markdown符号";
        String result = null;
        try {
            result = chatWithModel.chatClient(question, prompt.getTdId().toString(),botId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *      生成内容
     */
    private String generateContent(RedisDesign redisDesign, PromptVo prompt){
        String question = "教学设计的主题：\n一级主题：" + redisDesign.getDesignTitle() + "\n二级主题：" + redisDesign.getSecondaryTitle() + "\n" +
                "该教学设计的具体文章结构和内容如下：" + redisDesign.getContent() + "\n现在我正在写”" + prompt.getText() + "“这部分，" +
                "请根据该教学设计的主题和原有的结构和内容，来帮我生成我正在写的这部分应该写的具体内容，只要生成这部分具体应该写什么正文内容，" +
                "注意不要给我生成全文的结构和内容，请聚焦到我在编写的这个位置，我只要我正在写的这位置接下来应该写什么正文内容，即<p>标签下的内容\n"+"用户的额外需求"+prompt.getOther()+
                "\n生成格式限制：1、不要生成- **这样的markdown符号";
        String result = null;
        try {
            result = chatWithModel.chatClient(question, prompt.getTdId().toString(),botId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *      拓展内容
     */
    private String generateExpand(RedisDesign redisDesign, PromptVo prompt){
        String question = "教学设计的主题：\n一级主题：" + redisDesign.getDesignTitle() + "\n二级主题：" + redisDesign.getSecondaryTitle() + "\n" +
                "该教学设计的具体文章结构和内容如下：" + redisDesign.getContent() + "\n现在我正在写”" + prompt.getText() + "“这部分，" +
                "请根据该教学设计的主题和原有的结构和内容，来帮我续写接下的内容，在"+prompt.getText()+"这个的基础上进行续写，且只需要这段文字所在的小标题下的内容，不要生成其他标题下的内容" +
                "注意也不要生成全文的结构和内容，请聚焦到我在编写的这个位置，在我正在编辑的这个<p>标签下继续续写正文内容\n"+"用户的额外需求"+prompt.getOther()+
                "\n生成格式限制：1、不要生成- **这样的markdown符号";
        String result = null;
        try {
            result = chatWithModel.chatClient(question, prompt.getTdId().toString(),botId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 更新教学设计的数据到redis中
     * @param contentVo
     */
    @Override
    public void updateDesignContent(ContentVo contentVo) throws InterruptedException, RedisException {

//        判断对应key的教学设计是否存在
//        历史教学设计修改时，redis中的该教学设计的数据可能为空
//        Map<Object, Object> designMap = stringRedisTemplate.opsForHash().entries(RedisConstants.DESIGN_ID_KEY + key);
        TpaTeachDesign design = getById(contentVo.getTdId());
//        if (!Objects.equals(design.getAuthorId(), contentVo.getUid())) throw new RedisException("该用户无权限访问redis中不属于他的数据");
        Boolean keyExists = stringRedisTemplate.hasKey(RedisConstants.DESIGN_ID_KEY + contentVo.getTdId());
        if(!Boolean.TRUE.equals(keyExists)){
//            重新将这个教学设计写到redis中
            updateDesign(contentVo.getTdId(),contentVo.getNewContents());
        }else {
            long timeLength = getRemainingTimeToNextExecution();
            if (timeLength<=5){
//                如发现距离执行时间只剩下5秒一般内了，就直接执行，然后将数据重新写到redis中
                    globalKey=contentVo.getTdId();
                    newContents=contentVo.getNewContents();//将需要写入redis中过的数据保存到全局数据中
                    assert globalKey!=null;
                    assert newContents!=null;//断言不为null，不然进入后会被弹出去
                    syncRedisToMysql();
            }else {
//        如何还有这条数据，就进行单个属性content的更新
                lambdaUpdate().eq(TpaTeachDesign::getTdId,contentVo.getTdId()).set(TpaTeachDesign::getLastModify,CurrentTime.getTime())
                                .update();//更新最新修改时间
                stringRedisTemplate.opsForHash().put(RedisConstants.DESIGN_ID_KEY+contentVo.getTdId(),"content",contentVo.getNewContents());
                stringRedisTemplate.opsForHash().put(RedisConstants.DESIGN_ID_KEY+contentVo.getTdId(),"designName",design.getDesignName());
//        给这个key重置时间，只要编辑保存一次就重置一次时间，如果发现已经嘎了，就走上面的逻辑
                stringRedisTemplate.expire(RedisConstants.DESIGN_ID_KEY+ contentVo.getTdId(),RedisConstants.DESIGN_ID_TTL, TimeUnit.MINUTES);
            }
        }
        /**
         * 注意：这里不需要对数据库进行更新，即使是对历史教学设计进行修改，我们只需要修改redis中的数据即可，后续redis会将数据自动储存到数据库中
         */
    }

    /**
     * todo 生成视频,使用kimi
     */
    @Override
    public List<VideoVo> getVideo(LongTextVo text) throws InterruptedException {
//        todo 创建固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        todo 定时启动
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//        视频+关键字的集合
        List<VideoVo> videoVoList=new ArrayList<>();
        //   如果关键字长度小于10就不用进行关键词提取,直接生成视频资源
        if(text.getText().length()<10){
            buildVideoList(text.getText(), videoVoList);
        }else {
            //      先调用长文本凝练接口进行关键词生成
            String keywords = textSummaryUtil.textProcessing(text.getText());
//      分割关键词字符串
            String[] keys = keywords.split("\n");
//          目前只能一分钟有一个并发和三次请求，所以这里暂时还不能实现多个关键字的网站同时获取
//            for (String key:keys){
//                buildVideoList(key, videoVoList);
//            }
//            todo 充钱方案，并发获取多个关键词的视频链接资源
//            try {
//                logger.info("开始获取视频链接");
//                int threadNum = (int) Math.ceil((double) keys.length/2);
//                for(int i=0;i<threadNum;i++){
//                    final int threadIndex=i;
//                    scheduler.schedule(()->{
////                    计算关键词位置
//                        int startIndex=threadIndex*2;
//                        int  endIndex=Math.min(startIndex+2,keys.length);
//                        // 处理当前线程负责的关键词
//                        for (int j = startIndex; j < endIndex; j++) {
//                            String key = keys[j];
//                            buildVideoList(key, videoVoList);
//                        }
//                    },i,TimeUnit.SECONDS);
//                }
//                // 关闭调度线程池
//                scheduler.shutdown();
//                // 等待所有调度任务完成
//                scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//
//                // 等待所有图片获取图片的任务完成
//                executor.shutdown();
//                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//
//            }catch (Exception e) {
//                Thread.currentThread().interrupt();
//                throw new RuntimeException("图片生成失败，请重新尝试");
//            } finally {
//                logger.info("视频资源获取结束");
//                if (!executor.isTerminated()) {
//                    executor.shutdownNow();
//                }
//                if (!scheduler.isTerminated()) {
//                    scheduler.shutdownNow();
//                }
//            }
//           todo 不充钱方案
            buildVideoList(keys[0],videoVoList);//暂时只生成第一个凝缩关键字的视频资源
        }
        return videoVoList;
    }
//  todo 使用coze
//    @Override
//    public List<PhotoVo> getPhoto(@RequestBody LongTextVo text) {
////        todo 创建固定大小的线程池
//        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
////        todo 定时启动
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//        List<PhotoVo> photoVoList=new ArrayList<>();
//        if(text.getText().length()<10){
//            buildPhotoList(text.getText(),photoVoList);
//        }else {
////            todo 分词
//            String keywords = textSummaryUtil.textProcessing(text.getText());
//            String[] keys = keywords.split("\n");
////            todo 根据分词获取图片，这里可以使用多线程加快速度
//            try{
////          todo 计算线程数
//                logger.info("开始获取图片资源");
//                int threadNum = (int) Math.ceil((double) keys.length/2);
////                todo 遍历启动线程
//                for(int i=0;i<threadNum;i++){
//                   final int threadIndex=i;
//                   scheduler.schedule(()->{
////                    计算关键词位置
//                       int startIndex=threadIndex*2;
//                       int  endIndex=Math.min(startIndex+2,keys.length);
//                       // 处理当前线程负责的关键词
//                       for (int j = startIndex; j < endIndex; j++) {
//                           String key = keys[j];
//                           buildPhotoList(key, photoVoList);
//                       }
//                   },i,TimeUnit.SECONDS);
//                }
//
//                // 关闭调度线程池
//                scheduler.shutdown();
//                // 等待所有调度任务完成
//                scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//
//                // 等待所有图片获取图片的任务完成
//                executor.shutdown();
//                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//            }catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                throw new RuntimeException("图片生成失败，请重新尝试");
//            } finally {
//                if (!executor.isTerminated()) {
//                    executor.shutdownNow();
//                }
//                if (!scheduler.isTerminated()) {
//                    scheduler.shutdownNow();
//                }
//                logger.info("图片资源获取成功");
//            }
////            for (String key:keys){
////                buildPhotoList(key,photoVoList);
////            }
//        }
//        return photoVoList;
//    }

    @Override
    public List<PhotoVo> getPhoto(@RequestBody LongTextVo text) {
    // 使用线程安全的集合
        List<PhotoVo> photoVoList = new CopyOnWriteArrayList<>();

        if (text.getText().length() < 10) {
          buildPhotoList(text.getText(), photoVoList);
        } else {
           String keywords = textSummaryUtil.textProcessing(text.getText());
          String[] keys = keywords.split("\n");
          if (keys.length == 0) {
             return photoVoList;
         }

        // 1. 使用多线程池（核心线程数=CPU核心数）
          int corePoolSize = Runtime.getRuntime().availableProcessors();
          ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);

         try {
             logger.info("开始获取图片资源，关键词数量：{}", keys.length);

            // 2. 提交所有任务到线程池（并发执行）
              for (String key : keys) {
                // 避免lambda捕获循环变量的线程安全问题，显式声明final变量
                    final String currentKey = key;
                    executor.submit(() -> {
                        System.out.println("关键词"+currentKey+"线程启动");
                       buildPhotoList(currentKey, photoVoList);
                    });
              }

            // 3. 关闭线程池并等待所有任务完成
             executor.shutdown();
             if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                 executor.shutdownNow();
                    logger.warn("部分图片获取任务超时未完成");
              }
              logger.info("图片资源获取成功");
         } catch (InterruptedException e) {
             executor.shutdownNow();
               Thread.currentThread().interrupt();
                throw new RuntimeException("图片生成失败，请重新尝试");
         }
      }
     return photoVoList;
    }

    @Override
    public List<TpaTeachDesign> matchDesignByTitle(String title) {
        return tpaTeachDesignMapper.selectByTitle(title);
    }

    @Override
    public String PPTFromDesign(Integer tdId) {
        String savePath=null;
//        先查询教学设计的主题
        TpaTeachDesign design = getById(tdId);
        globalConfig.put("pptName",design.getDesignName());
        TpaTeacher user = teacherService.getCurrentUser();
        String request="请生成人工智能通识课教学课件ppt，主题为："+design.getSecondaryTitle()+"("+design.getDesignTitle()+"),";
        try {
            // 阶段1: 生成PPT文档 (约35秒)
            globalConfig.put("progress", 25.0);
           savePath=aipptRequest.generatePPT(user, design, request);
           logger.info("<UNK>ppt制作完成! 保存路径：<UNK>"+savePath);

//           todo 为每一页ppt添加批注
            // 阶段2: 添加批注 (约2分钟)
            globalConfig.put("progress", 50.0);
            addRemark(savePath,user,design.getDesignName());
            logger.info("批注添加完成");
        } catch (Exception e) {
            // 任何阶段失败都更新状态
            globalConfig.put("status", "fail");
            globalConfig.put("progress",null);
            logger.error("PPT生成失败: {}", e.getMessage());
            throw new RuntimeException("PPT生成失败: " + e.getMessage(), e);
        }
        return savePath;
    }

    public void addRemark(String pptPath, TpaTeacher user, String pptName) throws IOException {
        Map<Integer, CommentInfo> remarkMap = new ConcurrentHashMap<>(); // 使用ConcurrentHashMap保证线程安全

        // 1. 创建图片存储目录
        Path pptPhotoRootPath = resource.getResourcePath(ResourceType.PPTPHOTO, "");
        Path pptPhotoUserPath = pptPhotoRootPath.resolve(user.getAccount());
        Files.createDirectories(pptPhotoUserPath);

        // 2. 预加载PPT获取总页数和所有幻灯片
        int totalPages;
        List<XSLFSlide> slides = new ArrayList<>();
        Dimension pageSize = null;
        byte[] pptBytes = Files.readAllBytes(Paths.get(pptPath));

        try (ByteArrayInputStream bis = new ByteArrayInputStream(pptBytes);
             XMLSlideShow xmlSlideShow = new XMLSlideShow(bis)) {
            totalPages = xmlSlideShow.getSlides().size();
            pageSize = xmlSlideShow.getPageSize();

            // 预加载所有幻灯片到内存
            for (XSLFSlide slide : xmlSlideShow.getSlides()) {
                slides.add(slide);
            }

            logger.info("PPT总页数：{}", totalPages);
        }

        final Dimension finalPageSize = pageSize; // 用于lambda表达式

        // 3. 创建优化的线程池（IO密集型任务）
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory() {
                    private final AtomicInteger threadNum = new AtomicInteger(1);
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        Thread t = new Thread(r, "ppt-remark-thread-" + threadNum.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时让提交线程执行，避免任务丢失
        );

        // 4. 任务控制与进度监控
        CountDownLatch completionLatch = new CountDownLatch(totalPages);
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger processedPages = new AtomicInteger(0);

//        Consumer<Integer> progressUpdater = pageIndex -> {
//            double baseProgress = 50.0; // 从50%开始
//            double increment = 50.0 / totalPages; // 每页进度增量
//            double newProgress = baseProgress + (processedPages.incrementAndGet() * increment);
//            globalConfig.put("progress", Math.min(newProgress, 99.9)); // 保留最后0.1%给收尾
//        };
        Consumer<Integer> progressUpdater = pageIndex -> {
            double baseProgress = 50.0; // 从50%开始
            double increment = 35.0 / totalPages; // 调整为35%分配给页面处理阶段
            double newProgress = baseProgress + (processedPages.incrementAndGet() * increment);
            globalConfig.put("progress", Math.min(newProgress, 85.0)); // 限制最大为85%
        };


        // 5. 提交任务（无延迟，立即并发执行）
        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
            final int currentPage = pageIndex + 1;
            final int slideIndex = pageIndex; // 幻灯片索引（0-based）

            Future<?> future = executor.submit(() -> {
                String imagePath = null;
                try {
                    // === 阶段1: 转换单页PPT为图片（使用预加载的幻灯片）===
                    // 验证页码有效性
                    if (slideIndex >= slides.size()) {
                        logger.error("无效页码: {}(最大页数: {})", currentPage, slides.size());
                        return;
                    }

                    XSLFSlide slide = slides.get(slideIndex);
                    imagePath = PPTToImageUtil.convertSlideToImage(
                            slide, finalPageSize, pptPhotoUserPath.toString(), currentPage
                    );

                    // === 阶段2: 获取讲词（带动态重试策略）===
                    String require = "使用OCR插件识别这页ppt图片，并生成授课讲词，这是教案ppt的第" + currentPage + "页";
                    int maxRetries = 4;
                    int retryCount = 0;
                    String remark = null;

                    while (retryCount < maxRetries) {
                        try {
                            remark = chatWithImage.chatWithImageCall(imagePath, require);
                            if (remark.contains("NULL")||remark.contains("无法")) throw new Exception("图片识别失败，重试");
                            break;
                        } catch (Exception e) {
                            retryCount++;
                            if (retryCount < maxRetries) {
                                long sleepTime = (long) (Math.pow(2, retryCount) * 1000); // 指数退避：1s→2s→4s
                                logger.warn("第{}页调用失败，{}ms后重试({}/{})",
                                        currentPage, sleepTime, retryCount, maxRetries);
                                Thread.sleep(sleepTime);
                            } else {
                                logger.error("第{}页调用失败", currentPage, e);
                                remark = "该页授课讲词获取失败，请自行添加";
                            }
                        }
                    }

                    // === 阶段3: 存储批注信息 ===
                    CommentInfo info = new CommentInfo(
                            user.getAccount(),
                            remark,
                            new Point2D.Float(25, 8),
                            Date.from(Instant.now())
                    );
                    remarkMap.put(slideIndex, info);
                    logger.info("第{}页批注信息已存储", currentPage);

                    // 更新进度
                    progressUpdater.accept(currentPage);
                } catch (Exception e) {
                    logger.error("第{}页处理失败: {}", currentPage, e.getMessage());
                } finally {
                    // 清理临时图片
                    if (imagePath != null) {
                        try {
                            Files.deleteIfExists(Paths.get(imagePath));
                        } catch (IOException e) {
                            logger.warn("第{}页图片清理失败: {}", currentPage, e.getMessage());
                        }
                    }
                    completionLatch.countDown(); // 无论成功失败都标记完成
                }
            });

            futures.add(future);
        }

        // 6. 等待所有任务完成
        try {
            if (!completionLatch.await(30, TimeUnit.MINUTES)) {
                logger.warn("部分任务未在30分钟内完成");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("主线程等待被中断");
        } finally {
            // 7. 处理批注并合并到PPT
            globalConfig.put("progress", 85.0);
            Path tempDir = null;
            Path mergedOutputPath = null;

            try {
                // 优化：页数少则无需拆分
                List<Path> splitFiles;
                if (totalPages <= 10) {
                    // 页数少，直接使用原文件处理
                    splitFiles = Collections.singletonList(Paths.get(pptPath));
                } else {
                    // 创建临时目录
                    tempDir = pptPhotoUserPath.resolve("temp-split-" + System.currentTimeMillis());
                    Files.createDirectories(tempDir);
                    logger.info("创建临时目录: {}", tempDir);

                    // 拆分PPT
//                splitFiles = pptUtil.splitPPT(pptPath, tempDir.toString(), pptName);
                    System.out.println("开始拆分");
                    CompletableFuture<List<Path>> future = pptUtil.splitPPT(pptPath, tempDir.toString(), pptName);
                    splitFiles = future.get(5, TimeUnit.MINUTES);
                    logger.info("拆分完成，拆分后的PPT保存路径: {}", splitFiles);
                }

                // 处理每个拆分文件并添加批注
                for (int i = 0; i < splitFiles.size(); i++) {
                    Path subPptPath = splitFiles.get(i);
                    int startPage = i * 3;
                    int endPage = Math.min(startPage + 2, totalPages - 1);

                    // 创建当前组的批注映射
                    Map<Integer, CommentInfo> subComments = new HashMap<>();
                    for (int page = startPage; page <= endPage; page++) {
                        if (remarkMap.containsKey(page)) {
                            // 新key = 原始页码 - 起始页码
                            subComments.put(page - startPage, remarkMap.get(page));
                        }
                    }

                    // 添加批注到子PPT（覆盖原文件）加到线程池中运行
                    Future<Void> future = remarkUtil.addComments(subPptPath.toString(), subPptPath.toString(), subComments);
                    future.get(5, TimeUnit.MINUTES);
                    logger.info("已为第 {} 组添加批注 (页码 {}-{})",
                            i + 1, startPage + 1, endPage + 1);

                    // 更新进度 (85% → 95%)
//                    double progress = 85.0 + (10.0 * (i + 1) / splitFiles.size());
//                    globalConfig.put("progress", progress);
                    // 更新进度：从85%开始，逐步增加到99.9%
                    double progress = 85.0 + (14.9 * (i + 1) / splitFiles.size());
                    globalConfig.put("progress", progress);
                }

                // 合并所有子PPT
                mergedOutputPath = pptPhotoUserPath.resolve("with-comments-"+pptName+".pptx");//注意这里要加后缀pptx
                Future<Void> future = pptUtil.mergePPTs(splitFiles, mergedOutputPath.toString());
                future.get(5, TimeUnit.MINUTES);
                // 替换原始PPT文件
                Files.copy(mergedOutputPath, Paths.get(pptPath),
                        StandardCopyOption.REPLACE_EXISTING);
                logger.info("PPT文件已更新: {}", pptPath);
            } catch (Exception e) {
                globalConfig.put("status", "fail");
                logger.error("批注添加失败", e);
            }
            globalConfig.put("progress", 99.9);
            // 8. 关闭线程池并清理资源
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

            // 清理临时文件
            cleanTempFiles(pptPhotoUserPath, tempDir, mergedOutputPath);
        }
    }

    // 优化：使用NIO递归清理临时文件
    private void cleanTempFiles(Path pptPhotoUserPath, Path tempDir, Path mergedOutputPath) {
        cleanImageFiles(pptPhotoUserPath);
        // 1. 清理临时拆分目录及其内容
        if (tempDir != null) {
            try {
                Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
                    @NotNull
                    @Override
                    public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @NotNull
                    @Override
                    public FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
                logger.info("已删除临时拆分目录: {}", tempDir);
            } catch (IOException e) {
                logger.error("临时拆分目录删除失败: {}", e.getMessage());
            }
        }

        // 2. 清理合并的临时PPT文件
        if (mergedOutputPath != null) {
            try {
                Files.deleteIfExists(mergedOutputPath);
                logger.info("已删除合并临时文件: {}", mergedOutputPath);
            } catch (IOException e) {
                logger.error("合并临时文件删除失败: {}", e.getMessage());
            }
        }
    }

    // 清理图片文件（增强版）
    private void cleanImageFiles(Path imageDir) {
        try {
            // 清理所有图片格式
            String[] imageExtensions = {"jpg", "jpeg", "png"};

            for (String ext : imageExtensions) {
                try (DirectoryStream<Path> stream =
                             Files.newDirectoryStream(imageDir, "*." + ext)) {
                    for (Path file : stream) {
                        Files.deleteIfExists(file);
                        logger.debug("已删除临时图片: {}", file.getFileName());
                    }
                }
            }

            // 清理可能存在的临时PPT文件
            try (DirectoryStream<Path> pptStream =
                         Files.newDirectoryStream(imageDir, "*.pptx")) {
                for (Path file : pptStream) {
                    // 只删除临时文件，避免删除最终PPT
                    if (file.getFileName().toString().contains("-temp-") ||
                            file.getFileName().toString().endsWith("-with-comments.pptx")) {
                        Files.deleteIfExists(file);
                        logger.debug("已删除临时PPT: {}", file.getFileName());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("图片清理失败: {}", e.getMessage());
        }
    }


    /**
     * 根据知识点获取相关的ppt
     * @param title
     * @return
     */
    @Override
    public List<PPTVo> getPPtByKnowledge(String title) throws IOException {
//        todo 1、获取与该知识点相关的教学设计
        TpaTeacher user = tpaTeacherService.getCurrentUser();
        List<TpaTeachDesign> designs = tpaTeachDesignMapper.selectByTitle(title);
        List<PPTVo> pptVoList = new ArrayList<>();
//        todo 2、遍历教学设计，根据教学设计查找视频
        for(TpaTeachDesign design:designs){
            PPTVo pptVo = new PPTVo();
//            todo 2.1 构建该教学设计的名字
            String pptName = makePPTName(design.getDesignName(), user.getUid(), design.getTdId());
//            todo 2.2 组装ppt路径
            int idCount=1;
            Path pptRootPath = Paths.get(user.getAccount()+"-ppt");//  雪之下的猫-ppt
            Path pptPath = pptRootPath.resolve(pptName);//  雪之下的猫-ppt/ppt名字
            if(resource.existsResource(ResourceType.PPT,pptPath.toString())){
                String pptUrl =pptPath.toString().replace(File.separator, "/");
                pptVo.setPptUrl(pptUrl);
                pptVo.setPptName(pptName);
                pptVo.setPptSize(FileSizeUtil.getSize(resource.getResourcePath(ResourceType.PPT,pptUrl)));
                pptVo.setPptId(idCount);
                idCount++;
            }
            pptVoList.add(pptVo);
        }
        return pptVoList;
    }
    /**
     * 获取该用户现有的所有教学设计+ppt
     * @return
     */
    @Override
    public List<DesignAndPPTVo> getAllDesignAndPPT() throws IOException {
//        先根据authorId获取所有教学设计
        TpaTeacher user = teacherService.getCurrentUser();
        List<TpaTeachDesign> designs = tpaTeachDesignMapper.selectByAuthorId(user.getUid());
        List<DesignAndPPTVo> designAndPPTVoList=new ArrayList<>();
//        遍历所有教学设计
        for (TpaTeachDesign design:designs){
            DesignAndPPTVo designAndPPTVo = new DesignAndPPTVo();
//            将这个教学设计赋给DesignAndPPTVo
            BeanUtils.copyProperties(design,designAndPPTVo,"content");
//            组装ppt的名字：
            String pptName = makePPTName(design.getDesignName(), user.getUid(), design.getTdId());
//            组装ppt路径，根路径的用户目录有一个“-ppt”的后缀
            Path pptRootPath = Paths.get(user.getAccount()+"-ppt");//  src/main/resources/pptFile/雪之下的猫-ppt
            Path pptPath = pptRootPath.resolve(pptName);//  src/main/resources/pptFile/雪之下的猫-ppt/
            int idCount=1;
//            判断该教学设计是否有ppt

            if(resource.existsResource(ResourceType.PPT,pptPath.toString())){
//               如果ppt存在就写到这个
                designAndPPTVo.setPptName(pptName);
                String pptUrl =pptPath.toString().replace(File.separator, "/");
                designAndPPTVo.setPptUrl(pptUrl);
                designAndPPTVo.setPptId(idCount);
                idCount++;
//                计算ppt大小
                designAndPPTVo.setPptSize(FileSizeUtil.getSize(resource.getResourcePath(ResourceType.PPT,pptUrl)));
                designAndPPTVo.setIsHavePPT(true);
            }else designAndPPTVo.setIsHavePPT(false);//不存在ppt文件就直接设置IsHavePPT为false

            designAndPPTVoList.add(designAndPPTVo);
        }
        return designAndPPTVoList;
    }

    public String makePPTName(String designName, Integer uid,Integer tdId) {
        return  uid+"--"+tdId+"--"+designName+".pptx";
    }

    //  获取定时任务距离下一次执行还有多长时间
    private long getRemainingTimeToNextExecution() {
        Date nextExecutionTime = getNextExecutionTime();
        if (nextExecutionTime != null) {
            // 计算当前时间与下一次执行时间的差值（毫秒）
            long remainingMillis = nextExecutionTime.getTime() - new Date().getTime();
            // 转换为秒
            return remainingMillis / 1000;
        }
        return -1; // 如果没有找到定时任务或下一次执行时间为空，返回-1
    }

    public void buildVideoList(String text, List<VideoVo> videoVoList) {
        List<String> videos = textSummaryUtil.getVideoUrlFromKimi(text);
        VideoVo videoVo = new VideoVo();
        videoVo.setKeyword(text);
        videoVo.setVideoUrl(videos);
        videoVoList.add(videoVo);
    }

    public void buildPhotoList(String text,List<PhotoVo> photoVoList){
        String photoUrl = textSummaryUtil.getPhotoUrl(text);
        String[] photoList = photoUrl.split("\",\"");
        PhotoVo photoVo=new PhotoVo();
        List<String> photos=new ArrayList<>(Arrays.asList(photoList));
        photoVo.setKey(text);
        photoVo.setPhotoUrl(photos);
        photoVoList.add(photoVo);
    }

    public  void updateDesign(Integer key, String newContents) {
//        此时，项目已经执行    @Scheduled(cron = "0 0/5 * * * ?")这个将redis中的数据写到数据库中并将redis中的数据给删除掉了，
//        所以这里需要再次根据这个饺子哦学设计的信息再次创建数据到redis中：
//            查询这个教学设计的主题
        TpaTeachDesign design = getById(key);
//            包装 RedisDesign
        RedisDesign redisDesign = new RedisDesign();
        redisDesign.setContent(newContents);
        redisDesign.setDesignTitle(design.getDesignTitle());
        redisDesign.setSecondaryTitle(design.getSecondaryTitle());
        redisDesign.setDesignName(design.getDesignName());
//            转map储存
        Map<String, Object> designToMap = BeanUtil.beanToMap(redisDesign);
        stringRedisTemplate.opsForHash().putAll(RedisConstants.DESIGN_ID_KEY + key,designToMap);
//            重建后再重置时间
        stringRedisTemplate.expire(RedisConstants.DESIGN_ID_KEY+ key,RedisConstants.DESIGN_ID_TTL, TimeUnit.MINUTES);
        lambdaUpdate().eq(TpaTeachDesign::getTdId,key).set(TpaTeachDesign::getLastModify,CurrentTime.getTime())
                .update();//更新最新修改时间
        log.error("已将redis中因定时保存到数据库中的数据重新储存到redis中了！");
    }

}
