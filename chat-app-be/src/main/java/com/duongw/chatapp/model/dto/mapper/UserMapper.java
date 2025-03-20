package com.duongw.chatapp.model.dto.mapper;

import com.duongw.chatapp.model.base.DefaultMapperConfig;
import com.duongw.chatapp.model.base.EntityMapper;
import com.duongw.chatapp.model.dto.response.user.UserResponseDTO;
import com.duongw.chatapp.model.entity.Users;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMapperConfig.class)
public interface UserMapper extends EntityMapper<UserResponseDTO, Users> {
}
