package com.zed.company_service.service;

import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO createUser(CreateUserDTO userDTO);

    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UpdateUserDTO updateUserDTO);

    void deleteUser(Long id);

    List<UserDTO> getAllUsers(int page, int size);
}
