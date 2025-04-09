package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.ResetToken;
import com.duongw.chatapp.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {
    Optional<ResetToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUser(Users users);
}
