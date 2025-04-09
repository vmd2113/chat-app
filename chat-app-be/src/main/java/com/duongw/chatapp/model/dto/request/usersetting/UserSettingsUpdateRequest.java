package com.duongw.chatapp.model.dto.request.usersetting;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsUpdateRequest {

    private Boolean notificationEnabled;
    private Boolean notificationSound;
    private Boolean showStatus;
    private String language;
    private String theme;
}
