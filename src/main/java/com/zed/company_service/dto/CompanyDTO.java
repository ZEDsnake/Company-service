package com.zed.company_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompanyDTO {
    private Long id;
    private String name;
    private BigDecimal budget;
}
