package com.zed.user_service.service;

import com.zed.user_service.dto.CompanyInfoDTO;
import com.zed.user_service.dto.CreateUserDTO;
import com.zed.user_service.dto.EmployeeDTO;
import com.zed.user_service.dto.UpdateUserDTO;
import com.zed.user_service.dto.UserDTO;
import com.zed.user_service.dto.UserInfoDTO;
import com.zed.user_service.entity.User;
import com.zed.user_service.exception.AlreadyExistsException;
import com.zed.user_service.exception.NotFoundException;
import com.zed.user_service.feign.CompanyClient;
import com.zed.user_service.mapper.UserMapper;
import com.zed.user_service.repository.UserRepository;
import com.zed.user_service.service.impl.UserServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CompanyClient companyClient;

    private User user;
    private CreateUserDTO createUserDTO;
    private UpdateUserDTO updateUserDTO;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John", "Doe", "+71234567890", 1L);
        createUserDTO = new CreateUserDTO("John", "Doe", "+71234567890", 1L);
        updateUserDTO = new UpdateUserDTO("Jane", "Doe", "+79876543210", 2L);
    }

    @Test
    void createUser_ShouldSaveUserSuccessfully() {
        when(userRepository.existsByPhoneNumber(createUserDTO.getPhoneNumber())).thenReturn(false);
        when(companyClient.getCompanyById(createUserDTO.getCompanyId())).thenReturn(new CompanyInfoDTO());
        when(userMapper.toUserEntity(createUserDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(new UserDTO(1L, "John", "Doe", "+71234567890"));

        UserDTO result = userService.createUser(createUserDTO);

        assertEquals("John", result.getFirstName());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_ShouldThrowAlreadyExists_WhenPhoneExists() {
        when(userRepository.existsByPhoneNumber(createUserDTO.getPhoneNumber())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(createUserDTO));
    }

    @Test
    void createUser_ShouldThrowNotFound_WhenCompanyNotFound() {
        when(userRepository.existsByPhoneNumber(createUserDTO.getPhoneNumber())).thenReturn(false);
        when(companyClient.getCompanyById(createUserDTO.getCompanyId())).thenThrow(FeignException.NotFound.class);

        assertThrows(NotFoundException.class, () -> userService.createUser(createUserDTO));
    }

    @Test
    void updateUser_ShouldUpdateSuccessfully_WhenPhoneChanged() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber(updateUserDTO.getPhoneNumber())).thenReturn(false);
        when(companyClient.getCompanyById(updateUserDTO.getCompanyId())).thenReturn(new CompanyInfoDTO());
        doAnswer(inv -> {
            User u = inv.getArgument(1);
            u.setFirstName(updateUserDTO.getFirstName());
            u.setPhoneNumber(updateUserDTO.getPhoneNumber());
            return null;
        }).when(userMapper).updateEntityFromDTO(eq(updateUserDTO), any(User.class));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(new UserDTO(1L, "Jane", "Doe", "+79876543210"));

        UserDTO result = userService.updateUser(1L, updateUserDTO);

        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void updateUser_ShouldThrowNotFound_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(1L, updateUserDTO));
    }

    @Test
    void updateUser_ShouldThrowAlreadyExists_WhenPhoneChangedAndExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber(updateUserDTO.getPhoneNumber())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, updateUserDTO));
    }

    @Test
    void updateUser_ShouldNotCheckPhone_WhenPhoneNotChanged() {
        updateUserDTO.setPhoneNumber(user.getPhoneNumber());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(companyClient.getCompanyById(updateUserDTO.getCompanyId())).thenReturn(new CompanyInfoDTO());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(new UserDTO());

        assertDoesNotThrow(() -> userService.updateUser(1L, updateUserDTO));
    }

    @Test
    void deleteUser_ShouldDelete_WhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrowNotFound_WhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void getUserWithCompany_ShouldReturnEmployeeDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toEmployeeDTO(user)).thenReturn(new EmployeeDTO());
        when(companyClient.getCompanyById(user.getCompanyId())).thenReturn(new CompanyInfoDTO());

        EmployeeDTO result = userService.getUserWithCompany(1L);

        assertNotNull(result);
    }

    @Test
    void getAllUsers_ShouldReturnUserDTOList() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(userMapper.toUserDTOList(page.getContent())).thenReturn(List.of(new UserDTO()));

        List<UserDTO> result = userService.getAllUsers(0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void getUserInfoByCompanyId_ShouldReturnList() {
        when(userRepository.findByCompanyId(1L, PageRequest.of(0, 10))).thenReturn(List.of(user));
        when(userMapper.toUserInfoDtoList(List.of(user))).thenReturn(List.of(new UserInfoDTO()));

        List<UserInfoDTO> result = userService.getUserInfoByCompanyId(1L, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void deleteUsersByCompanyId_ShouldDeleteUsers() {
        doNothing().when(userRepository).deleteByCompanyId(1L);

        userService.deleteUsersByCompanyId(1L);

        verify(userRepository).deleteByCompanyId(1L);
    }
}

