package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {
}
