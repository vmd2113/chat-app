package com.duongw.chatapp.service;

import com.duongw.chatapp.model.entity.UserStatusLog;
import com.duongw.chatapp.model.enums.UserStatus;

import java.util.List;

public interface IUserStatusService {

    void logStatusChange(Long userId, UserStatus status);

    List<Long> getOnlineUsers();

    List<UserStatusLog> getUserStatusHistory(Long userId, int limit);
}
