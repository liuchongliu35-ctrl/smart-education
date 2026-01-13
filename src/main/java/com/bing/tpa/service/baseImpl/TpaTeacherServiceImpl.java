package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bing.tpa.domain.VO.ExtraInfo;
import com.bing.tpa.domain.VO.TeachInfoVo;
import com.bing.tpa.domain.dto.LoginUser;
import com.bing.tpa.domain.dto.UserLoginDTO;
import com.bing.tpa.domain.entity.TpaSubject;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.exception.IdentityException;
import com.bing.tpa.exception.LessonException;
import com.bing.tpa.mapper.TpaTeacherMapper;
import com.bing.tpa.service.baseService.TpaSubjectService;
import com.bing.tpa.service.baseService.TpaTeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.utils.MD5Utils;
import com.bing.tpa.utils.jwt.JwtUtil;
import com.bing.tpa.utils.jwt.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaTeacherServiceImpl extends ServiceImpl<TpaTeacherMapper, TpaTeacher> implements TpaTeacherService {

    @Autowired
    private TpaTeacherMapper teacherMapper;
//    @Autowired
//    private SubjectUpdateThread subjectUpdateThread;
    @Autowired
    private TpaSubjectService subjectService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private PasswordEncoder passwordEncoder;  // 注入 BCryptPasswordEncoder


    @Override
    public Integer newAndAddInfo(TpaTeacher teacher) throws IdentityException {
        //TODO  1. 使用BCryptPasswordEncoder加密密码!!,不然后续登录校验密码的时候就无法匹配了
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));

        //这里可以添加对教师信息的其他校验逻辑
//        设置某些属性的默认值
        teacher.setTeachLesson(teacher.getTeachLesson()==null?"人工智能通识课":teacher.getTeachLesson());
        teacher.setTeachStage(teacher.getTeachStage()==null?"大学":teacher.getTeachStage());
        teacher.setStageNum(teacher.getStageNum()==null?1:teacher.getStageNum());
        //等用户登录进去了再让用户选择课程培养方案，即课程结构组成，有默认的课程列表，也支持在原来的基础上添加，也可以清除重新定制，也可以就使用默认的，根据各自学校的规定来安排

//        查询是否重名
        boolean isSuccess=false;
        TpaTeacher tpaTeacher = teacherMapper.findByAccount(teacher.getAccount());
        if(tpaTeacher != null){
            throw new IdentityException("用户名已存在!");
        }else {
            isSuccess = save(teacher);
        }
//        // 2. 检查并修改学科库
//        // 2.1 封装 TpaSubject 对象
//        TpaSubject subject = new TpaSubject();
//        subject.setSubjectName(teacher.getTeachLesson()==null?"人工智能通识课":teacher.getTeachLesson());
//        subject.setSubjectStage(teacher.getTeachStage()==null?"大学":teacher.getTeachStage());
//        subject.setGrade(teacher.getStageNum()==null?1:teacher.getStageNum());
//        subject.setVolume(teacher.getVolume()==null?"第1版":teacher.getVolume());
//        subject.setSubtitle(teacher.getSubtitle()==null?"人工智能":teacher.getSubtitle());
        // 2.2 检查学科是否符合实际
//        boolean conforms = subjectCheck.isConforms(subject);
        // 符合规定的格式才可以继续执行
            // 3. 插入数据
                // 返回老师的 uid，表示操作成功启动
        if(isSuccess)
                return teacher.getUid();
        return null;
    }


    /**
     * 更新老师的数据
     * @param teachInfoVo
     * @return
     */
    @Override
    public int updateInfo(TeachInfoVo teachInfoVo) throws LessonException {
        // 1. 校验 uid 不能为空
        if (teachInfoVo.getUid() == null) {
            throw new LessonException("教师ID不能为空");
        }
        // 2. 构建更新条件
        LambdaUpdateWrapper<TpaTeacher> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TpaTeacher::getUid, teachInfoVo.getUid());
        // 3. 构建更新内容
        TpaTeacher teacher = new TpaTeacher();
        // 动态设置非空字段
        if (teachInfoVo.getAccount() != null) {
            teacher.setAccount(teachInfoVo.getAccount());
        }
        if (teachInfoVo.getPassword() != null) {
            teacher.setPassword(MD5Utils.encrypt(teacher.getPassword()));
        }
        if (teachInfoVo.getName() != null) {
            teacher.setName(teachInfoVo.getName().toString());
        }
        if (teachInfoVo.getEmail() != null) {
            teacher.setEmail(teachInfoVo.getEmail());
        }

        if (teachInfoVo.getPhone() != null) {
            teacher.setPhone(teachInfoVo.getPhone());
        }

        // 4. 执行更新操作
        int result = teacherMapper.update(teacher, updateWrapper);

        // 5. 可选：更新关联的课程信息（如果需要）
        // 这里需要取消之前注释掉的代码并完善

        return result;
    }

    @Override
    public TpaSubject getLessonIdByTid(Integer tid) {
        TpaTeacher teacher = getById(tid);
        assert teacher.getTsId()!=null;
        return subjectService.getById(teacher.getTsId());
    }


    /**
     * 结合spring security 来实现登录
     * @param userLoginDTO
     * @return
     * @throws IdentityException
     */
    @Override
public HashMap<String, String> login(UserLoginDTO userLoginDTO) throws IdentityException{

    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(),userLoginDTO.getPassword());
    Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if(Objects.isNull(authenticate)){
            throw new RuntimeException("用户名或密码错误");
        }

    LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
    String userId = loginUser.getTeacher().getUid().toString();
    String jwt = JwtUtil.createJWT(userId);
    redisCache.setCacheObject("login:"+userId,loginUser);
    HashMap<String, String> map = new HashMap<>();
    map.put("token",jwt);
    return map;
//    TpaTeacher user = teacherMapper.findByAccount(userLoginDTO.getAccount());
//
//    if (user == null) {
//        throw new IdentityException("用户不存在");
//    }
//
//    if (!Objects.equals(user.getPassword(),MD5Utils.encrypt(userLoginDTO.getPassword()))) {
//        throw new IdentityException("密码错误");
//    }
//
//    if (user.getIsActive() == 0) {
//        throw new IdentityException("用户已禁用");
//    }
//
//    return user;
}

//todo 退出登录
    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userid = Long.valueOf(loginUser.getTeacher().getUid());
        System.out.println(userid);
        redisCache.deleteObject("login:"+userid);
    }

    //todo 获取当前用户
    @Override
    public TpaTeacher getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return loginUser.getTeacher();
    }

    @Override
    public boolean updateSchoolInfo(Integer userId, Integer schoolId) {
        TpaTeacher user = teacherMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setSchoolId(schoolId);
        return teacherMapper.updateById(user) > 0;
    }

}

//    private void newSubjectBeanAndCheck(TeachInfoVo teachInfoVo) throws ExecutionException, InterruptedException, LessonException {
//        TpaSubject subject = new TpaSubject();
//        subject.setSubjectName(teachInfoVo.getTeachLesson());
//        subject.setSubjectStage(teachInfoVo.getTeachStage());
//        subject.setGrade(teachInfoVo.getStageNum());
//        subject.setVolume(teachInfoVo.getVolume());
////            更改后需要检查课程库中是否有老师教的课程，如果没有就添加这个课程，并将生成该课程的知识点
//        Future<Object> result= subjectUpdateThread.checkAndUpdate(subject, teachInfoVo.getUid());
//        String re = (String) result.get();
//        if (!"操作成功".equals(re)){
//        throw new LessonException("课程信息类型错误");
//        }
//    }


// 直接保存
//                String result = String.valueOf(subjectUpdateThread.checkAndUpdate(subject,teacher.getUid()));
//                CompletableFuture.supplyAsync(() -> {
//                    try {
//                        // 检查教材版本，如果导入的是新版本的教材，就需要更新知识点库（判定教材的标准，1.教材版本，2.出版社，3主编）
//                        String result = String.valueOf(subjectUpdateThread.checkAndUpdate(subject, teacher.getUid()));
//                        return result;
//                    } catch (Exception e) {
//                        if (e.getCause() instanceof LessonException) {
//                            try {
//                                throw (LessonException) e.getCause();
//                            } catch (LessonException ex) {
//                                ex.printStackTrace();
//                            }
//                        } else {
//                            throw new RuntimeException("服务器内部错误", e);
//                        }
//                    }
//                    return null;
//                }).whenComplete((result, exception) -> {
//                    if (exception == null) {
//                        if ("操作成功".equals(result)) {
//                            // 如果操作成功，返回老师的 uid
//                            System.out.println("操作成功，老师 ID: " + teacher.getUid());
//                        } else {
//                            System.out.println(result+"这个错了吗！！！");
//                            // 如果操作失败，抛出异常
//                            System.err.println("操作失败: " + result);
//                        }
//                    } else {
//                        // 处理异常
//                        System.err.println("异步任务发生异常: " + exception.getMessage());
//                        // 在这里可以添加回滚逻辑，例如删除刚插入的老师数据
//                    }
//                });


//    @Override
//    public int updateInfo(TeachInfoVo teachInfoVo) throws LessonException {
//        // 1. 校验 uid 不能为空（根据业务需要）
//        if (teachInfoVo.getUid() == null) {
//            return 0; // 如果 uid 为空或者年级为0，直接返回 0
//        }
//        // 2. 构建更新条件
//        LambdaUpdateWrapper<TpaTeacher> updateWrapper = new LambdaUpdateWrapper<>();
//        updateWrapper.eq(TpaTeacher::getUid, teachInfoVo.getUid());
//        boolean hasUpdate = false; // 用于标记是否有更新字段
//
////        TpaSubject subject = null;
////        if (StringUtils.isNotBlank(teachInfoVo.getSubtitle())) {
////            boolean b = TypeCheck.isAllChinese(teachInfoVo.getSubtitle());
////            if (!b) throw new LessonException("课程信息数据格式不正确");
////            TpaTeacher teacher = getById(teachInfoVo.getUid());
////            assert teacher.getTsId() != null;
////            subject = new TpaSubject();
////            subject.setTsId(teacher.getTsId());
////            subject.setSubtitle(teachInfoVo.getSubtitle());
////            subjectUpdate = true;
////        }
//
//        // 3. 执行更新操作
//        if (hasUpdate) {
//            return teacherMapper.update(null, updateWrapper); // 执行更新操作教师信息
//        } else {return 0;}
//    }
