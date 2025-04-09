package com.zed.company_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserDTO createUserDTO;
    private UpdateUserDTO updateUserDTO;
    private UserDTO userDTO;

    @BeforeEach
    void setup() {
        createUserDTO = new CreateUserDTO("Ivan", "Ivanov", "+79123456789");
        updateUserDTO = new UpdateUserDTO("Petr", "Petrov", "+79234567890");
        userDTO = new UserDTO(1L, "Ivan", "Ivanov", "+79123456789");
    }

    // ------------------------- Create User Tests -------------------------
    @Test
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.firstName").value(userDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDTO.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(userDTO.getPhoneNumber()));
    }

    @Test
    void createUser_FirstNameBlank_ReturnsBadRequest() throws Exception {
        createUserDTO.setFirstName("   ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name is required"));
    }

    @Test
    void createUser_InvalidFirstName_ReturnsBadRequest() throws Exception {
        createUserDTO.setFirstName("A");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_FirstNameMinLength_ReturnsCreated() throws Exception {
        createUserDTO.setFirstName("Al");
        userDTO.setFirstName("Al");

        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_FirstNameMaxLength_ReturnsCreated() throws Exception {
        createUserDTO.setFirstName("A".repeat(50));
        userDTO.setFirstName("A".repeat(50));

        when(userService.createUser(any())).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_FirstNameMidLength_ReturnsCreated() throws Exception {
        createUserDTO.setFirstName("A".repeat(20));
        userDTO.setFirstName("A".repeat(20));

        when(userService.createUser(any())).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_FirstNameTooLong_ReturnsBadRequest() throws Exception {
        createUserDTO.setFirstName("A".repeat(51));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_LastNameBlank_ReturnsBadRequest() throws Exception {
        createUserDTO.setLastName("   ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Last name is required"));
    }

    @Test
    void createUser_InvalidLastName_ReturnsBadRequest() throws Exception {
        createUserDTO.setLastName("I");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Lastname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_LastNameMinLength_ReturnsCreated() throws Exception {
        createUserDTO.setLastName("Iv");
        userDTO.setLastName("Iv");

        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_LastNameMaxLength_ReturnsCreated() throws Exception {
        createUserDTO.setLastName("I".repeat(50));
        userDTO.setLastName("I".repeat(50));

        when(userService.createUser(any())).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_LastNameMidLength_ReturnsCreated() throws Exception {
        createUserDTO.setLastName("I".repeat(25));
        userDTO.setLastName("I".repeat(25));

        when(userService.createUser(any())).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_LastNameTooLong_ReturnsBadRequest() throws Exception {
        createUserDTO.setLastName("I".repeat(51));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Lastname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_InvalidPhone_ReturnsBadRequest() throws Exception {
        createUserDTO.setPhoneNumber("123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
    }

    @Test
    void createUser_AlreadyExists_ReturnsConflict() throws Exception {
        when(userService.createUser(any(CreateUserDTO.class)))
                .thenThrow(new AlreadyExistsException("Phone already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone already exists"));
    }

    // ------------------------- Get User Tests -------------------------
    @Test
    void getUserById_ValidId_ReturnsOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDTO);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDTO.getId()));
    }

    @Test
    void getUserById_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/users/{id}", 0L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void getUserById_NotFound_ReturnsNotFound() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void createUser_ValidPhone_ReturnsCreated() throws Exception {
        createUserDTO.setPhoneNumber("+79211234567");
        userDTO.setPhoneNumber("+79211234567");

        when(userService.createUser(any())).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phoneNumber").value("+79211234567"));
    }

    @Test
    void createUser_PhoneNull_ReturnsBadRequest() throws Exception {
        createUserDTO.setPhoneNumber(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("must not be blank"));
    }

    @Test
    void createUser_PhoneBlank_ReturnsBadRequest() throws Exception {
        createUserDTO.setPhoneNumber("   ");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
    }

    @Test
    void createUser_PhoneInvalidFormat_ReturnsBadRequest() throws Exception {
        String[] invalidPhones = {
                "89211234567",
                "+7921123456",
                "+792112345678",
                "+7921abc4567",
                "+7921 123 4567"
        };

        for (String phone : invalidPhones) {
            createUserDTO.setPhoneNumber(phone);

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
        }
    }

    @Test
    void createUser_PhoneAlreadyExists_ReturnsConflict() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new AlreadyExistsException("Phone number already in use"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already in use"));
    }

    // ------------------------- Update User Tests -------------------------
    @Test
    void updateUser_ValidRequest_ReturnsOk() throws Exception {
        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_FirstNameBlank_ReturnsBadRequest() throws Exception {
        updateUserDTO.setFirstName("   ");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name is required"));
    }

    @Test
    void updateUser_InvalidFirstName_ReturnsBadRequest() throws Exception {
        updateUserDTO.setFirstName("A");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void updateUser_FirstNameMinLength_ReturnsOk() throws Exception {
        updateUserDTO.setFirstName("Al");
        userDTO.setFirstName("Al");

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_FirstNameMaxLength_ReturnsOk() throws Exception {
        updateUserDTO.setFirstName("A".repeat(50));
        userDTO.setFirstName("A".repeat(50));

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_FirstNameMidLength_ReturnsOk() throws Exception {
        updateUserDTO.setFirstName("A".repeat(25));
        userDTO.setFirstName("A".repeat(25));

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_FirstNameTooLong_ReturnsBadRequest() throws Exception {
        updateUserDTO.setFirstName("A".repeat(51));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void updateUser_LastNameBlank_ReturnsBadRequest() throws Exception {
        updateUserDTO.setLastName("   ");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Last name is required"));
    }

    @Test
    void updateUser_InvalidLastName_ReturnsBadRequest() throws Exception {
        updateUserDTO.setLastName("I");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Lastname must be between 2 and 50 characters"));
    }

    @Test
    void updateUser_LastNameMinLength_ReturnsOk() throws Exception {
        updateUserDTO.setLastName("Iv");
        userDTO.setLastName("Iv");

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_LastNameMaxLength_ReturnsOk() throws Exception {
        updateUserDTO.setLastName("I".repeat(50));
        userDTO.setLastName("I".repeat(50));

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_LastNameMidLength_ReturnsOk() throws Exception {
        updateUserDTO.setLastName("I".repeat(25));
        userDTO.setLastName("I".repeat(25));

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_LastNameTooLong_ReturnsBadRequest() throws Exception {
        updateUserDTO.setLastName("I".repeat(51));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Lastname must be between 2 and 50 characters"));
    }

    @Test
    void updateUser_ValidPhone_ReturnsOk() throws Exception {
        updateUserDTO.setPhoneNumber("+79211234567");
        userDTO.setPhoneNumber("+79211234567");

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+79211234567"));
    }

    @Test
    void updateUser_PhoneEmpty_ReturnsBadRequest() throws Exception {
        updateUserDTO.setPhoneNumber("");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
    }

    @Test
    void updateUser_PhoneInvalidFormat_ReturnsBadRequest() throws Exception {
        String[] invalidPhones = {
                "89211234567",
                "+7921123456",
                "+792112345678",
                "+7921abc4567",
                "+7921 123 4567"
        };

        for (String phone : invalidPhones) {
            updateUserDTO.setPhoneNumber(phone);

            mockMvc.perform(put("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
        }
    }

    @Test
    void updateUser_PhoneAlreadyExists_ReturnsConflict() throws Exception {
        updateUserDTO.setPhoneNumber("+79123456789");

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class)))
                .thenThrow(new AlreadyExistsException("Phone number already in use"));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already in use"));
    }

    @Test
    void updateUser_SamePhoneNoConflict_ReturnsOk() throws Exception {
        // Пользователь обновляет данные, но оставляет свой текущий номер
        updateUserDTO.setPhoneNumber("+79123456789"); // Тот же номер, что был ранее
        userDTO.setPhoneNumber("+79123456789");

        when(userService.updateUser(anyLong(), any(UpdateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk());
    }

    // ------------------------- Delete User Tests -------------------------
    @Test
    void deleteUser_ValidId_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_InvalidId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(delete("/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void deleteUser_NotFound_ReturnsNotFound() throws Exception {
        Long id = 999L;

        doThrow(new NotFoundException("User not found with id: " + id))
                .when(userService).deleteUser(id);

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: " + id));
    }

    // ------------------------- Get All Users Tests -------------------------
    @Test
    void getAllUsers_ValidPagination_ReturnsOk() throws Exception {
        List<UserDTO> users = List.of(
                new UserDTO(1L, "Ivan", "Ivanov", "+79123456789"),
                new UserDTO(2L, "Petr", "Petrov", "+79234567890")
        );

        when(userService.getAllUsers(0, 10)).thenReturn(users);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("Ivan"))
                .andExpect(jsonPath("$[1].phoneNumber").value("+79234567890"));
    }

    @Test
    void getAllUsers_InvalidPage_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page must be greater than or equal to 0"));
    }

    @Test
    void getAllUsers_InvalidSize_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be greater than or equal to 1"));
    }

    @Test
    void getAllUsers_PageMinValue_ReturnsOk() throws Exception {
        when(userService.getAllUsers(0, 10)).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_SizeMinValue_ReturnsOk() throws Exception {
        when(userService.getAllUsers(0, 1)).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllUsers_WithDefaultPagination_ReturnsOk() throws Exception {
        when(userService.getAllUsers(0, 10)).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/users")) // Без параметров
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
