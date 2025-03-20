package com.duongw.chatapp.model.dto.response.role;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RoleResponseDTO {

    private Long id;
    private String name;
    private String description;


}
