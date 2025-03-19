package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;

public interface CompanyService {
    CompanyDTO addCompany(CreateCompanyDTO companyDTO);
    CompanyDTO getCompanyById(Long id);
}

