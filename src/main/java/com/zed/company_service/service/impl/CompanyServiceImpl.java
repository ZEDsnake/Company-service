package com.zed.company_service.service.impl;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.mapper.CompanyMapper;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.CompanyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;


    public CompanyDTO addCompany(CompanyDTO companyDTO) {
        CompanyEntity companyEntity = companyMapper.toEntity(companyDTO);
        CompanyEntity savedEntity = companyRepository.save(companyEntity);
        return companyMapper.toDTO(savedEntity);
    }

    public CompanyDTO getCompanyById(Long id) {
        CompanyEntity companyEntity = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
        return companyMapper.toDTO(companyEntity);
    }
}
