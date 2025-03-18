package com.zed.company_service.service.impl;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.exception.CompanyNotFoundException;
import com.zed.company_service.mapper.CompanyMapper;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    public CompanyDTO addCompany(CompanyDTO companyDTO) {
        CompanyEntity companyEntity = companyMapper.toEntity(companyDTO);
        CompanyEntity savedEntity = companyRepository.save(companyEntity);
        CompanyDTO result = companyMapper.toDTO(savedEntity);
        log.info("CompanyService: addCompany method result: {}", result);
        return result;
    }

    @Override
    public CompanyDTO getCompanyById(Long id) {
        CompanyEntity companyEntity = companyRepository.findById(id)
                .orElseThrow(()-> new CompanyNotFoundException("Company not found with id: " +id));
        CompanyDTO result = companyMapper.toDTO(companyEntity);
        log.info("CompanyService: getCompanyById method result: {}", result);
        return result;
    }
}
