package com.zed.company_service.service;

import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.entity.User;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private String generateUniquePhoneNumber() {
        return "+7" + (9100000000L + new Random().nextInt(1000000));
    }

    @Test
    void createUser_ShouldSaveUserAndReturnDtoWithGeneratedId() {
        String uniquePhone = generateUniquePhoneNumber();
        CreateUserDTO dto = new CreateUserDTO("Alice", "Brown", uniquePhone);

        UserDTO result = userService.createUser(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getPhoneNumber()).isEqualTo(uniquePhone);

        User savedUser = userRepository.findById(result.getId()).orElseThrow();
        assertThat(savedUser.getFirstName()).isEqualTo("Alice");
        assertThat(savedUser.getLastName()).isEqualTo("Brown");
    }

    @Test
    void createUser_ShouldThrowAlreadyExistsException_WhenPhoneExists() {
        String existingPhone = generateUniquePhoneNumber();
        userRepository.save(new User(null, "Existing", "User", existingPhone));
        CreateUserDTO dto = new CreateUserDTO("John", "Doe", existingPhone);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        String uniquePhone = generateUniquePhoneNumber();
        User savedUser = userRepository.save(new User(null, "Test", "User", uniquePhone));

        UserDTO result = userService.getUserById(savedUser.getId());

        assertThat(result.getId()).isEqualTo(savedUser.getId());
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
    }

    @Test
    void getUserById_ShouldThrowNotFoundException_WhenUserNotExists() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateUser_ShouldUpdateFields_WhenDataIsValid() {
        String originalPhone = generateUniquePhoneNumber();
        User savedUser = userRepository.save(new User(null, "Original", "Name", originalPhone));
        UpdateUserDTO dto = new UpdateUserDTO("Updated", "Name", originalPhone);

        UserDTO result = userService.updateUser(savedUser.getId(), dto);

        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Name");

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
    }

    @Test
    void updateUser_ShouldThrowAlreadyExistsException_WhenPhoneTaken() {
        String existingPhone = generateUniquePhoneNumber();
        userRepository.save(new User(null, "Existing", "User", existingPhone));
        User userToUpdate = userRepository.save(new User(null, "Original", "User", generateUniquePhoneNumber()));
        UpdateUserDTO dto = new UpdateUserDTO("John", "Doe", existingPhone);

        assertThatThrownBy(() -> userService.updateUser(userToUpdate.getId(), dto))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void deleteUser_ShouldRemoveUser_WhenUserExists() {
        User savedUser = userRepository.save(new User(null, "ToDelete", "User", generateUniquePhoneNumber()));

        userService.deleteUser(savedUser.getId());

        assertThat(userRepository.existsById(savedUser.getId())).isFalse();
    }

    @Test
    void deleteUser_ShouldThrowNotFoundException_WhenUserNotExists() {
        Long nonExistentId = 999L;

        assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllUsers_ShouldReturnPaginatedResults() {
        userRepository.deleteAll();

        userRepository.save(new User(null, "User1", "Last1", generateUniquePhoneNumber()));
        userRepository.save(new User(null, "User2", "Last2", generateUniquePhoneNumber()));
        userRepository.save(new User(null, "User3", "Last3", generateUniquePhoneNumber()));

        List<UserDTO> page1 = userService.getAllUsers(0, 2);
        List<UserDTO> page2 = userService.getAllUsers(1, 2);

        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(1);
    }
}
