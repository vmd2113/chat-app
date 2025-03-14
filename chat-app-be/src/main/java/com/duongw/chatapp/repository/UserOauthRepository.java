package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.UserOauth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOauthRepository extends JpaRepository<UserOauth, Long> {

}
