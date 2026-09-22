package com.tokenmall.auth;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.auth.dto.LoginRequest;
import com.tokenmall.auth.dto.LoginResponse;
import com.tokenmall.auth.dto.RegisterRequest;
import com.tokenmall.auth.dto.UserView;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.security.JwtService;
import com.tokenmall.security.SecurityUser;
import com.tokenmall.security.SecurityUtils;
import com.tokenmall.security.UserAuthCacheService;
import com.tokenmall.security.UserAuthSnapshot;
import com.tokenmall.user.entity.SysUser;
import com.tokenmall.user.entity.UserTokenAccount;
import com.tokenmall.user.mapper.SysUserMapper;
import com.tokenmall.user.mapper.UserTokenAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserTokenAccountMapper userTokenAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserAuthCacheService userAuthCacheService;

    @Transactional
    public UserView register(RegisterRequest request) {
        Long count = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.username())
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setRole("USER");
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);

        UserTokenAccount account = new UserTokenAccount();
        account.setUserId(user.getId());
        account.setPackBalance(0L);
        account.setPlanBalance(0L);
        account.setTotalPurchased(0L);
        account.setTotalConsumed(0L);
        userTokenAccountMapper.insert(account);

        userAuthCacheService.put(UserAuthSnapshot.from(user));
        return UserView.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, request.username())
        );
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已禁用");
        }

        UserAuthSnapshot snapshot = UserAuthSnapshot.from(user);
        userAuthCacheService.put(snapshot);
        return new LoginResponse(
                jwtService.generateToken(new SecurityUser(snapshot)),
                "Bearer",
                jwtService.getExpiresSeconds(),
                UserView.from(user)
        );
    }

    public UserView currentUser() {
        SysUser user = sysUserMapper.selectById(SecurityUtils.currentUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return UserView.from(user);
    }
}
