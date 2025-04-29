package com.zed.user_service.service;



import com.zed.user_service.dto.CreateUserDTO;
import com.zed.user_service.dto.EmployeeDTO;
import com.zed.user_service.dto.UpdateUserDTO;
import com.zed.user_service.dto.UserDTO;
import com.zed.user_service.dto.UserInfoDTO;

import java.util.List;

public interface UserService {
    List<UserInfoDTO> getUserInfoByCompanyId(Long companyId, int page, int size);

    void deleteUsersByCompanyId(Long companyId);

    UserDTO createUser(CreateUserDTO userDTO);

    EmployeeDTO getUserWithCompany(Long id);

    UserDTO updateUser(Long id, UpdateUserDTO updateUserDTO);

    void deleteUser(Long id);

    List<UserDTO> getAllUsers( int page, int size);
}
