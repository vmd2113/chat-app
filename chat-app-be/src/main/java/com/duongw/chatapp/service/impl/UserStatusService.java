package com.duongw.chatapp.service.impl;

import com.duongw.chatapp.model.entity.UserStatusLog;
import com.duongw.chatapp.model.enums.UserStatus;
import com.duongw.chatapp.repository.UserStatusLogRepository;
import com.duongw.chatapp.service.IUserStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;


@Service
@Slf4j
public class UserStatusService implements IUserStatusService {

    private final UserStatusLogRepository userStatusRepository;

    public UserStatusService(UserStatusLogRepository userStatusRepository) {
        this.userStatusRepository = userStatusRepository;
    }

    @Transactional
    @Override
    public void logStatusChange(Long userId, UserStatus status) {
        UserStatusLog userStatusLog = UserStatusLog.builder()
                .userId(userId)
                .status(status)
                .timestamp(Instant.now())
                .build();

        userStatusRepository.save(userStatusLog);


    }

    @Override
    public List<Long> getOnlineUsers() {
        return userStatusRepository.findUserIdsByStatusIn(
                List.of(UserStatus.ONLINE, UserStatus.AWAY)
        );
    }

    @Override
    public List<UserStatusLog> getUserStatusHistory(Long userId, int limit) {
        return userStatusRepository.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, limit));
    }
}
