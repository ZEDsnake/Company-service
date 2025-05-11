package com.zed.user_service.mapper;

import com.zed.user_service.dto.CreateUserDTO;
import com.zed.user_service.dto.EmployeeDTO;
import com.zed.user_service.dto.UpdateUserDTO;
import com.zed.user_service.dto.UserDTO;
import com.zed.user_service.dto.UserInfoDTO;
import com.zed.user_service.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserInfoDTO toUserInfoDto(User user);

    List<UserInfoDTO> toUserInfoDtoList(List<User> users);

    User toUserEntity(CreateUserDTO createUserDTO);

    UserDTO toUserDTO(User savedEntity);

    EmployeeDTO toEmployeeDTO(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateUserDTO updateUserDTO, @MappingTarget User user);

    List<UserDTO> toUserDTOList(List<User> userEntities);
}

