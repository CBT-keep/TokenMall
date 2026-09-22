package com.tokenmall.security;

import com.tokenmall.user.entity.SysUser;

public record UserAuthSnapshot(
        Long userId,
        String username,
        String role,
        boolean enabled
) {

    public static UserAuthSnapshot from(SysUser user) {
        return new UserAuthSnapshot(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                Integer.valueOf(1).equals(user.getStatus())
        );
    }
}
