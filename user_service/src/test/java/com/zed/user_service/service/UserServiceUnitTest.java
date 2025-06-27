package com.zed.user_service.service;

import com.zed.user_service.dto.CompanyInfoDto;
import com.zed.user_service.dto.CreateUserDto;
import com.zed.user_service.dto.PatchUserDto;
import com.zed.user_service.dto.UpdateUserDto;
import com.zed.user_service.dto.UserInfoDto;
import com.zed.user_service.dto.UserResponseDto;
import com.zed.user_service.dto.PagedUserResponseDto;
import com.zed.user_service.entity.UserEntity;
import com.zed.user_service.exception.AlreadyExistsException;
import com.zed.user_service.exception.NotFoundException;
import com.zed.user_service.feign.CompanyClient;
import com.zed.user_service.mapper.UserMapper;
import com.zed.user_service.repository.UserRepository;
import com.zed.user_service.service.impl.UserServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CompanyClient companyClient;

    @InjectMocks
    private UserServiceImpl userService;

    private final Long VALID_ID = 1L;
    private final Long INVALID_ID = 999L;
    private final Long COMPANY_ID = 2L;
    private final String PHONE = "+71234567890";

    @Test
    void createUser_ShouldReturnUserResponse_WhenValid() {
        CreateUserDto dto = new CreateUserDto("John", "Doe", PHONE, COMPANY_ID);
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        CompanyInfoDto company = new CompanyInfoDto(COMPANY_ID, "Test Company", new BigDecimal("1000.00"));
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("John").lastName("Doe").phoneNumber(PHONE).company(company).build();

        when(userRepository.existsByPhoneNumber(PHONE)).thenReturn(false);
        when(companyClient.getCompanyById(COMPANY_ID)).thenReturn(company);
        when(userMapper.toUserEntity(dto)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toUserResponseDto(entity, company)).thenReturn(response);

        UserResponseDto result = userService.createUser(dto);

        assertEquals(response, result);
        verify(userRepository).existsByPhoneNumber(PHONE);
        verify(companyClient, times(2)).getCompanyById(COMPANY_ID);
        verify(userMapper).toUserEntity(dto);
        verify(userRepository).save(entity);
        verify(companyClient).addEmployee(COMPANY_ID, VALID_ID);
        verify(userMapper).toUserResponseDto(entity, company);
    }

    @Test
    void createUser_ShouldThrowAlreadyExists_WhenPhoneExists() {
        CreateUserDto dto = new CreateUserDto("John", "Doe", PHONE, COMPANY_ID);

        when(userRepository.existsByPhoneNumber(PHONE)).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(dto));
        verify(userRepository).existsByPhoneNumber(PHONE);
        verifyNoMoreInteractions(userRepository, companyClient, userMapper);
    }

    @Test
    void createUser_ShouldThrowNotFound_WhenCompanyNotFound() {
        CreateUserDto dto = new CreateUserDto("John", "Doe", PHONE, COMPANY_ID);

        when(userRepository.existsByPhoneNumber(PHONE)).thenReturn(false);
        when(companyClient.getCompanyById(COMPANY_ID)).thenThrow(FeignException.NotFound.class);

        assertThrows(NotFoundException.class, () -> userService.createUser(dto));
        verify(userRepository).existsByPhoneNumber(PHONE);
        verify(companyClient).getCompanyById(COMPANY_ID);
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void createUser_ShouldHandleNullCompanyId() {
        CreateUserDto dto = new CreateUserDto("John", "Doe", PHONE, null);
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, null);
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("John").lastName("Doe").phoneNumber(PHONE).company(null).build();

        when(userRepository.existsByPhoneNumber(PHONE)).thenReturn(false);
        when(userMapper.toUserEntity(dto)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toUserResponseDto(entity, null)).thenReturn(response);

        UserResponseDto result = userService.createUser(dto);

        assertEquals(response, result);
        verify(userRepository).existsByPhoneNumber(PHONE);
        verify(userMapper).toUserEntity(dto);
        verify(userRepository).save(entity);
        verify(userMapper).toUserResponseDto(entity, null);
        verifyNoInteractions(companyClient);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenValid() {
        UpdateUserDto dto = new UpdateUserDto("Jane", "Doe", PHONE, COMPANY_ID);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserEntity updated = new UserEntity(VALID_ID, "Jane", "Doe", PHONE, COMPANY_ID);
        CompanyInfoDto company = new CompanyInfoDto(COMPANY_ID, "Test Company", new BigDecimal("1000.00"));
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("Jane").lastName("Doe").phoneNumber(PHONE).company(company).build();

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(companyClient.getCompanyById(COMPANY_ID)).thenReturn(company);
        doNothing().when(userMapper).updateUserFromDto(dto, existing);
        when(userRepository.save(existing)).thenReturn(updated);
        when(userMapper.toUserResponseDto(updated, company)).thenReturn(response);

        UserResponseDto result = userService.updateUser(VALID_ID, dto);

        assertEquals(response, result);
        verify(userRepository).findById(VALID_ID);
        verify(companyClient, times(2)).getCompanyById(COMPANY_ID);
        verify(userMapper).updateUserFromDto(dto, existing);
        verify(userRepository).save(existing);
        verify(userMapper).toUserResponseDto(updated, company);
    }

    @Test
    void updateUser_ShouldThrowNotFound_WhenUserNotFound() {
        UpdateUserDto dto = new UpdateUserDto("Jane", "Doe", PHONE, COMPANY_ID);

        when(userRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(INVALID_ID, dto));
        verify(userRepository).findById(INVALID_ID);
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void updateUser_ShouldThrowAlreadyExists_WhenPhoneChangedAndExists() {
        UpdateUserDto dto = new UpdateUserDto("Jane", "Doe", "+79876543210", COMPANY_ID);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(userRepository.existsByPhoneNumber("+79876543210")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(VALID_ID, dto));
        verify(userRepository).findById(VALID_ID);
        verify(userRepository).existsByPhoneNumber("+79876543210");
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void updateUser_ShouldThrowNotFound_WhenCompanyNotFound() {
        UpdateUserDto dto = new UpdateUserDto("Jane", "Doe", PHONE, COMPANY_ID);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(companyClient.getCompanyById(COMPANY_ID)).thenThrow(FeignException.NotFound.class);

        assertThrows(NotFoundException.class, () -> userService.updateUser(VALID_ID, dto));
        verify(userRepository).findById(VALID_ID);
        verify(companyClient).getCompanyById(COMPANY_ID);
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void updateUser_ShouldSyncCompanyChange_WhenCompanyIdChanged() {
        UpdateUserDto dto = new UpdateUserDto("Jane", "Doe", PHONE, 3L);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserEntity updated = new UserEntity(VALID_ID, "Jane", "Doe", PHONE, 3L);
        CompanyInfoDto company = new CompanyInfoDto(3L, "New Company", new BigDecimal("2000.00"));
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("Jane").lastName("Doe").phoneNumber(PHONE).company(company).build();

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(companyClient.getCompanyById(3L)).thenReturn(company);
        doNothing().when(userMapper).updateUserFromDto(dto, existing);
        when(userRepository.save(existing)).thenReturn(updated);
        when(userMapper.toUserResponseDto(updated, company)).thenReturn(response);

        UserResponseDto result = userService.updateUser(VALID_ID, dto);

        assertEquals(response, result);
        verify(userRepository).findById(VALID_ID);
        verify(companyClient, times(2)).getCompanyById(3L);
        verify(userMapper).updateUserFromDto(dto, existing);
        verify(userRepository).save(existing);
        verify(companyClient).removeEmployee(COMPANY_ID, VALID_ID);
        verify(companyClient).addEmployee(3L, VALID_ID);
        verify(userMapper).toUserResponseDto(updated, company);
    }

    @Test
    void patchUser_ShouldReturnPatchedUser_WhenValid() {
        PatchUserDto dto = new PatchUserDto("Jane", null, null, null);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserEntity patched = new UserEntity(VALID_ID, "Jane", "Doe", PHONE, COMPANY_ID);
        CompanyInfoDto company = new CompanyInfoDto(COMPANY_ID, "Test Company", new BigDecimal("1000.00"));
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("Jane").lastName("Doe").phoneNumber(PHONE).company(company).build();

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(companyClient.getCompanyById(COMPANY_ID)).thenReturn(company);
        doNothing().when(userMapper).patchUserFromDto(dto, existing);
        when(userRepository.save(existing)).thenReturn(patched);
        when(userMapper.toUserResponseDto(patched, company)).thenReturn(response);

        UserResponseDto result = userService.patchUser(VALID_ID, dto);

        assertEquals(response, result);
        verify(userRepository).findById(VALID_ID);
        verify(userMapper).patchUserFromDto(dto, existing);
        verify(userRepository).save(existing);
        verify(companyClient).getCompanyById(COMPANY_ID);
        verify(userMapper).toUserResponseDto(patched, company);
        verifyNoMoreInteractions(companyClient);
    }

    @Test
    void patchUser_ShouldThrowNotFound_WhenUserNotFound() {
        PatchUserDto dto = new PatchUserDto("Jane", null, null, null);

        when(userRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.patchUser(INVALID_ID, dto));
        verify(userRepository).findById(INVALID_ID);
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void patchUser_ShouldThrowAlreadyExists_WhenPhoneChangedAndExists() {
        PatchUserDto dto = new PatchUserDto(null, null, "+79876543210", null);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(userRepository.existsByPhoneNumber("+79876543210")).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.patchUser(VALID_ID, dto));
        verify(userRepository).findById(VALID_ID);
        verify(userRepository).existsByPhoneNumber("+79876543210");
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void patchUser_ShouldThrowNotFound_WhenCompanyNotFound() {
        PatchUserDto dto = new PatchUserDto(null, null, null, COMPANY_ID);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, 3L);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(companyClient.getCompanyById(COMPANY_ID)).thenThrow(FeignException.NotFound.class);

        assertThrows(NotFoundException.class, () -> userService.patchUser(VALID_ID, dto));
        verify(userRepository).findById(VALID_ID);
        verify(companyClient).getCompanyById(COMPANY_ID);
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void patchUser_ShouldSyncCompanyChange_WhenCompanyIdChanged() {
        PatchUserDto dto = new PatchUserDto(null, null, null, 3L);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserEntity patched = new UserEntity(VALID_ID, "John", "Doe", PHONE, 3L);
        CompanyInfoDto company = new CompanyInfoDto(3L, "New Company", new BigDecimal("2000.00"));
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("John").lastName("Doe").phoneNumber(PHONE).company(company).build();

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(companyClient.getCompanyById(3L)).thenReturn(company);
        doNothing().when(userMapper).patchUserFromDto(dto, existing);
        when(userRepository.save(existing)).thenReturn(patched);
        when(userMapper.toUserResponseDto(patched, company)).thenReturn(response);

        UserResponseDto result = userService.patchUser(VALID_ID, dto);

        assertEquals(response, result);
        verify(userRepository).findById(VALID_ID);
        verify(companyClient, times(2)).getCompanyById(3L);
        verify(userMapper).patchUserFromDto(dto, existing);
        verify(userRepository).save(existing);
        verify(companyClient).removeEmployee(COMPANY_ID, VALID_ID);
        verify(companyClient).addEmployee(3L, VALID_ID);
        verify(userMapper).toUserResponseDto(patched, company);
    }

    @Test
    void patchUser_ShouldHandleAllNullFields() {
        PatchUserDto dto = new PatchUserDto(null, null, null, null);
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        CompanyInfoDto company = new CompanyInfoDto(COMPANY_ID, "Test Company", new BigDecimal("1000.00"));
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("John").lastName("Doe").phoneNumber(PHONE).company(company).build();

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        when(companyClient.getCompanyById(COMPANY_ID)).thenReturn(company);
        doNothing().when(userMapper).patchUserFromDto(dto, existing);
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toUserResponseDto(existing, company)).thenReturn(response);

        UserResponseDto result = userService.patchUser(VALID_ID, dto);

        assertEquals(response, result);
        verify(userRepository).findById(VALID_ID);
        verify(userMapper).patchUserFromDto(dto, existing);
        verify(userRepository).save(existing);
        verify(companyClient).getCompanyById(COMPANY_ID);
        verify(userMapper).toUserResponseDto(existing, company);
        verifyNoMoreInteractions(companyClient);
    }

    @Test
    void deleteUser_ShouldDelete_WhenUserExists() {
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        doNothing().when(userRepository).deleteById(VALID_ID);

        userService.deleteUser(VALID_ID);

        verify(userRepository).findById(VALID_ID);
        verify(userRepository).deleteById(VALID_ID);
        verify(companyClient).removeEmployee(COMPANY_ID, VALID_ID);
    }

    @Test
    void deleteUser_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(INVALID_ID));
        verify(userRepository).findById(INVALID_ID);
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void deleteUser_ShouldHandleNullCompanyId() {
        UserEntity existing = new UserEntity(VALID_ID, "John", "Doe", PHONE, null);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(existing));
        doNothing().when(userRepository).deleteById(VALID_ID);

        userService.deleteUser(VALID_ID);

        verify(userRepository).findById(VALID_ID);
        verify(userRepository).deleteById(VALID_ID);
        verifyNoInteractions(companyClient);
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        CompanyInfoDto company = new CompanyInfoDto(COMPANY_ID, "Test Company", new BigDecimal("1000.00"));
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("John").lastName("Doe").phoneNumber(PHONE).company(company).build();

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(entity));
        when(companyClient.getCompanyById(COMPANY_ID)).thenReturn(company);
        when(userMapper.toUserResponseDto(entity, company)).thenReturn(response);

        UserResponseDto result = userService.getUserById(VALID_ID);

        assertEquals(response, result);
        verify(userRepository).findById(VALID_ID);
        verify(companyClient).getCompanyById(COMPANY_ID);
        verify(userMapper).toUserResponseDto(entity, company);
    }

    @Test
    void getUserById_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(INVALID_ID));
        verify(userRepository).findById(INVALID_ID);
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void getUserById_ShouldHandleCompanyNotFound() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("John").lastName("Doe").phoneNumber(PHONE).company(null).build();

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(entity));
        when(companyClient.getCompanyById(COMPANY_ID)).thenThrow(FeignException.NotFound.class);
        when(userMapper.toUserResponseDto(entity, null)).thenReturn(response);

        UserResponseDto result = userService.getUserById(VALID_ID);

        assertEquals(response, result);
        verify(userRepository).findById(VALID_ID);
        verify(companyClient).getCompanyById(COMPANY_ID);
        verify(userMapper).toUserResponseDto(entity, null);
    }

    @Test
    void getAllUsers_ShouldReturnPagedUsers_WhenValid() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        CompanyInfoDto company = new CompanyInfoDto(COMPANY_ID, "Test Company", new BigDecimal("1000.00"));
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("John").lastName("Doe").phoneNumber(PHONE).company(company).build();
        Page<UserEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(companyClient.getCompanyById(COMPANY_ID)).thenReturn(company);
        when(userMapper.toUserResponseDto(entity, company)).thenReturn(response);

        PagedUserResponseDto result = userService.getAllUsers(0, 10);

        assertEquals(1, result.getUsers().size());
        assertEquals(response, result.getUsers().get(0));
        assertEquals(0, result.getPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Pageable.class));
        verify(companyClient).getCompanyById(COMPANY_ID);
        verify(userMapper).toUserResponseDto(entity, company);
    }

    @Test
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsers() {
        Page<UserEntity> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedUserResponseDto result = userService.getAllUsers(0, 10);

        assertTrue(result.getUsers().isEmpty());
        assertEquals(0, result.getPage());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getTotalElements());
        verify(userRepository).findAll(any(Pageable.class));
        verifyNoInteractions(userMapper, companyClient);
    }

    @Test
    void getAllUsers_ShouldHandleCompanyNotFound() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserResponseDto response = UserResponseDto.builder()
                .id(VALID_ID).firstName("John").lastName("Doe").phoneNumber(PHONE).company(null).build();
        Page<UserEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(companyClient.getCompanyById(COMPANY_ID)).thenThrow(FeignException.NotFound.class);
        when(userMapper.toUserResponseDto(entity, null)).thenReturn(response);

        PagedUserResponseDto result = userService.getAllUsers(0, 10);

        assertEquals(1, result.getUsers().size());
        assertEquals(response, result.getUsers().get(0));
        verify(userRepository).findAll(any(Pageable.class));
        verify(companyClient).getCompanyById(COMPANY_ID);
        verify(userMapper).toUserResponseDto(entity, null);
    }

    @Test
    void getUsersByIds_ShouldReturnUsers_WhenIdsValid() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserInfoDto response = new UserInfoDto(VALID_ID, "John", "Doe", PHONE);

        when(userRepository.findAllById(List.of(VALID_ID))).thenReturn(List.of(entity));
        when(userMapper.toUserInfoDto(entity)).thenReturn(response);

        List<UserInfoDto> result = userService.getUsersByIds(List.of(VALID_ID));

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
        verify(userRepository).findAllById(List.of(VALID_ID));
        verify(userMapper).toUserInfoDto(entity);
    }

    @Test
    void getUsersByIds_ShouldReturnEmptyList_WhenIdsEmpty() {
        List<UserInfoDto> result = userService.getUsersByIds(Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(userRepository).findAllById(Collections.emptyList());
        verifyNoInteractions(userMapper, companyClient);
    }

    @Test
    void getUsersByIds_ShouldReturnPartialList_WhenSomeIdsNotFound() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserInfoDto response = new UserInfoDto(VALID_ID, "John", "Doe", PHONE);

        when(userRepository.findAllById(List.of(VALID_ID, INVALID_ID))).thenReturn(List.of(entity));
        when(userMapper.toUserInfoDto(entity)).thenReturn(response);

        List<UserInfoDto> result = userService.getUsersByIds(List.of(VALID_ID, INVALID_ID));

        assertEquals(1, result.size());
        assertEquals(response, result.get(0));
        verify(userRepository).findAllById(List.of(VALID_ID, INVALID_ID));
        verify(userMapper).toUserInfoDto(entity);
    }

    @Test
    void deleteUsersByCompanyId_ShouldDelete_WhenUsersExist() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);

        when(userRepository.findAllByCompanyId(COMPANY_ID)).thenReturn(List.of(entity));
        doNothing().when(userRepository).deleteAll(List.of(entity));

        userService.deleteUsersByCompanyId(COMPANY_ID);

        verify(userRepository).findAllByCompanyId(COMPANY_ID);
        verify(userRepository).deleteAll(List.of(entity));
        verifyNoInteractions(companyClient);
    }

    @Test
    void deleteUsersByCompanyId_ShouldDoNothing_WhenNoUsers() {
        when(userRepository.findAllByCompanyId(COMPANY_ID)).thenReturn(Collections.emptyList());

        userService.deleteUsersByCompanyId(COMPANY_ID);

        verify(userRepository).findAllByCompanyId(COMPANY_ID);
        verify(userRepository).deleteAll(Collections.emptyList());
        verifyNoInteractions(companyClient);
    }

    @Test
    void updateUserCompany_ShouldUpdate_WhenValid() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserEntity updated = new UserEntity(VALID_ID, "John", "Doe", PHONE, 3L);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(updated);

        userService.updateUserCompany(VALID_ID, 3L);

        verify(userRepository).findById(VALID_ID);
        verify(userRepository).save(entity);
        verifyNoInteractions(companyClient);
    }

    @Test
    void updateUserCompany_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUserCompany(INVALID_ID, COMPANY_ID));
        verify(userRepository).findById(INVALID_ID);
        verifyNoMoreInteractions(userRepository, userMapper, companyClient);
    }

    @Test
    void updateUserCompany_ShouldHandleNullCompanyId() {
        UserEntity entity = new UserEntity(VALID_ID, "John", "Doe", PHONE, COMPANY_ID);
        UserEntity updated = new UserEntity(VALID_ID, "John", "Doe", PHONE, null);

        when(userRepository.findById(VALID_ID)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(updated);

        userService.updateUserCompany(VALID_ID, null);

        verify(userRepository).findById(VALID_ID);
        verify(userRepository).save(entity);
        verifyNoInteractions(companyClient);
    }
}