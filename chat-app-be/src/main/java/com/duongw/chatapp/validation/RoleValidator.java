package com.duongw.chatapp.validation;

import com.duongw.chatapp.exception.AlreadyExistedException;
import com.duongw.chatapp.model.dto.request.role.RoleCreateRequest;
import com.duongw.chatapp.model.dto.request.role.RoleUpdateRequest;
import com.duongw.chatapp.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleValidator {

    private final RoleRepository roleRepository;

    public RoleValidator(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void validateRoleName(String name, Long id) {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Role name is required");
        } else {
            boolean existed;
            if (id != null) {
                existed = roleRepository.existsByName(name);
            } else {
                existed = roleRepository.existsByNameAndIdNot(name, id);
            }

            if (existed) {
                throw new AlreadyExistedException("Role name is already existed");
            }

        }
    }

}
