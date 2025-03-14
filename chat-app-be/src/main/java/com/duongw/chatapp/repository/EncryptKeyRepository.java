package com.duongw.chatapp.repository;

import com.duongw.chatapp.model.entity.EncryptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EncryptKeyRepository extends JpaRepository<EncryptionKey, Long> {
}
