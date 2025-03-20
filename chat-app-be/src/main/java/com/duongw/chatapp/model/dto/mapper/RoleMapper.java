package com.duongw.chatapp.model.dto.mapper;

import com.duongw.chatapp.model.base.DefaultMapperConfig;
import com.duongw.chatapp.model.base.EntityMapper;
import com.duongw.chatapp.model.dto.response.role.RoleResponseDTO;
import com.duongw.chatapp.model.entity.Role;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMapperConfig.class)
public interface RoleMapper extends EntityMapper<RoleResponseDTO, Role> {


}
