package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.dto.UserInfoDTO;

import java.util.List;

public interface CompanyService {

    List<UserInfoDTO> getCompanyEmployees(Long companyId, int page, int size);

    CompanyDTO addCompany(CreateCompanyDTO companyDTO);

    CompanyDTO updateCompany(Long id, UpdateCompanyDTO updateCompanyDTO);

    void deleteCompany(Long id);

    List<CompanyDTO> getCompanies(int page, int size);

    CompanyDTO getCompanyById(Long id);
}

