package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.base.PageResponse;
import com.duongw.chatapp.model.entity.UserStatusLog;
import com.duongw.chatapp.model.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserStatusLogRepository extends JpaRepository<UserStatusLog, Long> {

    List<UserStatusLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    @Query("SELECT DISTINCT u.userId FROM UserStatusLog u WHERE u.status IN :statuses")
    List<Long> findUserIdsByStatusIn(List<UserStatus> statuses);


}
