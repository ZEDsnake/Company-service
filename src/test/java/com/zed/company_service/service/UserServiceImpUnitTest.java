package com.zed.company_service.service;

import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.entity.User;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.mapper.UserMapper;
import com.zed.company_service.repository.UserRepository;
import com.zed.company_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImpUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private static final Long USER_ID = 1L;
    private static final String VALID_PHONE = "+79123456789";
    private static final String NEW_PHONE = "+79234567890";

    private CreateUserDTO createUserDTO() {
        return new CreateUserDTO("Ivan", "Petrov", VALID_PHONE);
    }

    private UpdateUserDTO updateUserDTO() {
        return new UpdateUserDTO("Sergey", "Sidorov", NEW_PHONE);
    }

    private User userEntity() {
        User user = new User();
        user.setId(USER_ID);
        user.setFirstName("Ivan");
        user.setLastName("Petrov");
        user.setPhoneNumber(VALID_PHONE);
        return user;
    }

    private UserDTO userDTO() {
        return new UserDTO(USER_ID, "Ivan", "Petrov", VALID_PHONE);
    }

    @Test
    void createUser_Success() {
        CreateUserDTO request = createUserDTO();
        User user = userEntity();
        User savedUser = userEntity();
        UserDTO expectedDTO = userDTO();

        when(userRepository.existsByPhoneNumber(VALID_PHONE)).thenReturn(false);
        when(userMapper.toUserEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserDTO(savedUser)).thenReturn(expectedDTO);

        UserDTO result = userService.createUser(request);

        verify(userRepository).existsByPhoneNumber(VALID_PHONE);
        verify(userMapper).toUserEntity(request);
        verify(userRepository).save(user);
        assertEquals(expectedDTO, result);
    }

    @Test
    void createUser_PhoneExists_ThrowsException() {
        when(userRepository.existsByPhoneNumber(VALID_PHONE)).thenReturn(true);
        assertThrows(AlreadyExistsException.class, () -> userService.createUser(createUserDTO()));
    }

    @Test
    void getUserById_Success() {
        User user = userEntity();
        UserDTO expectedDTO = userDTO();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(expectedDTO);

        UserDTO result = userService.getUserById(USER_ID);

        assertEquals(expectedDTO, result);
        verify(userRepository).findById(USER_ID);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserById(USER_ID));
    }

    @Test
    void updateUser_Success_NoPhoneChange() {
        User existingUser = userEntity();
        UpdateUserDTO request = updateUserDTO();
        request.setPhoneNumber(VALID_PHONE);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toUserDTO(existingUser)).thenReturn(userDTO());

        UserDTO result = userService.updateUser(USER_ID, request);

        verify(userRepository, never()).existsByPhoneNumber(any());
        verify(userMapper).updateEntityFromDTO(request, existingUser);
        assertNotNull(result);
    }

    @Test
    void updateUser_PhoneChanged_ChecksUniqueness() {
        User existingUser = userEntity();
        UpdateUserDTO request = updateUserDTO(); // С новым телефоном

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByPhoneNumber(NEW_PHONE)).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toUserDTO(existingUser)).thenReturn(userDTO());

        UserDTO result = userService.updateUser(USER_ID, request);

        verify(userRepository).existsByPhoneNumber(NEW_PHONE);
        assertNotNull(result);
    }

    @Test
    void updateUser_NewPhoneExists_ThrowsException() {
        User existingUser = userEntity();
        UpdateUserDTO request = updateUserDTO();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByPhoneNumber(NEW_PHONE)).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> userService.updateUser(USER_ID, request));
    }

    @Test
    void deleteUser_Success() {
        User user = userEntity();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(USER_ID);

        verify(userRepository).delete(user);
    }

    @Test
    void getAllUsers_Pagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(userEntity()));

        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toUserDTOList(anyList())).thenReturn(List.of(userDTO()));

        List<UserDTO> result = userService.getAllUsers(0, 10);

        assertEquals(1, result.size());
        verify(userRepository).findAll(pageable);
    }
}
