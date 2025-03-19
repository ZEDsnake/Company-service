package com.zed.company_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.math.BigDecimal;

@Data
public class CreateCompanyDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Budget is required")
    @PositiveOrZero(message = "Budget must be positive or zero")
    private BigDecimal budget;
}
