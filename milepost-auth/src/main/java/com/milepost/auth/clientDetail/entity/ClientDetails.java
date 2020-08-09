package com.milepost.auth.clientDetail.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证客户端信息配置
 *
 * @author huarf
 * @since 2020-08-10
 */
@TableName("oauth_client_details")
@ApiModel(value="ClientDetails对象", description="")
@Component
@ConfigurationProperties(prefix="auth.client-details")
public class ClientDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 不需要配置，固定为client_id_ + tenant
     */
    @TableField
    @TableId(value = "client_id", type = IdType.ASSIGN_UUID)
    private String clientId;

    /**
     * 不需要配置，保留。
     */
    private String resourceIds;

    /**
     * 不需要配置，固定为client_secret_ + tenant
     */
    private String clientSecret;

    /**
     * 不需要配置，固定为 all
     */
    private String scope = "all";

    /**
     * 不需要配置，固定为 implicit,refresh_token,password,authorization_code
     */
    private String authorizedGrantTypes = "implicit,refresh_token,password,authorization_code";

    /**
     * 不需要配置，保留。
     */
    private String webServerRedirectUri;

    /**
     * 不需要配置，保留。
     */
    private String authorities;

    /**
     * 默认为 7200
     */
    private Integer accessTokenValidity = 7200;

    /**
     * 默认为 7200
     */
    private Integer refreshTokenValidity = 7200;

    /**
     * 附加信息
     */
    private String additionalInformation;

    /**
     * 不需要配置，固定为 true
     */
    private String autoapprove = "true";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(String resourceIds) {
        this.resourceIds = resourceIds;
    }
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getAuthorizedGrantTypes() {
        return authorizedGrantTypes;
    }

    public void setAuthorizedGrantTypes(String authorizedGrantTypes) {
        this.authorizedGrantTypes = authorizedGrantTypes;
    }
    public String getWebServerRedirectUri() {
        return webServerRedirectUri;
    }

    public void setWebServerRedirectUri(String webServerRedirectUri) {
        this.webServerRedirectUri = webServerRedirectUri;
    }
    public String getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }
    public Integer getAccessTokenValidity() {
        return accessTokenValidity;
    }

    public void setAccessTokenValidity(Integer accessTokenValidity) {
        this.accessTokenValidity = accessTokenValidity;
    }
    public Integer getRefreshTokenValidity() {
        return refreshTokenValidity;
    }

    public void setRefreshTokenValidity(Integer refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }
    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
    public String getAutoapprove() {
        return autoapprove;
    }

    public void setAutoapprove(String autoapprove) {
        this.autoapprove = autoapprove;
    }

    @Override
    public String toString() {
        return "ClientDetails{" +
            "clientId=" + clientId +
            ", resourceIds=" + resourceIds +
            ", clientSecret=" + clientSecret +
            ", scope=" + scope +
            ", authorizedGrantTypes=" + authorizedGrantTypes +
            ", webServerRedirectUri=" + webServerRedirectUri +
            ", authorities=" + authorities +
            ", accessTokenValidity=" + accessTokenValidity +
            ", refreshTokenValidity=" + refreshTokenValidity +
            ", additionalInformation=" + additionalInformation +
            ", autoapprove=" + autoapprove +
        "}";
    }
}
