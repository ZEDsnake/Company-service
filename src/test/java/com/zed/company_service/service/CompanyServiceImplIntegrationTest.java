package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.impl.CompanyServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompanyServiceImplIntegrationTest {

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanUp() {
    }

    @Test
    void addCompany_ReturnDto_WithGeneratedIdAndValidData() {
        CreateCompanyDTO dto = new CreateCompanyDTO("NewCo", new BigDecimal("500000.00"));

        CompanyDTO result = companyService.addCompany(dto);

        assertNotNull(result.getId());
        CompanyEntity savedEntity = companyRepository.findById(result.getId()).orElseThrow();
        assertEquals("NewCo", savedEntity.getName());
        assertEquals(0, new BigDecimal("500000.00").compareTo(savedEntity.getBudget()));
    }

    @Test
    void addCompany_shouldThrow_WhenNameNotUnique() {
        companyRepository.save(new CompanyEntity(null, "ExistingName", BigDecimal.ONE));
        CreateCompanyDTO dto = new CreateCompanyDTO("ExistingName", BigDecimal.TEN);

        assertThrows(DataIntegrityViolationException.class, () -> companyService.addCompany(dto));
    }

    @Test
    void getCompanyById_ReturnCorrectEntity() {
        CompanyEntity savedEntity = companyRepository.save(
                new CompanyEntity(null, "TestCompany", new BigDecimal("1000.00"))
        );

        CompanyDTO result = companyService.getCompanyById(savedEntity.getId());

        assertEquals(savedEntity.getId(), result.getId());
        assertEquals(savedEntity.getName(), result.getName());
        assertEquals(0, savedEntity.getBudget().compareTo(result.getBudget()));
    }

    @Test
    void updateCompany_ModifyOnlyProvidedFields() {
        CompanyEntity original = companyRepository.save(
                new CompanyEntity(null, "Original", new BigDecimal("2000.00"))
        );

        CompanyDTO result = companyService.updateCompany(
                original.getId(),
                new UpdateCompanyDTO("Updated", null)
        );

        assertEquals("Updated", result.getName());
        assertEquals(0, original.getBudget().compareTo(result.getBudget()));
    }

    @Test
    void deleteCompany_RemoveEntityFromDb() {
        CompanyEntity entity = companyRepository.save(
                new CompanyEntity(null, "ToDelete", BigDecimal.ONE)
        );

        companyService.deleteCompany(entity.getId());

        assertThrows(NotFoundException.class,
                () -> companyService.getCompanyById(entity.getId()));
    }

    @Test
    void getCompanies_shouldReturnPaginatedResults() {
        companyRepository.save(new CompanyEntity(null, "Company D", new BigDecimal("400000")));
        companyRepository.save(new CompanyEntity(null, "Company E", new BigDecimal("500000")));

        List<CompanyDTO> page1 = companyService.getCompanies(0, 2);
        List<CompanyDTO> page2 = companyService.getCompanies(1, 2);

        assertEquals(2, page1.size());
        assertEquals(2, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }
}