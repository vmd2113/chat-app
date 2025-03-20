package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.model.entity.UserRole;
import com.duongw.chatapp.model.entity.Users;
import com.duongw.chatapp.repository.RoleRepository;
import com.duongw.chatapp.repository.UserRepository;
import com.duongw.chatapp.repository.UserRoleRepository;
import com.duongw.chatapp.service.IUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserRoleService implements IUserRoleService {


    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    public UserRoleService(UserRoleRepository userRoleRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


    @Transactional(readOnly = true)
    public List<UserRole> findByUser(Users user) {
        return userRoleRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Role> getRolesForUser(Users user) {
        return userRoleRepository.findByUser(user).stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Users> getUsersWithRole(Role role) {
        return userRoleRepository.findByRole(role).stream()
                .map(UserRole::getUser)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignRoleToUser(Users user, Role role) {
        if (!userRoleRepository.existsByUserAndRole(user, role)) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        Optional<Users> userOpt = userRepository.findById(userId);
        Optional<Role> roleOpt = roleRepository.findById(roleId);

        if (userOpt.isPresent() && roleOpt.isPresent()) {
            assignRoleToUser(userOpt.get(), roleOpt.get());
        }
    }

    @Transactional
    public void removeRoleFromUser(Users user, Role role) {
        userRoleRepository.deleteByUserAndRole(user, role);
    }

    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        Optional<Users> userOpt = userRepository.findById(userId);
        Optional<Role> roleOpt = roleRepository.findById(roleId);

        if (userOpt.isPresent() && roleOpt.isPresent()) {
            removeRoleFromUser(userOpt.get(), roleOpt.get());
        }
    }

    @Transactional(readOnly = true)
    public boolean hasRole(Users user, String roleName) {
        Optional<Role> role = roleRepository.findByName(roleName);
        return role.map(r -> userRoleRepository.existsByUserAndRole(user, r))
                .orElse(false);
    }
}
