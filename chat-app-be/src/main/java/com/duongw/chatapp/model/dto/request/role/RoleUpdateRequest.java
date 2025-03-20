package com.duongw.chatapp.model.dto.request.role;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RoleUpdateRequest {

    private String name;

    private String description;

}
