package com.bing.tpa.common.filter;

import com.bing.tpa.domain.dto.LoginUser;
import com.bing.tpa.utils.jwt.JwtUtil;
import com.bing.tpa.utils.jwt.RedisCache;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

//TODO 对token的校验过滤
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        获取token
        String token = request.getHeader("token");
        if(!StringUtils.hasText(token)){
//            放行
            filterChain.doFilter(request,response);
            return;
        }
//        System.out.println("访问失败！");

//        解析token
        String teacherId;
        try {
            Claims claims = JwtUtil.parseJWT(token);//解析token中的jwt，将老师的id解析出来
            teacherId=claims.getSubject();
//            System.out.println("老师的id为"+teacherId);
        } catch (Exception e) {
            throw new RuntimeException("token非法");
        }
        // 根据老师的id从redis中获取用户信息
        String key="login:"+teacherId;
        LoginUser loginUser = redisCache.getCacheObject(key);
        if(Objects.isNull(loginUser)){
            throw new RuntimeException("用户未登录");
        }
        //存入SecurityContextHolder
        //TODO 获取权限信息封装到SecurityContextHolder的Authentication中
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//        放行
        filterChain.doFilter(request,response);

    }
}
