package com.zed.company_service.service.impl;

import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public CompanyEntity createCompany(CompanyEntity company) {
        return companyRepository.save(company);
    }

    @Override
    public Optional<CompanyEntity> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }
}
