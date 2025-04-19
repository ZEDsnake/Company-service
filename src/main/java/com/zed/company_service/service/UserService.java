package com.zed.company_service.service;

import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.EmployeeDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.dto.UserInfoDTO;

import java.util.List;

public interface UserService {
    List<UserInfoDTO> getUserInfoByCompanyId(Long companyId, int page, int size);

    void deleteUsersByCompanyId(Long companyId);

    UserDTO createUser(CreateUserDTO userDTO);

    EmployeeDTO getUserWithCompany(Long id);

    UserDTO updateUser(Long id, UpdateUserDTO updateUserDTO);

    void deleteUser(Long id);

    List<EmployeeDTO> getAllUsers( int page, int size);
}
