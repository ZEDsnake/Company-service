package com.zed.company_service.service.impl;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.mapper.CompanyMapper;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    @Transactional
    public CompanyDTO updateCompany(Long id, UpdateCompanyDTO updateCompanyDTO) {
        CompanyEntity companyEntity = findCompanyById(id); // Находим компанию по ID
        companyMapper.updateEntityFromDTO(updateCompanyDTO, companyEntity); // Обновляем поля
        CompanyEntity updatedEntity = companyRepository.save(companyEntity); // Сохраняем изменения
        CompanyDTO result = companyMapper.toCompanyDTO(updatedEntity); // Преобразуем Entity в DTO
        log.info("CompanyService: updateCompany method result: {}", result); // Логируем результат
        return result; // Возвращаем обновлённую компанию
    }

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        CompanyEntity companyEntity = findCompanyById(id); // Находим компанию по ID
        companyRepository.delete(companyEntity);// Удаляем компанию
        log.info("CompanyService: Company with id={} deleted successfully", id);//логгируем успешное удаление
    }

    @Override
    @Transactional
    public List<CompanyDTO> getCompanies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // Создаём объект пагинации
        Page<CompanyEntity> companyPage = companyRepository.findAll(pageable); // Получаем страницу компаний
        List<CompanyDTO> companyDTOList = companyMapper.toCompanyDTOList(companyPage.getContent());
        log.info("Retrieved {} companies for page {} with size {}", companyDTOList.size(), page, size);
        return  companyDTOList;
    }

    private CompanyEntity findCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + id));
    }
}
