package com.duongw.chatapp.model.dto.response.usersetting;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponseDTO {
    private Long id;
    private Long userId;
    private Boolean notificationEnabled;
    private Boolean notificationSound;
    private Boolean showStatus;
    private String language;
    private String theme;

}
