package com.duongw.chatapp.model.dto.mapper;

import com.duongw.chatapp.model.base.DefaultMapperConfig;
import com.duongw.chatapp.model.base.EntityMapper;
import com.duongw.chatapp.model.dto.response.usersetting.UserSettingsResponseDTO;
import com.duongw.chatapp.model.entity.UserSettings;
import com.duongw.chatapp.model.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface UserSettingMapper extends EntityMapper<UserSettingsResponseDTO, UserSettings> {

    @Mapping(target = "userId", source = "user.id")
    UserSettingsResponseDTO toDto(UserSettings entity);


    @Mapping(target = "user", ignore = true)
    UserSettings toEntity(UserSettingsResponseDTO dto);
    // Add this method to handle user mapping in a custom way
    default UserSettings mapWithUser(UserSettingsResponseDTO dto, Users user) {
        UserSettings settings = toEntity(dto);
        settings.setUser(user);
        return settings;
    }

}
