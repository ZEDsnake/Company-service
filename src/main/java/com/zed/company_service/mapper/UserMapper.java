package com.zed.company_service.mapper;

import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper{
    @Mapping(target = "company", source = "company")
    UserDTO toUserDTO(User user);

    @Mapping(target = "company", ignore = true)

    User toUserEntity(CreateUserDTO createUserDTO);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "company", ignore = true)
    void updateEntityFromDTO(UpdateUserDTO updateUserDTO, @MappingTarget User user);

    List<UserDTO> toUserDTOList(List<User> userEntities);
}
