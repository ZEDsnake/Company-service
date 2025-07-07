package com.zed.user_service.service;

import com.zed.user_service.dto.CreateUserDto;
import com.zed.user_service.dto.PagedUserResponseDto;
import com.zed.user_service.dto.PatchUserDto;
import com.zed.user_service.dto.UpdateUserDto;
import com.zed.user_service.dto.UserInfoDto;
import com.zed.user_service.dto.UserResponseDto;

import java.util.List;


public interface UserService {

    UserResponseDto createUser(CreateUserDto dto);

    UserResponseDto updateUser(Long id, UpdateUserDto dto);

    UserResponseDto patchUser(Long id, PatchUserDto dto);

    void deleteUser(Long id);

    UserResponseDto getUserById(Long id);

    PagedUserResponseDto getAllUsers(int page, int size);

    List<UserInfoDto> getUsersByIds(List<Long> userIds);

    void deleteUsersByCompanyId(Long companyId);

    void updateUserCompany(Long userId, Long companyId);
}