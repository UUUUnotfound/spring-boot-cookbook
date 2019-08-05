package com.tangcheng.app.domain.vo;

import com.tangcheng.app.domain.entity.RoleDo;
import com.tangcheng.app.domain.entity.UserDo;
import org.joda.time.LocalDateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by tang.cheng on 2017/4/13.
 */
public class CustomUserDetails extends UserDo {
    private List<RoleDo> roles;

    public List<RoleDo> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDo> roles) {
        this.roles = roles;
    }

    public Collection<? extends GrantedAuthority> generateAuthorities() {
        //定义权限集合
        List<GrantedAuthority> authorities = new ArrayList<>();
        //当前用户的角色信息集合
        List<RoleDo> roles = this.getRoles();
        //添加角色信息到权限集合
        for (RoleDo role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getType()));
        }
        return authorities;
    }

    public boolean generateAccountNonExpired() {
        return new Date().before(this.getAccountExpired());
    }

    public boolean generateCredentialsNonExpired() {
        return new Date().before(this.getAccountExpired());
    }
}
