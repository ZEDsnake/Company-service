package com.zed.company_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters ")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Lastname must be between 2 and 50 characters ")
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^\\+7[0-9]{10}$", message = "Phone must be in format +79123456789")
    private String phoneNumber;
}