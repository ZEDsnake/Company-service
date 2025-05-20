package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class CompanyServiceImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll(); // чистим перед каждым тестом
    }

    @Test
    void addCompany_ShouldPersistCompany() {
        // given
        CreateCompanyDTO createCompanyDTO = new CreateCompanyDTO("TestCompany", new BigDecimal("1000.00"));

        // when
        CompanyDTO result = companyService.addCompany(createCompanyDTO);

        // then
        assertNotNull(result.getId());
        assertEquals("TestCompany", result.getName());
        assertEquals(new BigDecimal("1000.00"), result.getBudget());
    }

    @Test
    void getCompanyById_ShouldReturnCorrectCompany() {
        // given
        CompanyEntity entity = new CompanyEntity(null, "Acme", new BigDecimal("500.00"));
        CompanyEntity saved = companyRepository.save(entity);

        // when
        CompanyDTO result = companyService.getCompanyById(saved.getId());

        // then
        assertEquals(saved.getId(), result.getId());
        assertEquals("Acme", result.getName());
        assertEquals(new BigDecimal("500.00"), result.getBudget());
    }
}
