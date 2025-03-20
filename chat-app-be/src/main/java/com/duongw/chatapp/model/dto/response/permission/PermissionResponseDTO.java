package com.duongw.chatapp.model.dto.response.permission;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDTO {

    private Long id;

    private String name;

    private String description;
}
