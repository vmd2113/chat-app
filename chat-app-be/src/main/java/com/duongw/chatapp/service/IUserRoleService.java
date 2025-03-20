package com.duongw.chatapp.service;

import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.model.entity.UserRole;
import com.duongw.chatapp.model.entity.Users;

import java.util.List;

public interface IUserRoleService {

    List<UserRole> findByUser(Users user);

    List<Role> getRolesForUser(Users user);

     List<Users> getUsersWithRole(Role role);

    void assignRoleToUser(Users user, Role role);

     void assignRoleToUser(Long userId, Long roleId);

    void removeRoleFromUser(Users user, Role role);

    void removeRoleFromUser(Long userId, Long roleId);

    boolean hasRole(Users user, String roleName);


}


