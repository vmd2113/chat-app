package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

}
