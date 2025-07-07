package com.zed.user_service.controller;

import com.zed.user_service.dto.CreateUserDto;
import com.zed.user_service.dto.PagedUserResponseDto;
import com.zed.user_service.dto.PatchUserDto;
import com.zed.user_service.dto.UpdateUserDto;
import com.zed.user_service.dto.UserInfoDto;
import com.zed.user_service.dto.UserResponseDto;
import com.zed.user_service.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createUser(
            @Valid
            @RequestBody CreateUserDto dto) {
        log.info("POST request to /users with body: {}", dto);
        return userService.createUser(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto updateUser(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0") Long id,
            @Valid
            @RequestBody UpdateUserDto dto) {
        log.info("PUT request to /users/{} - update user", id);
        return userService.updateUser(id, dto);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto patchUser(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0") Long id,
            @Valid
            @RequestBody PatchUserDto dto) {
        log.info("PATCH request to /users/{} - patch user", id);
        return userService.patchUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0") Long id) {
        log.info("DELETE request to /users/{} - delete user", id);
        userService.deleteUser(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponseDto getUserById(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0") Long id) {
        log.info("GET request to /users/{} - get user", id);
        return userService.getUserById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedUserResponseDto getAllUsers(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be positive and greater than 0") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be positive and greater than 1") int size) {
        log.info("GET request to /users - get all users, page: {}, size: {}", page, size);
        return userService.getAllUsers(page, size);
    }

    @PostMapping("/by-ids")
    @ResponseStatus(HttpStatus.OK)
    public List<UserInfoDto> getUsersByIds(
            @RequestBody List<Long> userIds) {
        return userService.getUsersByIds(userIds);
    }

    @DeleteMapping("/by-company/{companyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUsersByCompanyId(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0")Long companyId) {
        userService.deleteUsersByCompanyId(companyId);
    }

    @PostMapping("/{userId}/company")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserCompany(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0")Long userId,
            @RequestParam(required = false)
            @Min(value = 1, message = "Id must be positive and greater than 0")Long companyId) {
        log.info("Updating company for user {} to {}", userId, companyId);
        userService.updateUserCompany(userId, companyId);
    }
}