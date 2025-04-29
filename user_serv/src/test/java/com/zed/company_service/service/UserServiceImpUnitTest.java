//package com.zed.company_service.service;
//
//import com.zed.company_service.dto.CreateUserDTO;
//import com.zed.company_service.dto.UpdateUserDTO;
//import com.zed.company_service.dto.UserDTO;
//import com.zed.company_service.entity.CompanyEntity;
//import com.zed.company_service.entity.User;
//import com.zed.company_service.exception.AlreadyExistsException;
//import com.zed.company_service.exception.NotFoundException;
//import com.zed.company_service.mapper.UserMapper;
//import com.zed.company_service.repository.CompanyRepository;
//import com.zed.company_service.repository.UserRepository;
//import com.zed.company_service.service.impl.UserServiceImpl;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class UserServiceImpUnitTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @Mock
//    private CompanyRepository companyRepository;
//
//    @InjectMocks
//    private UserServiceImpl userService;
//
//    private static final Long USER_ID = 1L;
//    private static final Long COMPANY_ID = 1L;
//    private static final String VALID_PHONE = "+79123456789";
//    private static final String NEW_PHONE = "+79234567890";
//
//    // Test data builders
//    private CreateUserDTO createUserDTO() {
//        return new CreateUserDTO("Ivan", "Petrov", VALID_PHONE, COMPANY_ID);
//    }
//
//    private UpdateUserDTO updateUserDTO() {
//        return new UpdateUserDTO("Sergey", "Sidorov", NEW_PHONE, COMPANY_ID);
//    }
//
//    private User userEntity() {
//        User user = new User();
//        user.setId(USER_ID);
//        user.setFirstName("Ivan");
//        user.setLastName("Petrov");
//        user.setPhoneNumber(VALID_PHONE);
//
//        CompanyEntity company = new CompanyEntity();
//        company.setId(COMPANY_ID);
//        company.setName("Test Company");
//        company.setBudget(BigDecimal.valueOf(100000));
//        company.setEmployees(new ArrayList<>());
//
//        user.setCompany(company);
//        return user;
//    }
//
//    private UserDTO userDTO() {
//        return new UserDTO(USER_ID, "Ivan", "Petrov", VALID_PHONE);
//    }
//
//    private EmployeeDTO employeeDTO() {
//        return new EmployeeDTO(USER_ID, "Ivan", "Petrov", VALID_PHONE,
//                new CompanyInfoDTO("Test Company"));
//    }
//
//    // Tests
//    @Test
//    void createUser_ShouldSuccessfullyCreateUser() {
//        // Arrange
//        CreateUserDTO request = createUserDTO();
//        User user = userEntity();
//        User savedUser = userEntity();
//        UserDTO expectedDTO = userDTO();
//        CompanyEntity company = userEntity().getCompany();
//
//        when(userRepository.existsByPhoneNumber(VALID_PHONE)).thenReturn(false);
//        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company));
//        when(userMapper.toUserEntity(request)).thenReturn(user);
//        when(userRepository.save(user)).thenReturn(savedUser);
//        when(userMapper.toUserDTO(savedUser)).thenReturn(expectedDTO);
//
//        // Act
//        UserDTO result = userService.createUser(request);
//
//        // Assert
//        verify(userRepository).existsByPhoneNumber(VALID_PHONE);
//        verify(companyRepository).findById(COMPANY_ID);
//        verify(userMapper).toUserEntity(request);
//        verify(userRepository).save(user);
//        assertEquals(expectedDTO, result);
//    }
//
//    @Test
//    void createUser_ShouldThrowWhenPhoneExists() {
//        when(userRepository.existsByPhoneNumber(VALID_PHONE)).thenReturn(true);
//
//        assertThrows(AlreadyExistsException.class,
//                () -> userService.createUser(createUserDTO()));
//    }
//
//    @Test
//    void createUser_ShouldThrowWhenCompanyNotFound() {
//        when(userRepository.existsByPhoneNumber(VALID_PHONE)).thenReturn(false);
//        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.empty());
//
//        assertThrows(NotFoundException.class,
//                () -> userService.createUser(createUserDTO()));
//    }
//
//    @Test
//    void getUserById_ShouldReturnUserWhenExists() {
//        User user = userEntity();
//        UserDTO expectedDTO = userDTO();
//
//        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
//        when(userMapper.toUserDTO(user)).thenReturn(expectedDTO);
//
//        UserDTO result = userService.getUserById(USER_ID);
//
//        assertEquals(expectedDTO, result);
//        verify(userRepository).findById(USER_ID);
//    }
//
////    @Test
////    void getUserWithCompany_ShouldReturnFullInfo() {
////        User user = userEntity();
////        EmployeeDTO expectedDTO = employeeDTO();
////
////        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
////        when(userMapper.toEmployeeDTO(user)).thenReturn(expectedDTO);
////
////        EmployeeDTO result = userService.getUserWithCompany(USER_ID);
////
////        assertEquals(expectedDTO, result);
////        verify(userRepository).findById(USER_ID);
////    }
//
//    @Test
//    void updateUser_ShouldUpdateFieldsWithoutPhoneChange() {
//        User existingUser = userEntity();
//        UpdateUserDTO request = updateUserDTO();
//        request.setPhoneNumber(VALID_PHONE); // Телефон не меняется
//        UserDTO expectedDTO = userDTO();
//
//        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
//        when(userRepository.save(existingUser)).thenReturn(existingUser);
//        when(userMapper.toUserDTO(existingUser)).thenReturn(expectedDTO);
//
//        UserDTO result = userService.updateUser(USER_ID, request);
//
//        verify(userRepository, never()).existsByPhoneNumber(any());
//        verify(userMapper).updateEntityFromDTO(request, existingUser);
//        assertEquals(expectedDTO, result);
//    }
//
//    @Test
//    void updateUser_ShouldCheckPhoneUniquenessWhenChanged() {
//        User existingUser = userEntity();
//        UpdateUserDTO request = updateUserDTO(); // Новый телефон
//        UserDTO expectedDTO = userDTO();
//
//        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
//        when(userRepository.existsByPhoneNumber(NEW_PHONE)).thenReturn(false);
//        when(userRepository.save(existingUser)).thenReturn(existingUser);
//        when(userMapper.toUserDTO(existingUser)).thenReturn(expectedDTO);
//
//        UserDTO result = userService.updateUser(USER_ID, request);
//
//        verify(userRepository).existsByPhoneNumber(NEW_PHONE);
//        assertEquals(expectedDTO, result);
//    }
//
//    @Test
//    void updateUser_ShouldUpdateCompanyWhenChanged() {
//        Long newCompanyId = 2L;
//
//        CompanyEntity newCompany = new CompanyEntity();
//        newCompany.setId(newCompanyId);
//        newCompany.setName("New Company");
//        newCompany.setBudget(BigDecimal.valueOf(200000));
//        newCompany.setEmployees(new ArrayList<>());
//
//        User existingUser = userEntity();
//        UpdateUserDTO request = updateUserDTO();
//        request.setCompanyId(newCompanyId);
//
//        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
//        when(userRepository.existsByPhoneNumber(NEW_PHONE)).thenReturn(false);
//        when(companyRepository.findById(newCompanyId)).thenReturn(Optional.of(newCompany));
//        when(userRepository.save(existingUser)).thenReturn(existingUser);
//        when(userMapper.toUserDTO(existingUser)).thenReturn(userDTO());
//
//        userService.updateUser(USER_ID, request);
//
//        verify(companyRepository).findById(newCompanyId);
//        assertEquals(newCompanyId, existingUser.getCompany().getId());
//        assertEquals("New Company", existingUser.getCompany().getName());
//    }
//
//    @Test
//    void deleteUser_ShouldDeleteExistingUser() {
//        User user = userEntity();
//        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
//        doNothing().when(userRepository).delete(user);
//
//        userService.deleteUser(USER_ID);
//
//        verify(userRepository).delete(user);
//    }
//
////    @Test
////    void getAllUsers_ShouldReturnPaginatedResults() {
////        Pageable pageable = PageRequest.of(0, 10);
////        Page<User> page = new PageImpl<>(List.of(userEntity()));
////
////        when(userRepository.findAll(pageable)).thenReturn(page);
////        when(userMapper.toUserDTOList(anyList())).thenReturn(List.of(userDTO()));
////
////        List<UserDTO> result = userService.getAllUsers(0, 10);
////
////        assertEquals(1, result.size());
////        verify(userRepository).findAll(pageable);
////    }
//
//    @Test
//    void getUserById_ShouldThrowWhenUserNotFound() {
//        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
//
//        assertThrows(NotFoundException.class,
//                () -> userService.getUserById(USER_ID));
//    }
//
//    @Test
//    void updateUser_ShouldThrowWhenUserNotFound() {
//        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
//
//        assertThrows(NotFoundException.class,
//                () -> userService.updateUser(USER_ID, updateUserDTO()));
//    }
//
//    @Test
//    void deleteUser_ShouldThrowWhenUserNotFound() {
//        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
//
//        assertThrows(NotFoundException.class,
//                () -> userService.deleteUser(USER_ID));
//    }
//}
