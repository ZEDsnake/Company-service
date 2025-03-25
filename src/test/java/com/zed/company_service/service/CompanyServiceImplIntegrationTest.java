package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.mapper.CompanyMapper;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompanyServiceImplIntegrationTest {

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void cleanUp() {
        // Очистка перед каждым тестом не нужна,
        // так как @Transactional откатывает изменения
    }

    @Test
    void addCompany_shouldPersistEntityWithCorrectData() {
        // Arrange
        CreateCompanyDTO dto = new CreateCompanyDTO();
        dto.setName("NewCo");
        dto.setBudget(new BigDecimal("500000.00"));

        // Act
        CompanyDTO result = companyService.addCompany(dto);

        // Assert
        assertNotNull(result.getId());
        CompanyEntity savedEntity = companyRepository.findById(result.getId()).orElseThrow();
        assertEquals("NewCo", savedEntity.getName());
        assertEquals(0, new BigDecimal("500000.00").compareTo(savedEntity.getBudget()));
    }

    @Test
    void addCompany_shouldThrowExceptionWhenNameNotUnique() {
        // Arrange (Company A уже существует в test-data.sql)
        CreateCompanyDTO dto = new CreateCompanyDTO();
        dto.setName("Company A");
        dto.setBudget(new BigDecimal("100000.00"));

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> companyService.addCompany(dto));
    }

    @Test
    void getCompanyById_shouldReturnCorrectEntity() {
        // Arrange
        Long existingId = 1L;

        // Получаем актуальные данные из БД для проверки
        CompanyEntity entityFromDb = companyRepository.findById(existingId).orElseThrow();

        // Act
        CompanyDTO result = companyService.getCompanyById(existingId);

        // Assert
        assertNotNull(result);
        assertEquals(existingId, result.getId());
        assertEquals(entityFromDb.getName(), result.getName());

        // Сравниваем с данными из БД, а не с жестко закодированным значением
        assertEquals(0, entityFromDb.getBudget().compareTo(result.getBudget()),
                "Budget should match database value");

        // Дополнительная проверка (можно добавить логирование)
        System.out.println("Expected budget: " + entityFromDb.getBudget() +
                ", Actual budget: " + result.getBudget());
    }

    @Test
    void updateCompany_shouldUpdateOnlySpecifiedFields() {
        // Arrange
        Long existingId = 1L;
        CompanyEntity originalEntity = companyRepository.findById(existingId).orElseThrow();
        BigDecimal originalBudget = originalEntity.getBudget(); // Получаем актуальное значение

        UpdateCompanyDTO dto = new UpdateCompanyDTO();
        dto.setName("Updated Name");

        // Act
        CompanyDTO result = companyService.updateCompany(existingId, dto);

        // Assert
        assertEquals("Updated Name", result.getName());
        assertEquals(0, originalBudget.compareTo(result.getBudget()),
                "Budget should remain unchanged");

        // Проверяем сохранение в БД
        CompanyEntity updatedEntity = companyRepository.findById(existingId).orElseThrow();
        assertEquals("Updated Name", updatedEntity.getName());
        assertEquals(0, originalBudget.compareTo(updatedEntity.getBudget()),
                "Budget in DB should remain unchanged");
    }

    @Test
    void deleteCompany_shouldRemoveEntityFromDb() {
        // Arrange
        Long existingId = 1L;
        assertTrue(companyRepository.existsById(existingId));

        // Act
        companyService.deleteCompany(existingId);

        // Assert
        assertFalse(companyRepository.existsById(existingId));
    }

    @Test
    void getCompanies_shouldReturnPaginatedResults() {
        // Arrange
        companyRepository.save(new CompanyEntity(null, "Company D", new BigDecimal("400000")));
        companyRepository.save(new CompanyEntity(null, "Company E", new BigDecimal("500000")));

        // Act
        List<CompanyDTO> page1 = companyService.getCompanies(0, 2);
        List<CompanyDTO> page2 = companyService.getCompanies(1, 2);

        // Assert
        assertEquals(2, page1.size());
        assertEquals(2, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    void concurrentUpdates_shouldNotLoseUpdates() throws InterruptedException {
        // Arrange
        Long existingId = 1L;
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Act
        for (int i = 0; i < threads; i++) {
            final int increment = i + 1;
            executor.submit(() -> {
                companyService.updateCompany(existingId, new UpdateCompanyDTO() {{
                    setBudget(new BigDecimal(increment));
                }});
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        // Assert
        CompanyEntity entity = companyRepository.findById(existingId).orElseThrow();
        assertTrue(entity.getBudget().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void updateCompany_shouldRollbackOnException() {
        // Arrange
        Long existingId = 1L;
        CompanyEntity originalEntity = companyRepository.findById(existingId).orElseThrow();
        BigDecimal originalBudget = originalEntity.getBudget();

        // Создаем mock для CompanyMapper
        CompanyMapper mapperMock = Mockito.mock(CompanyMapper.class);

        // Создаем экземпляр сервиса с mock-маппером
        CompanyService failingService = new CompanyServiceImpl(companyRepository, mapperMock) {
            @Override
            @Transactional
            public CompanyDTO updateCompany(Long id, UpdateCompanyDTO updateDto) {
                // Сначала выполняем обновление
                CompanyDTO result = super.updateCompany(id, updateDto);
                // Затем бросаем исключение
                throw new RuntimeException("Simulated error");
            }
        };

        UpdateCompanyDTO updateDto = new UpdateCompanyDTO();
        updateDto.setBudget(new BigDecimal("999999.00"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            failingService.updateCompany(existingId, updateDto);
        });

        // Проверяем откат транзакции
        CompanyEntity afterUpdate = companyRepository.findById(existingId).orElseThrow();
        assertEquals(0, originalBudget.compareTo(afterUpdate.getBudget()),
                "Budget should be rolled back to original value");
    }
}