package com.mik.oauth.enetity;

import com.mik.db.entity.BaseEntity;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Table("client")
@Data
public class Client extends BaseEntity {
    @Id(keyType = KeyType.Auto)
    private Long id;

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String scope;

    /**
     *  authorization_code：授权码模式
     *  implicit：隐式模式
     *  password：资源所有者密码凭证模式
     *  client_credentials：客户端凭证模式
     *  refresh_token：刷新令牌模式
     */
    private String grantType;

    public Collection<String> getScopes() {
        return Arrays.stream(getScope().split(",")).collect(Collectors.toList());
    }

    public Collection<String> getRedirectUris() {
        return Arrays.stream(getRedirectUri().split(",")).collect(Collectors.toList());
    }
}
