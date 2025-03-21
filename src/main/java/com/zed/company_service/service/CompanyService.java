package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;

import java.util.List;

public interface CompanyService {
    CompanyDTO addCompany(CreateCompanyDTO companyDTO);

    CompanyDTO getCompanyById(Long id);

    CompanyDTO updateCompany(Long id,UpdateCompanyDTO updateCompanyDTO);

    void deleteCompany(Long id);

    List<CompanyDTO> getCompanies(int page, int size);
}

