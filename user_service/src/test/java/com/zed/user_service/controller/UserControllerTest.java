package com.zed.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zed.user_service.dto.CreateUserDto;
import com.zed.user_service.dto.PagedUserResponseDto;
import com.zed.user_service.dto.PatchUserDto;
import com.zed.user_service.dto.UpdateUserDto;
import com.zed.user_service.dto.UserInfoDto;
import com.zed.user_service.dto.UserResponseDto;
import com.zed.user_service.exception.AlreadyExistsException;
import com.zed.user_service.exception.NotFoundException;
import com.zed.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.cloud.config.enabled=false"})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_Return201_WhenValid() throws Exception {
        CreateUserDto dto = new CreateUserDto("John", "Doe", "+71234567890", 1L);
        UserResponseDto responseDto = new UserResponseDto(1L, "John", "Doe", "+71234567890", null);

        when(userService.createUser(any())).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void createUser_Return400_WhenFirstNameTooShort() throws Exception {
        CreateUserDto dto = new CreateUserDto("J", "Doe", "+71234567890", 1L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_Return409_WhenAlreadyExistsException() throws Exception {
        CreateUserDto dto = new CreateUserDto("John", "Doe", "+71234567890", 1L);

        when(userService.createUser(any())).thenThrow(new AlreadyExistsException("Phone already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone already exists"));
    }

    @Test
    void createUser_Return404_WhenNotFoundException() throws Exception {
        CreateUserDto dto = new CreateUserDto("John", "Doe", "+71234567890", 99L);

        when(userService.createUser(any())).thenThrow(new NotFoundException("Company not found"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"));
    }

    @Test
    void createUser_Return400_WhenFirstNameBlank() throws Exception {
        CreateUserDto dto = new CreateUserDto("", "Doe", "+71234567890", 1L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value(anyOf(
                        is("First name is required"),
                        is("Firstname must be between 2 and 50 characters")
                )));
    }

    @Test
    void createUser_Return400_WhenLastNameBlank() throws Exception {
        CreateUserDto dto = new CreateUserDto("John", "", "+71234567890", 1L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value(anyOf(
                        is("Last name is required"),
                        is("Lastname must be between 2 and 50 characters")
                )));
    }

    @Test
    void createUser_Return400_WhenLastNameTooShort() throws Exception {
        CreateUserDto dto = new CreateUserDto("John", "D", "+71234567890", 1L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Lastname must be between 2 and 50 characters"));
    }

    @Test
    void createUser_Return400_WhenInvalidPhoneFormat() throws Exception {
        CreateUserDto dto = new CreateUserDto("John", "Doe", "12345", 1L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
    }

    @Test
    void createUser_Return400_WhenCompanyIdNull() throws Exception {
        CreateUserDto dto = new CreateUserDto("John", "Doe", "+71234567890", null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Company ID is required"));
    }

    @Test
    void createUser_Return400_WhenCompanyIdInvalid() throws Exception {
        CreateUserDto dto = new CreateUserDto("John", "Doe", "+71234567890", 0L);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Company ID must be positive"));
    }

    @Test
    void getUserById_Return200_WhenValid() throws Exception {
        UserResponseDto responseDto = new UserResponseDto(1L, "John", "Doe", "+71234567890", null);

        when(userService.getUserById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserById_Return400_WhenInvalidId() throws Exception {
        mockMvc.perform(get("/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").value("Id must be positive and greater than 0"));
    }

    @Test
    void getUserById_Return404_WhenNotFoundException() throws Exception {
        when(userService.getUserById(1L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updateUser_Return200_WhenValid() throws Exception {
        UpdateUserDto dto = new UpdateUserDto("John", "Doe", "+71234567890", 1L);
        UserResponseDto responseDto = new UserResponseDto(1L, "John", "Doe", "+71234567890", null);

        when(userService.updateUser(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateUser_Return400_WhenInvalidId() throws Exception {
        UpdateUserDto dto = new UpdateUserDto("John", "Doe", "+71234567890", 1L);

        mockMvc.perform(put("/users/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").value("Id must be positive and greater than 0"));
    }

    @Test
    void updateUser_Return409_WhenAlreadyExistsException() throws Exception {
        UpdateUserDto dto = new UpdateUserDto("John", "Doe", "+71234567890", 1L);

        when(userService.updateUser(eq(1L), any())).thenThrow(new AlreadyExistsException("Phone exists"));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone exists"));
    }

    @Test
    void updateUser_Return404_WhenNotFoundException() throws Exception {
        UpdateUserDto dto = new UpdateUserDto("John", "Doe", "+71234567890", 1L);

        when(userService.updateUser(eq(1L), any())).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void updateUser_Return400_WhenFirstNameBlank() throws Exception {
        UpdateUserDto dto = new UpdateUserDto("", "Doe", "+71234567890", 1L);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value(anyOf(
                        is("First name is required"),
                        is("Firstname must be between 2 and 50 characters")
                )));
    }

    @Test
    void updateUser_Return400_WhenFirstNameTooShort() throws Exception {
        UpdateUserDto dto = new UpdateUserDto("J", "Doe", "+71234567890", 1L);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void updateUser_Return400_WhenInvalidPhoneFormat() throws Exception {
        UpdateUserDto dto = new UpdateUserDto("John", "Doe", "12345", 1L);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
    }

    @Test
    void deleteUser_Return204_WhenValid() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_Return400_WhenInvalidId() throws Exception {
        mockMvc.perform(delete("/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").value("Id must be positive and greater than 0"));
    }

    @Test
    void deleteUser_Return404_WhenNotFoundException() throws Exception {
        doThrow(new NotFoundException("User not found")).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getAllUsers_Return200_WhenValid() throws Exception {
        PagedUserResponseDto responseDto = new PagedUserResponseDto(List.of(), 0, 1, 0);

        when(userService.getAllUsers(0, 10)).thenReturn(responseDto);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray());
    }

    @Test
    void getAllUsers_Return400_WhenPageInvalid() throws Exception {
        mockMvc.perform(get("/users?page=-1&size=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.page").value("Page must be positive and greater than 0"));
    }

    @Test
    void getAllUsers_Return400_WhenSizeInvalid() throws Exception {
        mockMvc.perform(get("/users?page=0&size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.size").value("Size must be positive and greater than 1"));
    }

    @Test
    void patchUser_Return200_WhenValid() throws Exception {
        PatchUserDto dto = new PatchUserDto("John", "Doe", "+71234567890", 1L);
        UserResponseDto responseDto = new UserResponseDto(1L, "John", "Doe", "+71234567890", null);

        when(userService.patchUser(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void patchUser_Return400_WhenInvalidId() throws Exception {
        PatchUserDto dto = new PatchUserDto("John", "Doe", "+71234567890", 1L);

        mockMvc.perform(patch("/users/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").value("Id must be positive and greater than 0"));
    }

    @Test
    void patchUser_Return404_WhenNotFoundException() throws Exception {
        PatchUserDto dto = new PatchUserDto("John", "Doe", "+71234567890", 1L);

        when(userService.patchUser(eq(1L), any())).thenThrow(new NotFoundException("User not found"));
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void patchUser_Return400_WhenFirstNameTooShort() throws Exception {
        PatchUserDto dto = new PatchUserDto("J", null, null, null);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.firstName").value("Firstname must be between 2 and 50 characters"));
    }

    @Test
    void patchUser_Return400_WhenLastNameTooShort() throws Exception {
        PatchUserDto dto = new PatchUserDto(null, "D", null, null);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lastName").value("Lastname must be between 2 and 50 characters"));
    }

    @Test
    void patchUser_Return400_WhenInvalidPhoneFormat() throws Exception {
        PatchUserDto dto = new PatchUserDto(null, null, "12345", null);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phoneNumber").value("Phone must be in format +79123456789"));
    }

    @Test
    void patchUser_Return400_WhenInvalidCompanyId() throws Exception {
        PatchUserDto dto = new PatchUserDto(null, null, null, 0L);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Company ID must be positive"));
    }

    @Test
    void getUsersByIds_Return200_WhenValid() throws Exception {
        List<UserInfoDto> result = List.of(new UserInfoDto(1L, "John", "Doe", "+71234567890"));
        when(userService.getUsersByIds(anyList())).thenReturn(result);

        mockMvc.perform(post("/users/by-ids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void deleteUsersByCompanyId_Return204_WhenValid() throws Exception {
        mockMvc.perform(delete("/users/by-company/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUsersByCompanyId_Return400_WhenInvalidCompanyId() throws Exception {
        mockMvc.perform(delete("/users/by-company/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.companyId").value("Id must be positive and greater than 0"));
    }

    @Test
    void deleteUsersByCompanyId_Return404_WhenNotFoundException() throws Exception {
        doThrow(new NotFoundException("Company not found"))
                .when(userService).deleteUsersByCompanyId(1L);

        mockMvc.perform(delete("/users/by-company/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"));
    }

    @Test
    void updateUserCompany_Return204_WhenValid() throws Exception {
        mockMvc.perform(post("/users/1/company?companyId=2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateUserCompany_Return400_WhenInvalidUserId() throws Exception {
        mockMvc.perform(post("/users/0/company?companyId=2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId").value("Id must be positive and greater than 0"));
    }

    @Test
    void updateUserCompany_Return404_WhenNotFoundException() throws Exception {
        doThrow(new NotFoundException("User not found"))
                .when(userService).updateUserCompany(1L, 2L);

        mockMvc.perform(post("/users/1/company?companyId=2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}