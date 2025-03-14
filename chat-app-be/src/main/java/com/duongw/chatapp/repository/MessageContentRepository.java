package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.MessageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageContentRepository extends JpaRepository<MessageContent, Long> {

}
