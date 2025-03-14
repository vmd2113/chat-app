package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.UserPrivacyBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrivacyBlockRepository extends JpaRepository<UserPrivacyBlock, Long> {


}
