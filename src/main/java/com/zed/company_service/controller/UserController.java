package com.zed.company_service.controller;

import com.zed.company_service.dto.CreateUserDTO;
import com.zed.company_service.dto.EmployeeDTO;
import com.zed.company_service.dto.UpdateUserDTO;
import com.zed.company_service.dto.UserDTO;
import com.zed.company_service.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        log.info("POST request received: \"/users\" with body {}", createUserDTO);
        return  userService.createUser(createUserDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO getUserByID (@PathVariable @Min(value = 1, message = "ID must be a positive number and not less than 1")Long id) {
        log.info("GET request received: \"/users/{}\"",id);
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/info")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDTO getUserByIdWithCompany(@PathVariable @Min(value = 1, message = "ID must be a positive number and not less than 1") Long id) {
        log.info("GET request received: \"/users/{}/info\"", id);
        return userService.getUserWithCompany(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO updateUser(@PathVariable @Min(value = 1, message = "ID must be a positive number and not less than 1") Long id,
                              @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        log.info("PUT request received: \"/users/{}\" with body: {}", id, updateUserDTO);
        return userService.updateUser(id, updateUserDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Min(value = 1, message = "ID must be a positive number and not less than 1") Long id) {
        log.info("DELETE request received: \"/users/{}\"", id);
        userService.deleteUser(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDTO> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be greater than or equal to 1") int size) {
        log.info("GET request received: \"/users\" with pagination: page={}, size={}", page, size);
        return userService.getAllUsers(page, size);
    }
}
