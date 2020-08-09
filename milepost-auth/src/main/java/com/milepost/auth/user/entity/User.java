package com.milepost.auth.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>
 * 
 * </p>
 *
 * @author huarf
 * @since 2020-08-10
 */
@TableName("user")
@ApiModel(value="User对象", description="")
public class User implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "用户名，登录名，是唯一键")
    private String username;

    @ApiModelProperty(value = "用户真是姓名")
    private String truename;

    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "激活状态，1激活，0未激活")
    private Boolean activated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getTruename() {
        return truename;
    }

    public void setTruename(String truename) {
        this.truename = truename;
    }
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", username=" + username +
            ", truename=" + truename +
            ", mobile=" + mobile +
            ", email=" + email +
            ", password=" + password +
            ", activated=" + activated +
        "}";
    }

    /**
     * 指明该用户是否获取，
     * @return  true表示没过期、可用，false表示过期了、不可用
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 指明该用户是否被锁定，
     * @return  true表示没被锁定、可用，false表示被锁定了、不可用
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.activated;
    }

    /**
     * 指明该用户的密码是否过期，
     * @return  true表示没过期、可用，false表示过期了、不可用
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否启用此用户，
     * @return  true表示启用，false表示不启用
     */
    @Override
    public boolean isEnabled() {
        return this.activated;
    }

    /**
     * 角色，权限，不在认证服务端实现，
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

}
