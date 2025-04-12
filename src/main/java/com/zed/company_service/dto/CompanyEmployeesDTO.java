package com.zed.company_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompanyEmployeesDTO {
    private Long id;
    private String name;
    private BigDecimal budget;
    private List<UserInfoDTO> employees;
}