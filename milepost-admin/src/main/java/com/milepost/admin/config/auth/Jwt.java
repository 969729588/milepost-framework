package com.milepost.admin.config.auth;

/**
 * Created by Ruifu Hua on 2020/1/29.
 * Json Web Token
 */
public class Jwt {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private long born_time_millis;
    private String scope;
    private String jti;

    //在这个时间之后过期，milepost-admin中特有的属性，authentication-parent中没有
    private long expires_at;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public long getBorn_time_millis() {
        return born_time_millis;
    }

    public void setBorn_time_millis(long born_time_millis) {
        this.born_time_millis = born_time_millis;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public long getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(long expires_at) {
        this.expires_at = expires_at;
    }

    @Override
    public String toString() {
        return "Jwt{" +
                "access_token='" + access_token + '\'' +
                ", token_type='" + token_type + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", expires_in=" + expires_in +
                ", born_time_millis=" + born_time_millis +
                ", scope='" + scope + '\'' +
                ", jti='" + jti + '\'' +
                ", expires_at=" + expires_at +
                '}';
    }
}
