package com.zed.user_service.mapper;

import com.zed.user_service.dto.CreateUserDTO;
import com.zed.user_service.dto.UpdateUserDTO;
import com.zed.user_service.dto.UserDTO;
import com.zed.user_service.entity.AppUserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper{
    AppUserEntity toUserEntity(CreateUserDTO createUserDTO);

    UserDTO toUserDTO(AppUserEntity appUserEntity);

    // Обновление Entity из UpdateDTO
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(UpdateUserDTO updateUserDTO, @MappingTarget AppUserEntity appUserEntity);

    // Преобразование списка Entity в список DTO
    List<UserDTO> toUserDTOList(List<AppUserEntity> userEntities);
}
