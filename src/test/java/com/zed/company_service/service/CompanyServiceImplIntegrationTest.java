package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.entity.User;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.repository.UserRepository;
import com.zed.company_service.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    private UserRepository userRepository;

    private CompanyEntity savedCompany;
    private User savedUser;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();
        userRepository.deleteAll();

        CompanyEntity company = new CompanyEntity();
        company.setName("Test Company");
        company.setBudget(new BigDecimal("100000.00"));
        savedCompany = companyRepository.save(company);

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("+79123456789");
        user.setCompany(savedCompany);
        savedUser = userRepository.save(user);

        savedCompany.getEmployees().add(savedUser);
        companyRepository.save(savedCompany);
    }

    @Test
    void addCompany_ShouldReturnDtoWithGeneratedId() {
        CreateCompanyDTO dto = new CreateCompanyDTO("New Company", new BigDecimal("500000.00"));

        CompanyDTO result = companyService.addCompany(dto);

        assertNotNull(result.getId());
        assertEquals("New Company", result.getName());
        assertEquals(0, new BigDecimal("500000.00").compareTo(result.getBudget()));
    }

    @Test
    void getCompanyById_ShouldReturnCorrectDto() {
        CompanyDTO result = companyService.getCompanyById(savedCompany.getId());

        assertEquals(savedCompany.getId(), result.getId());
        assertEquals(savedCompany.getName(), result.getName());
        assertEquals(0, savedCompany.getBudget().compareTo(result.getBudget()));
    }

    @Test
    void getCompanyWithEmployees_ShouldReturnDtoWithUsers() {
        CompanyEmployeesDTO result = companyService.getCompanyWithEmployees(savedCompany.getId());

        assertEquals(savedCompany.getId(), result.getId());
        assertEquals(1, result.getEmployees().size());
        assertEquals("John", result.getEmployees().get(0).getFirstName());
    }

    @Test
    void updateCompany_ShouldUpdateOnlyProvidedFields() {
        UpdateCompanyDTO dto = new UpdateCompanyDTO();
        dto.setName("Updated Name");
        dto.setBudget(new BigDecimal("200000.00"));

        CompanyDTO result = companyService.updateCompany(savedCompany.getId(), dto);

        assertEquals("Updated Name", result.getName());
        assertEquals(0, new BigDecimal("200000.00").compareTo(result.getBudget()));
    }

    @Test
    void deleteCompany_ShouldRemoveCompanyAndUsers() {
        companyService.deleteCompany(savedCompany.getId());

        assertFalse(companyRepository.existsById(savedCompany.getId()));
        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void getCompanies_ShouldReturnPaginatedResults() {
        CompanyEntity company2 = new CompanyEntity();
        company2.setName("Company 2");
        company2.setBudget(new BigDecimal("200000.00"));
        companyRepository.save(company2);

        CompanyEntity company3 = new CompanyEntity();
        company3.setName("Company 3");
        company3.setBudget(new BigDecimal("300000.00"));
        companyRepository.save(company3);

        List<CompanyDTO> page1 = companyService.getCompanies(0, 2);
        List<CompanyDTO> page2 = companyService.getCompanies(1, 2);

        assertEquals(2, page1.size());
        assertEquals(1, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    void getCompanyById_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () ->
                companyService.getCompanyById(999L));
    }

    @Test
    void updateCompany_ShouldThrowNotFoundException() {
        UpdateCompanyDTO dto = new UpdateCompanyDTO();
        dto.setName("Non-existent");

        assertThrows(NotFoundException.class, () ->
                companyService.updateCompany(999L, dto));
    }
}