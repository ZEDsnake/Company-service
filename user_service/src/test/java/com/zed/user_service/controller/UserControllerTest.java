package com.zed.user_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zed.user_service.dto.CompanyInfoDTO;
import com.zed.user_service.dto.CreateUserDTO;
import com.zed.user_service.dto.EmployeeDTO;
import com.zed.user_service.dto.UpdateUserDTO;
import com.zed.user_service.dto.UserDTO;
import com.zed.user_service.dto.UserInfoDTO;
import com.zed.user_service.exception.AlreadyExistsException;
import com.zed.user_service.exception.NotFoundException;
import com.zed.user_service.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false"})

class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserDTO validCreateUserDTO() {
        return new CreateUserDTO("John", "Doe", "+79123456789", 1L);
    }

    private UpdateUserDTO validUpdateUserDTO() {
        return new UpdateUserDTO("John", "Doe", "+79123456789", 1L);
    }

    private UserDTO validUserDTO() {
        return new UserDTO(1L, "John", "Doe", "+79123456789");
    }


    @Test
    void getUserInfoByCompanyId_ShouldReturnListOfUsers() throws Exception {
        List<UserInfoDTO> mockUsers = List.of(
                new UserInfoDTO("John", "Doe", "+72345678906"),
                new UserInfoDTO("Jane", "Smith", "+79876543217")
        );

        when(userService.getUserInfoByCompanyId(1L, 0, 10)).thenReturn(mockUsers);

        mockMvc.perform(get("/users/by-company/{companyId}", 1)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));

        verify(userService).getUserInfoByCompanyId(1L, 0, 10);
    }

    @Test
    void getUserInfoByCompanyId_ShouldReturnNotFound_WhenUserServiceThrows() throws Exception {
        when(userService.getUserInfoByCompanyId(1L, 0, 10))
                .thenThrow(new NotFoundException("Company not found"));

        mockMvc.perform(get("/users/by-company/{companyId}", 1)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"));
    }

    @Test
    void deleteUsersByCompanyId_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUsersByCompanyId(1L);

        mockMvc.perform(delete("/users/by-company/{companyId}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUsersByCompanyId(1L);
    }

    @Test
    void createUser_ShouldReturnCreated_WhenValidInput() throws Exception {
        UserDTO userDTO = new UserDTO(1L, "John", "Doe", "+79123456789");

        Mockito.when(userService.createUser(any(CreateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateUserDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.phoneNumber").value("+79123456789"));

        Mockito.verify(userService).createUser(any(CreateUserDTO.class));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenFirstNameIsBlank() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setFirstName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value(
                        Matchers.anyOf(
                                Matchers.is("First name is required"),
                                Matchers.is("Firstname must be between 2 and 50 characters"))));
    }


    @Test
    void createUser_ShouldReturnBadRequest_WhenFirstNameTooShort() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setFirstName("A");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenFirstNameTooLong() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setFirstName("A".repeat(51));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenLastNameIsBlank() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setLastName("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value(
                        Matchers.anyOf(
                                Matchers.is("Last name is required"),
                                Matchers.is("Lastname must be between 2 and 50 characters"))));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenLastNameTooShort() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setLastName("B");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Lastname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenLastNameTooLong() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setLastName("B".repeat(51));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Lastname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenPhoneNumberInvalid() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setPhoneNumber("12345");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenCompanyIdIsNull() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setCompanyId(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Company ID is required"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenCompanyIdIsNegative() throws Exception {
        CreateUserDTO dto = validCreateUserDTO();
        dto.setCompanyId(0L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Company ID must be positive"));
    }


    @Test
    void createUser_ShouldReturnConflict_WhenPhoneAlreadyExists() throws Exception {
        Mockito.when(userService.createUser(any(CreateUserDTO.class)))
                .thenThrow(new AlreadyExistsException("Phone number already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateUserDTO())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already exists"));
    }

    @Test
    void createUser_ShouldReturnNotFound_WhenCompanyDoesNotExist() throws Exception {
        Mockito.when(userService.createUser(any(CreateUserDTO.class)))
                .thenThrow(new NotFoundException("Company not found"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateUserDTO())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"));
    }

    @Test
    void getUserByIdWithCompany_ShouldReturnEmployeeDTO_WhenUserExists() throws Exception {
        EmployeeDTO employeeDTO = new EmployeeDTO("John", "Doe", "+79123456789", new CompanyInfoDTO(1L, "Acme Corp"));
        given(userService.getUserWithCompany(1L)).willReturn(employeeDTO);

        mockMvc.perform(get("/users/1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+79123456789"))
                .andExpect(jsonPath("$.company.id").value(1))
                .andExpect(jsonPath("$.company.name").value("Acme Corp"));
    }

    @Test
    void getUserByIdWithCompany_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        given(userService.getUserWithCompany(99L)).willThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/99/info"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getUserByIdWithCompany_ShouldReturnBadRequest_WhenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/users/0/info"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenInputIsValid() throws Exception {
        UpdateUserDTO dto = validUpdateUserDTO();
        UserDTO responseDto = validUserDTO();

        when(userService.updateUser(eq(1L), any(UpdateUserDTO.class))).thenReturn(responseDto);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.firstName").value(responseDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(responseDto.getLastName()))
                .andExpect(jsonPath("$.phoneNumber").value(responseDto.getPhoneNumber()));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenIdIsInvalid() throws Exception {
        UpdateUserDTO dto = validUpdateUserDTO();

        mockMvc.perform(put("/users/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenFirstNameIsBlank() throws Exception {
        UpdateUserDTO dto = validUpdateUserDTO();
        dto.setFirstName("");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("First name is required"));
    }


    @Test
    void updateUser_ShouldReturnBadRequest_WhenFirstNameTooShort() throws Exception {
        UpdateUserDTO dto = validUpdateUserDTO();
        dto.setFirstName("A");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }


    @Test
    void updateUser_ShouldReturnBadRequest_WhenLastNameIsBlank() throws Exception {
        UpdateUserDTO dto = validUpdateUserDTO();
        dto.setLastName("");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value(
                        Matchers.anyOf(
                                Matchers.is("Last name is required"),
                                Matchers.is("Lastname must be between 2 and 50 characters"))));
    }


    @Test
    void updateUser_ShouldReturnBadRequest_WhenPhoneNumberIsInvalid() throws Exception {
        UpdateUserDTO dto = validUpdateUserDTO();
        dto.setPhoneNumber("12345");

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
    }


    @Test
    void updateUser_ShouldReturnBadRequest_WhenCompanyIdIsNull() throws Exception {
        UpdateUserDTO dto = validUpdateUserDTO();
        dto.setCompanyId(null);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Company ID is required"));
    }


    @Test
    void updateUser_ShouldReturnBadRequest_WhenCompanyIdIsZero() throws Exception {
        UpdateUserDTO dto = validUpdateUserDTO();
        dto.setCompanyId(0L);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Company ID must be positive"));
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenUserExists() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_ShouldReturnBadRequest_WhenIdIsInvalid() throws Exception {
        mockMvc.perform(delete("/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be a positive number and not less than 1"));
    }


    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        Long userId = 999L;

        doThrow(new NotFoundException("User with ID " + userId + " not found"))
                .when(userService).deleteUser(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with ID 999 not found"));
    }

    @Test
    void getAllUsers_ShouldReturnOk_WithDefaultPagination() throws Exception {
        List<UserDTO> users = List.of(
                new UserDTO(1L, "John", "Doe", "+71234567890"),
                new UserDTO(2L, "Jane", "Smith", "+79876543210")
        );

        when(userService.getAllUsers(0, 10)).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(users.size()))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].phoneNumber").value("+79876543210"));
    }

    @Test
    void getAllUsers_ShouldReturnOk_WithCustomPagination() throws Exception {
        List<UserDTO> users = List.of(new UserDTO(3L, "Alice", "Brown", "+79001234567"));

        when(userService.getAllUsers(1, 1)).thenReturn(users);

        mockMvc.perform(get("/users?page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Alice"));
    }

    @Test
    void getAllUsers_ShouldReturnBadRequest_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/users?page=-1&size=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page must be greater than or equal to 0"));
    }

    @Test
    void getAllUsers_ShouldReturnBadRequest_WhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/users?page=0&size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be greater than or equal to 1"));
    }

    @Test
    void getAllUsers_ShouldReturnBadRequest_WhenSizeIsNegative() throws Exception {
        mockMvc.perform(get("/users?page=0&size=-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Size must be greater than or equal to 1"));
    }
}

