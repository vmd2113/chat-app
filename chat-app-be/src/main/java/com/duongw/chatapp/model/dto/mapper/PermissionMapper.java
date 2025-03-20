package com.duongw.chatapp.model.dto.mapper;

import com.duongw.chatapp.model.base.DefaultMapperConfig;
import com.duongw.chatapp.model.base.EntityMapper;
import com.duongw.chatapp.model.dto.response.permission.PermissionResponseDTO;
import com.duongw.chatapp.model.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(config = DefaultMapperConfig.class)
public interface PermissionMapper extends EntityMapper<PermissionResponseDTO, Permission> {


}
