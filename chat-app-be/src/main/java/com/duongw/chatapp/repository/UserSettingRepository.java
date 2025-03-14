package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserSettingRepository extends JpaRepository<UserSettings, Long> {
}
