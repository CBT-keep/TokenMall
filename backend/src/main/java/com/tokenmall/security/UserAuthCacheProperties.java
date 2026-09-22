package com.tokenmall.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tokenmall.auth-cache")
public class UserAuthCacheProperties {

    private long localTtlSeconds = 300;
    private long redisTtlSeconds = 1800;
    private long localMaximumSize = 10000;
}
