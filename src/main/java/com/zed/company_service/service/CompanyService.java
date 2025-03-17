package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CompanyService {
    CompanyDTO addCompany(CompanyDTO companyDTO);
    CompanyDTO getCompanyById(Long id);
}

