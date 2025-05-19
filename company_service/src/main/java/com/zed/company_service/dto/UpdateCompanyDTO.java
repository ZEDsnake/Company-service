package com.zed.company_service.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateCompanyDTO {

    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @PositiveOrZero(message = "Budget must be positive or zero")
    private BigDecimal budget;
}
