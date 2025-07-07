package com.zed.company_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedCompanyResponseDto {

    private List<CompanyResponseDto> companies;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}