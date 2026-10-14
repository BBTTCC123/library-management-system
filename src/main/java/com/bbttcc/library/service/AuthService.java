package com.bbttcc.library.service;

import com.bbttcc.library.dto.auth.AuthResponse;
import com.bbttcc.library.dto.auth.LoginRequest;
import com.bbttcc.library.dto.auth.RegisterRequest;
import com.bbttcc.library.entity.Role;
import com.bbttcc.library.entity.User;
import com.bbttcc.library.exception.ConflictException;
import com.bbttcc.library.exception.ResourceNotFoundException;
import com.bbttcc.library.mapper.UserMapper;
import com.bbttcc.library.repository.UserRepository;
import com.bbttcc.library.security.JwtService;
import com.bbttcc.library.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务：注册与登录。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * 注册新用户，注册成功后直接返回访问令牌（默认角色 MEMBER）。
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("用户名已存在: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("邮箱已被注册: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .email(request.email())
                .role(Role.MEMBER)
                .enabled(true)
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(SecurityUser.from(user));
        return AuthResponse.of(token, jwtService.expirationMs(), UserMapper.toResponse(user));
    }

    /**
     * 登录并签发访问令牌。
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        SecurityUser principal = (SecurityUser) authentication.getPrincipal();
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + principal.getUsername()));

        String token = jwtService.generateToken(principal);
        return AuthResponse.of(token, jwtService.expirationMs(), UserMapper.toResponse(user));
    }
}
