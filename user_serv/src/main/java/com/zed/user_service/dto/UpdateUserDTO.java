package com.zed.user_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Lastname must be between 2 and 50 characters")
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^\\+7[0-9]{10}$", message = "Phone must be in format +79123456789")
    private String phoneNumber;

    @NotNull(message = "Company ID is required")
    @Min(value = 1, message = "Company ID must be positive")
    private Long companyId;
}
