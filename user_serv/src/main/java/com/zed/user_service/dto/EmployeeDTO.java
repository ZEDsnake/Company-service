package com.zed.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmployeeDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private CompanyInfoDTO company;
}
