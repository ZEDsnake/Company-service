package com.zed.user_service.mapper;

import com.zed.user_service.dto.CompanyInfoDto;
import com.zed.user_service.dto.CreateUserDto;
import com.zed.user_service.dto.PatchUserDto;
import com.zed.user_service.dto.UpdateUserDto;
import com.zed.user_service.dto.UserInfoDto;
import com.zed.user_service.dto.UserResponseDto;
import com.zed.user_service.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toUserEntity(CreateUserDto dto);

    @Mappings({
            @Mapping(source = "user.id", target = "id"),
            @Mapping(source = "user.firstName", target = "firstName"),
            @Mapping(source = "user.lastName", target = "lastName"),
            @Mapping(source = "user.phoneNumber", target = "phoneNumber"),
            @Mapping(source = "company", target = "company")
    })
    UserResponseDto toUserResponseDto(UserEntity user, CompanyInfoDto company);

    @Mapping(target = "id", source = "id")
    UserInfoDto toUserInfoDto(UserEntity user);

    void updateUserFromDto(UpdateUserDto dto, @MappingTarget UserEntity user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchUserFromDto(PatchUserDto dto, @MappingTarget UserEntity user);
}
