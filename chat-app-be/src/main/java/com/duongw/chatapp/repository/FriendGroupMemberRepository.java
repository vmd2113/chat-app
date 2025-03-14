package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.FriendGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendGroupMemberRepository extends JpaRepository<FriendGroupMember, Long> {

}
