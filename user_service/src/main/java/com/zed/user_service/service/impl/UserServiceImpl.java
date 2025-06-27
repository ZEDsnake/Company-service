package com.zed.user_service.service.impl;

import com.zed.user_service.dto.CompanyInfoDto;
import com.zed.user_service.dto.CreateUserDto;
import com.zed.user_service.dto.PagedUserResponseDto;
import com.zed.user_service.dto.PatchUserDto;
import com.zed.user_service.dto.UpdateUserDto;
import com.zed.user_service.dto.UserInfoDto;
import com.zed.user_service.dto.UserResponseDto;
import com.zed.user_service.entity.UserEntity;
import com.zed.user_service.exception.AlreadyExistsException;
import com.zed.user_service.exception.NotFoundException;
import com.zed.user_service.feign.CompanyClient;
import com.zed.user_service.mapper.UserMapper;
import com.zed.user_service.repository.UserRepository;
import com.zed.user_service.service.UserService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CompanyClient companyClient;

    @Override
    public UserResponseDto createUser(CreateUserDto dto) {
        checkPhoneUnique(dto.getPhoneNumber());
        checkCompanyExists(dto.getCompanyId());
        UserEntity entity = userMapper.toUserEntity(dto);
        UserEntity saved = userRepository.save(entity);
        syncAddUserToCompany(saved.getId(), saved.getCompanyId());
        CompanyInfoDto company = fetchCompany(saved.getCompanyId());
        UserResponseDto result = userMapper.toUserResponseDto(saved, company);
        log.info("Created user with id: {}", result.getId());
        return result;
    }

    @Override
    public UserResponseDto updateUser(Long id, UpdateUserDto dto) {
        UserEntity entity = getUserEntityById(id);
        checkPhoneChange(entity.getPhoneNumber(), dto.getPhoneNumber());
        checkCompanyExists(dto.getCompanyId());
        Long oldCompanyId = entity.getCompanyId();
        userMapper.updateUserFromDto(dto, entity);
        UserEntity updated = userRepository.save(entity);
        syncCompanyChange(updated.getId(), oldCompanyId, updated.getCompanyId());
        CompanyInfoDto company = fetchCompany(updated.getCompanyId());
        UserResponseDto result = userMapper.toUserResponseDto(updated, company);
        log.info("Updated user with id: {}", result.getId());
        return result;
    }

    @Override
    public UserResponseDto patchUser(Long id, PatchUserDto dto) {
        UserEntity entity = getUserEntityById(id);
        checkPhonePatch(entity.getPhoneNumber(), dto.getPhoneNumber());
        checkCompanyPatch(dto.getCompanyId());
        Long oldCompanyId = entity.getCompanyId();
        userMapper.patchUserFromDto(dto, entity);
        UserEntity patched = userRepository.save(entity);
        syncCompanyPatch(patched.getId(), oldCompanyId, dto.getCompanyId());
        CompanyInfoDto company = fetchCompany(patched.getCompanyId());
        UserResponseDto result = userMapper.toUserResponseDto(patched, company);
        log.info("Patched user with id: {}", result.getId());
        return result;
    }

    @Override
    public void deleteUser(Long id) {
        UserEntity entity = getUserEntityById(id);
        userRepository.deleteById(id);
        syncRemoveUserFromCompany(id, entity.getCompanyId());
        log.info("Deleted user with id: {}", id);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        UserEntity entity = getUserEntityById(id);
        CompanyInfoDto company = fetchCompany(entity.getCompanyId());
        UserResponseDto result = userMapper.toUserResponseDto(entity, company);
        log.info("Fetched user with id: {}", result.getId());
        return result;
    }

    @Override
    public PagedUserResponseDto getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserEntity> pageResult = userRepository.findAll(pageable);
        List<UserResponseDto> users = pageResult.stream()
                .map(user -> userMapper.toUserResponseDto(user, fetchCompany(user.getCompanyId())))
                .collect(Collectors.toList());
        PagedUserResponseDto result = new PagedUserResponseDto(
                users,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements());
        log.info("Fetched {} users", result.getUsers().size());
        return result;
    }

    @Override
    public List<UserInfoDto> getUsersByIds(List<Long> ids) {
        List<UserInfoDto> result = userRepository.findAllById(ids).stream()
                .map(userMapper::toUserInfoDto)
                .collect(Collectors.toList());
        log.info("Fetched {} users by ids", result.size());
        return result;
    }

    @Override
    public void deleteUsersByCompanyId(Long companyId) {
        List<UserEntity> users = userRepository.findAllByCompanyId(companyId);
        userRepository.deleteAll(users);
        log.info("Deleted {} users for company id: {}", users.size(), companyId);
    }

    @Override
    public void updateUserCompany(Long userId, Long companyId) {
        UserEntity user = getUserEntityById(userId);
        user.setCompanyId(companyId);
        userRepository.save(user);
        log.info("Updated user {} company to {}", userId, companyId == null ? "null" : companyId);
    }

    private UserEntity getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id " + id));
    }

    private void checkPhoneUnique(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new AlreadyExistsException("Phone number already exists: " + phoneNumber);
        }
    }

    private void checkPhoneChange(String oldPhone, String newPhone) {
        if (!oldPhone.equals(newPhone)) {
            checkPhoneUnique(newPhone);
        }
    }

    private void checkPhonePatch(String oldPhone, String newPhone) {
        if (newPhone != null && !newPhone.equals(oldPhone)) {
            checkPhoneUnique(newPhone);
        }
    }

    private void checkCompanyExists(Long companyId) {
        if (companyId == null) return;
        try {
            companyClient.getCompanyById(companyId);
        } catch (Exception e) {
            log.error("Company with id {} not found: {}", companyId, e.getMessage());
            throw new NotFoundException("Company not found with id " + companyId);
        }
    }

    private void checkCompanyPatch(Long companyId) {
        if (companyId != null) {
            checkCompanyExists(companyId);
        }
    }

    private void syncAddUserToCompany(Long userId, Long companyId) {
        if (companyId == null) return;
        try {
            companyClient.addEmployee(companyId, userId);
        } catch (Exception e) {
            log.warn("Failed to add user {} to company {}: {}", userId, companyId, e.getMessage());
        }
    }

    private void syncRemoveUserFromCompany(Long userId, Long companyId) {
        if (companyId == null) return;
        try {
            companyClient.removeEmployee(companyId, userId);
        } catch (Exception e) {
            log.warn("Failed to remove user {} from company {}: {}", userId, companyId, e.getMessage());
        }
    }

    private void syncCompanyChange(Long userId, Long oldCompanyId, Long newCompanyId) {
        if (!Objects.equals(oldCompanyId, newCompanyId)) {
            syncRemoveUserFromCompany(userId, oldCompanyId);
            syncAddUserToCompany(userId, newCompanyId);
        }
    }

    private void syncCompanyPatch(Long userId, Long oldCompanyId, Long newCompanyId) {
        if (newCompanyId != null && !Objects.equals(oldCompanyId, newCompanyId)) {
            syncRemoveUserFromCompany(userId, oldCompanyId);
            syncAddUserToCompany(userId, newCompanyId);
        }
    }

    private CompanyInfoDto fetchCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }
        try {
            return companyClient.getCompanyById(companyId);
        } catch (FeignException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.error("Error fetching company with id {}: {}", companyId, e.getMessage());
            return null;
        }
    }
}