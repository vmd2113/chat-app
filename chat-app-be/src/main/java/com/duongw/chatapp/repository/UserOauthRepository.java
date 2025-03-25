package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.UserOauth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOauthRepository extends JpaRepository<UserOauth, Long> {

    Optional<UserOauth> findByProviderAndProviderUserId(String provider, String providerUserId);
    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);

}
