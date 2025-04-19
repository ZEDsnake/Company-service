package com.zed.company_service.service.impl;


import com.zed.company_service.dto.CompanyInfoDTO;
import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.EmployeeDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.dto.UserInfoDTO;
import com.zed.company_service.entity.User;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.feign.CompanyClient;
import com.zed.company_service.mapper.UserMapper;
import com.zed.company_service.repository.UserRepository;
import com.zed.company_service.service.UserService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CompanyClient companyClient;

    @Override
    public List<UserInfoDTO> getUserInfoByCompanyId(Long companyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<User> users = userRepository.findByCompanyId(companyId, pageable);
        return userMapper.toUserInfoDtoList(users);
    }

    @Override
    @Transactional
    public void deleteUsersByCompanyId(Long companyId) {
        userRepository.deleteByCompanyId(companyId);
    }

    @Override
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        checkPhoneAlreadyExists(createUserDTO.getPhoneNumber());
        checkIfCompanyExists(createUserDTO.getCompanyId());

        User user = userMapper.toUserEntity(createUserDTO);
        user.setCompanyId(createUserDTO.getCompanyId());

        User savedEntity = userRepository.save(user);
        UserDTO result = userMapper.toUserDTO(savedEntity);
        log.info("User created successfully: {}", result);
        return result;
    }

    @Override
    @Transactional
    public EmployeeDTO getUserWithCompany(Long userId) {
        User user = findUserById(userId);
        EmployeeDTO dto = userMapper.toEmployeeDTO(user);
        dto.setCompany(fetchCompany(user.getCompanyId()));
        log.info("Returning employee with company info: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserDTO updateUserDTO) {
        User existingUser = findUserById(id);
        validatePhoneNumberChange(existingUser, updateUserDTO);

        checkIfCompanyExists(updateUserDTO.getCompanyId());

        userMapper.updateEntityFromDTO(updateUserDTO, existingUser);
        existingUser.setCompanyId(updateUserDTO.getCompanyId());

        User updatedEntity = userRepository.save(existingUser);
        UserDTO result = userMapper.toUserDTO(updatedEntity);
        log.info("User updated successfully: {}", result);
        return result;
    }


    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
        log.info("UserService: User with id={} deleted successfully", id);
    }

    @Override
    @Transactional
    public List<EmployeeDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);

        List<EmployeeDTO> employeeDTOList = mapUsersToEmployeeDTOList(userPage.getContent());

        log.info("Retrieved {} users for page {} with size {}", employeeDTOList.size(), page, size);
        return employeeDTOList;
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new NotFoundException("User not found with id: " + id));
    }

    private void checkPhoneAlreadyExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new AlreadyExistsException(
                    "User with phone number " + phoneNumber + " already exists");
        }
    }

    private void validatePhoneNumberChange(User existingUser, UpdateUserDTO updateUserDTO) {
        if (isPhoneNumberChanged(existingUser, updateUserDTO)) {
            checkPhoneAlreadyExists(updateUserDTO.getPhoneNumber());
        }
    }

    private boolean isPhoneNumberChanged(User existingUser, UpdateUserDTO updateUserDTO) {
        return !existingUser.getPhoneNumber().equals(updateUserDTO.getPhoneNumber());
    }

    private void checkIfCompanyExists(Long companyId) {
        try {
            companyClient.getCompanyById(companyId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Company not found with ID: " + companyId);
        }
    }

    private CompanyInfoDTO fetchCompany(Long companyId) {
        return companyId != null ? companyClient.getCompanyById(companyId) : null;
    }

    private EmployeeDTO mapUserToEmployeeDTOWithCompany(User user) {
        EmployeeDTO dto = userMapper.toEmployeeDTO(user);
        dto.setCompany(fetchCompany(user.getCompanyId()));
        return dto;
    }

    private List<EmployeeDTO> mapUsersToEmployeeDTOList(List<User> users) {
        return users.stream()
                .map(this::mapUserToEmployeeDTOWithCompany)
                .toList();
    }

}
