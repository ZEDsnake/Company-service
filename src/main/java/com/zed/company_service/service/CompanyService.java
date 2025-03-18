package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;

public interface CompanyService {
    CompanyDTO addCompany(CompanyDTO companyDTO);
    CompanyDTO getCompanyById(Long id);
}

