package com.duongw.chatapp.validation;

import com.duongw.chatapp.exception.AlreadyExistedException;
import com.duongw.chatapp.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PermissionValidator {

    private final PermissionRepository repository;

    public void validatePermissionName(String name, Long id) {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Permission is required");
        } else {
            boolean existed;
            if (id != null) {
                existed = repository.existsByName(name);
            } else {
                existed = repository.existsByNameAndIdNot(name, id);
            }
            if (existed) {
                throw new AlreadyExistedException("Permission is already existed");
            }

        }
    }
}
