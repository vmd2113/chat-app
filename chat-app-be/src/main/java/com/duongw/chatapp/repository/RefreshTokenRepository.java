package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.RefreshToken;
import com.duongw.chatapp.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevokedFalseAndExpiresAtAfter(Users user, Instant now);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId AND r.revoked = false")
    void revokeAllUserTokens(Long userId);


    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllUserTokens(Users user);


    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    void deleteAllExpiredTokens(Instant now);

    Optional<RefreshToken> findByUserAndDeviceInfo(Users user, String deviceInfo);

    Optional<RefreshToken> findByUser_Id(Long id);

    void deleteByUserId(Long userId);

}
