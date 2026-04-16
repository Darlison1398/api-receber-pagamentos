package com.auth.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthTokenResponse {

    private String access_token;
    private String refresh_token;
    private Long user_id;

    // getters personalizados (pra facilitar uso)
    public String getAccessToken() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public Long getUserId() {
        return user_id;
    }
    
}
