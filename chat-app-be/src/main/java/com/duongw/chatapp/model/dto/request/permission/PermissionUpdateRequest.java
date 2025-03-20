package com.duongw.chatapp.model.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class PermissionUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

}
