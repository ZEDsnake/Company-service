package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyResponseDto;
import com.zed.company_service.dto.CreateCompanyDto;
import com.zed.company_service.dto.PagedCompanyResponseDto;
import com.zed.company_service.dto.PatchCompanyDto;
import com.zed.company_service.dto.UpdateCompanyDto;

public interface CompanyService {

    CompanyResponseDto createCompany(CreateCompanyDto dto);

    CompanyResponseDto updateCompany(Long id, UpdateCompanyDto dto);

    CompanyResponseDto patchCompany(Long id, PatchCompanyDto dto);

    void deleteCompany(Long id);

    CompanyResponseDto getCompanyById(Long id);

    PagedCompanyResponseDto getAllCompanies(int page, int size);

    void addEmployeeToCompany(Long companyId, Long employeeId);

    void removeEmployeeFromCompany(Long companyId, Long employeeId);
}