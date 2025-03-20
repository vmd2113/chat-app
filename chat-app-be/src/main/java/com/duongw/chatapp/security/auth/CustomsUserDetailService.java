package com.duongw.chatapp.security.auth;

import com.duongw.chatapp.model.entity.Permission;
import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.model.entity.Users;
import com.duongw.chatapp.repository.UserRepository;
import com.duongw.chatapp.service.IRolePermissionService;
import com.duongw.chatapp.service.IRoleService;
import com.duongw.chatapp.service.IUserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomsUserDetailService implements UserDetailsService {

    private UserRepository userRepository;
    private IUserRoleService userRoleService;
    private IRolePermissionService permissionService;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Users> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with " + email);
        }

        Users authUser = users.get();
        List<Role> roles = userRoleService.getRolesForUser(authUser);

        Set<String> roleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissions = new HashSet<>();

        for (Role role : roles) {
            List<Permission> rolePermissions = permissionService.getPermissionsForRole(role);
            Set<String> permissionNames = rolePermissions.stream().map(Permission::getName).collect(Collectors.toSet());
            permissions.addAll(permissionNames);
        }

        return new AuthUserDetails(authUser, roleNames, permissions);

    }
}
