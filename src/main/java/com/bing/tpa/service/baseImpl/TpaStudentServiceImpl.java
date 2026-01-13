package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bing.tpa.domain.entity.TpaStudent;
import com.bing.tpa.mapper.TpaStudentMapper;
import com.bing.tpa.service.baseService.TpaStudentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuc
 * @since 2025-02-21
 */
@Service
public class TpaStudentServiceImpl extends ServiceImpl<TpaStudentMapper, TpaStudent> implements TpaStudentService {


    @Override
    public Integer addOneStu(TpaStudent student) {
        if(student.getStuStage()!=null&&student.getStuName()!=null&&student.getStuNum()!=null){
            if(save(student)){
                return student.getSid();
            }else return -1;
        }else {
            throw new NullPointerException("学生参数不可以为空");
        }
    }

    @Override
    public Integer isExit(TpaStudent tpaStudent) {
        TpaStudent student=lambdaQuery()
                .eq(TpaStudent::getStuName, tpaStudent.getStuName())
                .eq(TpaStudent::getStuNum, tpaStudent.getStuNum())
                .one();
        if(student!=null) return student.getSid();
       return null;
    }
}
