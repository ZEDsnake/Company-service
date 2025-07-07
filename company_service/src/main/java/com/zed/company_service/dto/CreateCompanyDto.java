package com.zed.company_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyDto {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Budget is required")
    @PositiveOrZero(message = "Budget must be positive or zero")
    private BigDecimal budget;

    @NotNull(message = "Employee IDs cannot be null")
    private List<@Min(value = 1, message = "Employee ID must be greater than 0") Long> employeeIds = new ArrayList<>();
}