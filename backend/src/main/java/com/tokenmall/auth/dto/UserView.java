package com.tokenmall.auth.dto;

import com.tokenmall.user.entity.SysUser;

public record UserView(Long id, String username, String nickname, String role) {

    public static UserView from(SysUser user) {
        return new UserView(user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }
}
