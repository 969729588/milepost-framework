package com.milepost.auth.user.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

public class User implements UserDetails, Serializable {
    /**
     * user.id
     * 
     *
     * @mbggenerated
     */
    private String id;

    /**
     * user.username
     * 用户名，登录名，是唯一键
     *
     * @mbggenerated
     */
    private String username;

    /**
     * user.truename
     * 用户真是姓名
     *
     * @mbggenerated
     */
    private String truename;

    /**
     * user.mobile
     * 手机号码
     *
     * @mbggenerated
     */
    private String mobile;

    /**
     * user.email
     * 邮箱
     *
     * @mbggenerated
     */
    private String email;

    /**
     * user.password
     * 密码
     *
     * @mbggenerated
     */
    private String password;

    /**
     * user.activated
     * 激活状态，1激活，0未激活
     *
     * @mbggenerated
     */
    private Boolean activated;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getUsername() {
        return username;
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

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getTruename() {
        return truename;
    }

    public void setTruename(String truename) {
        this.truename = truename == null ? null : truename.trim();
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    /**
     * 角色，权限，不在认证服务端实现，
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", username=").append(username);
        sb.append(", truename=").append(truename);
        sb.append(", mobile=").append(mobile);
        sb.append(", email=").append(email);
        sb.append(", password=").append(password);
        sb.append(", activated=").append(activated);
        sb.append("]");
        return sb.toString();
    }
}