package com.bing.tpa.service.baseImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bing.tpa.domain.dto.LoginUser;
import com.bing.tpa.domain.entity.TpaTeacher;
import com.bing.tpa.mapper.TpaTeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private TpaTeacherMapper teacherMapper;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        //根据用户名查询用户信息
        LambdaQueryWrapper<TpaTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TpaTeacher::getAccount,account);
        TpaTeacher user = teacherMapper.selectOne(wrapper);
        //如果查询不到数据就通过抛出异常来给出提示
        if(Objects.isNull(user)){
            throw new RuntimeException("用户名或密码错误");
        }
        //TODO 根据用户查询权限信息 添加到LoginUser中

        //封装成UserDetails对象返回
        return new LoginUser(user);
    }

}
