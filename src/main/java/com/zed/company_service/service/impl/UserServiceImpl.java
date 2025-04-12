package com.zed.company_service.service.impl;


import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.EmployeeDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.entity.User;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.mapper.UserMapper;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.repository.UserRepository;
import com.zed.company_service.service.UserService;
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
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public UserDTO createUser(CreateUserDTO createUserDTO) {
        checkPhoneAlreadyExists(createUserDTO.getPhoneNumber());

        CompanyEntity company = findCompanyById(createUserDTO.getCompanyId());

        User user = userMapper.toUserEntity(createUserDTO);
        user.setCompany(company);

        User savedEntity = userRepository.save(user);
        UserDTO result = userMapper.toUserDTO(savedEntity);
        log.info("User created successfully: {}", result);
        return result;
    }

    @Override
    @Transactional
    public UserDTO getUserById(Long id) {
        User userEntity = findUserById(id);
        UserDTO result = userMapper.toUserDTO(userEntity);
        log.info("UserService: getUserById method result: {}", result);
        return result;
    }

    @Override
    @Transactional
    public EmployeeDTO getUserWithCompany(Long id) {
        User user = findUserById(id);
        EmployeeDTO result = userMapper.toEmployeeDTO(user); // company внутри уже маппится как CompanyInfoDTO
        log.info("UserService: getUserWithCompany method result: {}", result);
        return result;
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserDTO updateUserDTO) {
        User existingUser = findUserById(id);
        validatePhoneNumberChange(existingUser, updateUserDTO);

        userMapper.updateEntityFromDTO(updateUserDTO, existingUser);

        updateCompanyIfChanged(existingUser, updateUserDTO.getCompanyId());

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
    public List<UserDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserDTO> userDTOList = userMapper.toUserDTOList(userPage.getContent());
        log.info("Retrieved {} users for page {} with size {}", userDTOList.size(), page, size);
        return userDTOList;
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new NotFoundException("User not found with id: " + id));
    }

    private CompanyEntity findCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + id));
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

    private void updateCompanyIfChanged(User user, Long newCompanyId) {
        if (newCompanyId != null &&
                (user.getCompany() == null || !newCompanyId.equals(user.getCompany().getId()))) {
            CompanyEntity newCompany = findCompanyById(newCompanyId);
            user.setCompany(newCompany);
        }
    }
}
