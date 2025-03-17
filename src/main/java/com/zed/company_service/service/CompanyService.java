package com.zed.company_service.service;

import com.zed.company_service.entity.CompanyEntity;

import java.util.Optional;

public interface CompanyService {
    CompanyEntity createCompany(CompanyEntity company);
    Optional<CompanyEntity> getCompanyById(Long id);
}
