package com.zed.company_service.service;

import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.EmployeeDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.entity.User;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private CompanyEntity company;
    private User existingUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        companyRepository.deleteAll();

        company = new CompanyEntity();
        company.setName("Test Company");
        company.setBudget(new BigDecimal("100000.00"));
        company = companyRepository.save(company);

        existingUser = new User();
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setPhoneNumber("+79123456783");
        existingUser.setCompany(company);
        existingUser = userRepository.save(existingUser);

        company.getEmployees().add(existingUser);
        companyRepository.save(company);
    }

    private String generateUniquePhoneNumber() {
        return "+7" + (9100000000L + new Random().nextInt(1000000));
    }

    @Test
    void createUser_ShouldSuccessfullyCreateUser() {
        CreateUserDTO dto = new CreateUserDTO(
                "Alice",
                "Brown",
                generateUniquePhoneNumber(),
                company.getId()
        );

        UserDTO result = userService.createUser(dto);

        assertNotNull(result.getId());
        User savedUser = userRepository.findById(result.getId()).orElseThrow();
        assertEquals("Alice", savedUser.getFirstName());
        assertEquals(company.getId(), savedUser.getCompany().getId());
    }

    @Test
    void createUser_ShouldThrowWhenPhoneExists() {
        CreateUserDTO dto = new CreateUserDTO(
                "John",
                "Doe",
                existingUser.getPhoneNumber(),
                company.getId()
        );

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(dto));
    }

    @Test
    void getUserById_ShouldReturnCorrectUser() {
        UserDTO result = userService.getUserById(existingUser.getId());

        assertEquals(existingUser.getId(), result.getId());
        assertEquals(existingUser.getFirstName(), result.getFirstName());
    }

    @Test
    void getUserWithCompany_ShouldReturnFullInfo() {
        EmployeeDTO result = userService.getUserWithCompany(existingUser.getId());

        assertEquals(existingUser.getId(), result.getId());
        assertEquals(company.getName(), result.getCompany().getName());
    }

    @Test
    void updateUser_ShouldUpdateAllFields() {
        CompanyEntity newCompany = new CompanyEntity();
        newCompany.setName("New Company");
        newCompany.setBudget(new BigDecimal("200000.00"));
        newCompany = companyRepository.save(newCompany);

        UpdateUserDTO dto = new UpdateUserDTO(
                "Updated",
                "Name",
                generateUniquePhoneNumber(),
                newCompany.getId()
        );

        UserDTO result = userService.updateUser(existingUser.getId(), dto);

        assertEquals("Updated", result.getFirstName());
        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertEquals(newCompany.getId(), updatedUser.getCompany().getId());
    }

    @Test
    void updateUser_ShouldThrowWhenPhoneExists() {
        User anotherUser = new User();
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setPhoneNumber(generateUniquePhoneNumber());
        anotherUser.setCompany(company);
        anotherUser = userRepository.save(anotherUser);

        UpdateUserDTO dto = new UpdateUserDTO(
                existingUser.getFirstName(),
                existingUser.getLastName(),
                anotherUser.getPhoneNumber(), // Используем существующий телефон
                company.getId()
        );

        assertThrows(AlreadyExistsException.class,
                () -> userService.updateUser(existingUser.getId(), dto));
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() {
        Long userId = existingUser.getId();
        Long companyId = company.getId();

        userService.deleteUser(userId);

        assertFalse(userRepository.findById(userId).isPresent());
        assertTrue(companyRepository.findById(companyId).isPresent());
    }

    @Test
    void getAllUsers_ShouldReturnPaginatedResults() {
        // Создаем дополнительных пользователей
        User user1 = new User();
        user1.setFirstName("User1");
        user1.setLastName("Test");
        user1.setPhoneNumber(generateUniquePhoneNumber());
        user1.setCompany(company);
        userRepository.save(user1);

        User user2 = new User();
        user2.setFirstName("User2");
        user2.setLastName("Test");
        user2.setPhoneNumber(generateUniquePhoneNumber());
        user2.setCompany(company);
        userRepository.save(user2);

        // Проверяем пагинацию
        List<UserDTO> page1 = userService.getAllUsers(0, 2);
        List<UserDTO> page2 = userService.getAllUsers(1, 2);

        assertEquals(2, page1.size());
        assertEquals(1, page2.size());
    }

    @Test
    void getUserById_ShouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    void updateUser_ShouldThrowWhenUserNotFound() {
        UpdateUserDTO dto = new UpdateUserDTO(
                "Test",
                "Test",
                generateUniquePhoneNumber(),
                company.getId()
        );

        assertThrows(NotFoundException.class,
                () -> userService.updateUser(999L, dto));
    }
}
