package com.bing.tpa.domain.dto;

import com.bing.tpa.domain.entity.TpaTeacher;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {
    //TODO 以老师实体类作为LoginUser的成员变量，这样就可以将从数据库侯中获取的老师对象teacher初始化到LoginUser这个实体类中，然后就可以在getUsername获取teacher的用户名account,以及密码password
    //todo UserDetailsServiceImpl类中loadUserByUsername中的new LoginUser(user); 这个代码就是在将数据库中获取的老师对象初始化teacher这个成员变量
    private TpaTeacher teacher;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将数据库中的角色字符串转换为Spring Security所需的GrantedAuthority对象
        // 假设teacher.getRole()返回的是类似"ROLE_ADMIN"或"ROLE_USER"的字符串
        return Collections.singletonList(new SimpleGrantedAuthority(teacher.getRole()));
    }


    @Override
    public String getPassword() {
        return teacher.getPassword();
    }

    @Override
    public String getUsername() {
        return teacher.getAccount();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
