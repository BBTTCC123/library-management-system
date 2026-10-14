package com.bbttcc.library.service;

import com.bbttcc.library.dto.user.UserResponse;
import com.bbttcc.library.entity.Role;
import com.bbttcc.library.entity.User;
import com.bbttcc.library.exception.BusinessException;
import com.bbttcc.library.exception.ResourceNotFoundException;
import com.bbttcc.library.mapper.UserMapper;
import com.bbttcc.library.repository.UserRepository;
import com.bbttcc.library.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse currentUser() {
        Long id = SecurityUtils.currentUserId();
        return UserMapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserMapper.toResponse(getEntity(id));
    }

    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        User user = getEntity(id);
        if (user.getId().equals(SecurityUtils.currentUserId()) && role != Role.ADMIN) {
            throw new BusinessException("不能修改自己的管理员角色");
        }
        user.setRole(role);
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateEnabled(Long id, boolean enabled) {
        User user = getEntity(id);
        if (user.getId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException("不能禁用当前登录账号");
        }
        user.setEnabled(enabled);
        return UserMapper.toResponse(userRepository.save(user));
    }

    private User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("用户", id));
    }
}
