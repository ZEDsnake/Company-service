package com.zed.company_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompanyDTO {
    private Long id;
    private String name;
    private BigDecimal budget;

    @JsonIgnoreProperties("company")
    private List<UserDTO> employees;
}