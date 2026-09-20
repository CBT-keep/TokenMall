package com.tokenmall.security;

import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static SecurityUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return securityUser;
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }
}
