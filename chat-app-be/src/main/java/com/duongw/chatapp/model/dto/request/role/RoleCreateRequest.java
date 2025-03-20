package com.duongw.chatapp.model.dto.request.role;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleCreateRequest {

    private String name;

    private String description;

}
