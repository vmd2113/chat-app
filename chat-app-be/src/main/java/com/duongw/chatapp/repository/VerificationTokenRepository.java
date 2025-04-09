package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.Users;
import com.duongw.chatapp.model.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    void deleteByUsers(Users users);

    void deleteByUsers_Id(Long usersId);
    
    
}
