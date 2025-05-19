package com.zed.company_service.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.dto.UserInfoDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.feign.UserClient;
import com.zed.company_service.repository.CompanyRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
class CompanyServiceImplIntegrationTest {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @MockBean
    private UserClient userClient; // Feign клиент мокаем?

    private Long testCompanyId;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();

        CreateCompanyDTO dto = new CreateCompanyDTO("Test Company", new BigDecimal("500000"));
        CompanyDTO created = companyService.addCompany(dto);
        testCompanyId = created.getId();
    }

    @Test
    void addCompany_ShouldPersistCompany() {
        CreateCompanyDTO dto = new CreateCompanyDTO("New Company", BigDecimal.valueOf(3000));
        CompanyDTO result = companyService.addCompany(dto);

        assertThat(result.getName()).isEqualTo("New Company");
        assertThat(result.getBudget()).isEqualTo(BigDecimal.valueOf(3000));

        Optional<CompanyEntity> entity = companyRepository.findById(result.getId());
        assertThat(entity).isPresent();
        assertThat(entity.get().getName()).isEqualTo("New Company");
    }

    @Test
    void getCompanyById_ShouldReturnDTO() {
        CompanyDTO result = companyService.getCompanyById(testCompanyId);

        assertThat(result.getName()).isEqualTo("Test Company");
        assertThat(result.getBudget()).isEqualTo(new BigDecimal("500000"));
    }

    @Test
    void updateCompany_ShouldChangeData() {
        UpdateCompanyDTO updateDTO = new UpdateCompanyDTO("Updated Company", BigDecimal.valueOf(5555));

        CompanyDTO result = companyService.updateCompany(testCompanyId, updateDTO);

        assertThat(result.getName()).isEqualTo("Updated Company");
        assertThat(result.getBudget()).isEqualTo(BigDecimal.valueOf(5555));
    }

    @Test
    void deleteCompany_ShouldRemoveFromDB() {
        companyService.deleteCompany(testCompanyId);

        Optional<CompanyEntity> entity = companyRepository.findById(testCompanyId);
        assertThat(entity).isEmpty();

        verify(userClient).deleteUsersByCompanyId(testCompanyId); // проверяем вызов Feign клиента
    }

    @Test
    void getCompanies_ShouldReturnPagedDTOs() {
        List<CompanyDTO> companies = companyService.getCompanies(0, 10);

        assertThat(companies).isNotEmpty();
        assertThat(companies.get(0).getName()).isEqualTo("Test Company");
    }

    @Test
    void getCompanyEmployees_ShouldReturnUserInfoList() {
        List<UserInfoDTO> mockUsers = List.of(
                new UserInfoDTO("John", "Doe", "+72345678902"),
                new UserInfoDTO("Jane", "Smith", "+79876543217")
        );

        when(userClient.getUsersByCompanyId(testCompanyId, 0, 10)).thenReturn(mockUsers);

        List<UserInfoDTO> result = companyService.getCompanyEmployees(testCompanyId, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserInfoDTO::getFirstName).contains("John", "Jane");
    }
}
