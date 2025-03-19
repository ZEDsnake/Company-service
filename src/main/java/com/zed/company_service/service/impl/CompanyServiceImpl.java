package com.zed.company_service.service.impl;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.mapper.CompanyMapper;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.CompanyService;
import jakarta.transaction.Transactional;
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
    @Transactional
    public CompanyDTO addCompany(CreateCompanyDTO createCompanyDTO) {
        CompanyEntity companyEntity = companyMapper.toCompanyEntity(createCompanyDTO); // Преобразуем CreateCompanyDTO в Entity
        CompanyEntity savedEntity = companyRepository.save(companyEntity);
        CompanyDTO result = companyMapper.toCompanyDTO(savedEntity); // Преобразуем Entity в CompanyDTO
        log.info("CompanyService: addCompany method result: {}", result);
        return result; // Возвращаем CompanyDTO
    }

    @Override
    public CompanyDTO getCompanyById(Long id) {
        CompanyEntity companyEntity = findCompanyById(id);
        CompanyDTO result = companyMapper.toCompanyDTO(companyEntity);
        log.info("CompanyService: getCompanyById method result: {}", result);
        return result;
    }

    private CompanyEntity findCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + id));
    }
}
