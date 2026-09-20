package com.tokenmall.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tokenmall.jwt")
public class JwtProperties {

    private String secret;
    private long expiresSeconds = 7200;
}
