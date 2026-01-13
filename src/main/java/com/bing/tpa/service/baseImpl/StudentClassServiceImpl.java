package com.bing.tpa.service.baseImpl;

import com.bing.tpa.mapper.StudentClassMapper;
import com.bing.tpa.domain.entity.StudentClass;
import com.bing.tpa.service.baseService.StudentClassService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class StudentClassServiceImpl extends ServiceImpl<StudentClassMapper, StudentClass> implements StudentClassService {

    @Override
    public boolean addConnection(List<StudentClass> studentClasses) {
       return saveBatch(studentClasses);
    }
}
