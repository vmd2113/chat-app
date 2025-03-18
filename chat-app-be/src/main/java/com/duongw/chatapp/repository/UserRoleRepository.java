package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.Role;
import com.duongw.chatapp.model.entity.UserRole;
import com.duongw.chatapp.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(Users user);

    List<UserRole> findByRole(Role role);

    Optional<UserRole> findByUserAndRole(Users user, Role role);

    boolean existsByUserAndRole(Users user, Role role);

    void deleteByUserAndRole(Users user, Role role);
}