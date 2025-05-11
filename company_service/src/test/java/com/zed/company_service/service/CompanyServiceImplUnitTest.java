//package com.zed.company_service.service;
//
//import com.zed.company_service.dto.CompanyDTO;
//import com.zed.company_service.dto.CreateCompanyDTO;
//import com.zed.company_service.dto.UpdateCompanyDTO;
//import com.zed.company_service.dto.UserInfoDTO;
//import com.zed.company_service.entity.CompanyEntity;
//import com.zed.company_service.entity.User;
//import com.zed.company_service.exception.NotFoundException;
//import com.zed.company_service.mapper.CompanyMapper;
//import com.zed.company_service.repository.CompanyRepository;
//import com.zed.company_service.service.impl.CompanyServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.math.BigDecimal;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class CompanyServiceImplUnitTest {
//
//    @Mock
//    private CompanyRepository repository;
//
//    @Mock
//    private CompanyMapper mapper;
//
//    @InjectMocks
//    private CompanyServiceImpl service;
//
//    private CompanyEntity companyEntity;
//    private CompanyDTO companyDTO;
//    private CreateCompanyDTO createCompanyDTO;
//    private UpdateCompanyDTO updateCompanyDTO;
//    private CompanyEmployeesDTO companyEmployeesDTO;
//    private User user1, user2;
//    private UserInfoDTO userInfoDTO1, userInfoDTO2;
//
//    @BeforeEach
//    void setUp() {
//        companyEntity = new CompanyEntity();
//        companyEntity.setId(1L);
//        companyEntity.setName("Test Company");
//        companyEntity.setBudget(BigDecimal.valueOf(1000));
//
//        user1 = new User(1L, "John", "Doe", "+1234567890", companyEntity);
//        user2 = new User(2L, "Jane", "Smith", "+9876543210", companyEntity);
//
//        companyEntity.setEmployees(List.of(user1, user2));
//
//        companyDTO = new CompanyDTO(1L, "Test Company", BigDecimal.valueOf(1000));
//        createCompanyDTO = new CreateCompanyDTO("Test Company", BigDecimal.valueOf(1000));
//        updateCompanyDTO = new UpdateCompanyDTO("Updated Company", BigDecimal.valueOf(2000));
//
//        userInfoDTO1 = new UserInfoDTO("John", "Doe", "+1234567890");
//        userInfoDTO2 = new UserInfoDTO("Jane", "Smith", "+9876543210");
//        companyEmployeesDTO = new CompanyEmployeesDTO(1L, "Test Company", BigDecimal.valueOf(1000), List.of(userInfoDTO1, userInfoDTO2));
//    }
//
//        @Test
//        void addCompany_ShouldReturnSavedCompany_WhenValidInput() {
//            when(mapper.toCompanyEntity(createCompanyDTO)).thenReturn(companyEntity);
//            when(repository.save(companyEntity)).thenReturn(companyEntity);
//            when(mapper.toCompanyDTO(companyEntity)).thenReturn(companyDTO);
//
//            CompanyDTO result = service.addCompany(createCompanyDTO);
//
//            assertThat(result).isEqualTo(companyDTO);
//            verify(repository).save(companyEntity);
//            verify(mapper).toCompanyEntity(createCompanyDTO);
//        }
//
//        @Test
//        void addCompany_ShouldThrowException_WhenRepositoryFails() {
//            when(mapper.toCompanyEntity(createCompanyDTO)).thenReturn(companyEntity);
//            when(repository.save(companyEntity)).thenThrow(new DataAccessException("DB error") {});
//
//            assertThatThrownBy(() -> service.addCompany(createCompanyDTO))
//                    .isInstanceOf(DataAccessException.class)
//                    .hasMessageContaining("DB error");
//        }
//
//        @Test
//        void getCompanyById_ShouldReturnCompany_WhenExists() {
//            when(repository.findById(1L)).thenReturn(Optional.of(companyEntity));
//            when(mapper.toCompanyDTO(companyEntity)).thenReturn(companyDTO);
//
//            CompanyDTO result = service.getCompanyById(1L);
//
//            assertThat(result).isEqualTo(companyDTO);
//            verify(repository).findById(1L);
//        }
//
//        @Test
//        void getCompanyById_ShouldThrowNotFoundException_WhenNotExists() {
//            Long invalidId = 999L;
//            when(repository.findById(invalidId)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> service.getCompanyById(invalidId))
//                    .isInstanceOf(NotFoundException.class)
//                    .hasMessageContaining("Company not found with id: " + invalidId);
//        }
//
//        @Test
//        void getCompanyWithEmployees_ShouldReturnCompanyWithEmployees_WhenExists() {
//            when(repository.findById(1L)).thenReturn(Optional.of(companyEntity));
//            when(mapper.toCompanyEmployeesDTO(companyEntity)).thenReturn(companyEmployeesDTO);
//
//            CompanyEmployeesDTO result = service.getCompanyWithEmployees(1L);
//
//            assertThat(result).isEqualTo(companyEmployeesDTO);
//            assertThat(result.getEmployees())
//                    .hasSize(2)
//                    .containsExactly(userInfoDTO1, userInfoDTO2);
//            verify(repository).findById(1L);
//        }
//
//        @Test
//        void getCompanyWithEmployees_ShouldThrowNotFoundException_WhenNotExists() {
//            Long invalidId = 999L;
//            when(repository.findById(invalidId)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> service.getCompanyWithEmployees(invalidId))
//                    .isInstanceOf(NotFoundException.class)
//                    .hasMessageContaining("Company not found with id: " + invalidId);
//        }
//
//        // ========== updateCompany Tests ==========
//        @Test
//        void updateCompany_ShouldUpdateAndReturnCompany_WhenValidInput() {
//            CompanyEntity updatedEntity = new CompanyEntity();
//            updatedEntity.setId(1L);
//            updatedEntity.setName("Updated Company");
//            updatedEntity.setBudget(BigDecimal.valueOf(2000));
//
//            CompanyDTO updatedDto = new CompanyDTO(1L, "Updated Company", BigDecimal.valueOf(2000));
//
//            when(repository.findById(1L)).thenReturn(Optional.of(companyEntity));
//            when(repository.save(companyEntity)).thenReturn(updatedEntity);
//            when(mapper.toCompanyDTO(updatedEntity)).thenReturn(updatedDto);
//
//            CompanyDTO result = service.updateCompany(1L, updateCompanyDTO);
//
//            assertThat(result).isEqualTo(updatedDto);
//            verify(mapper).updateEntityFromDTO(updateCompanyDTO, companyEntity);
//            verify(repository).save(companyEntity);
//        }
//
//        @Test
//        void updateCompany_ShouldThrowNotFoundException_WhenNotExists() {
//            Long invalidId = 999L;
//            when(repository.findById(invalidId)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> service.updateCompany(invalidId, updateCompanyDTO))
//                    .isInstanceOf(NotFoundException.class)
//                    .hasMessageContaining("Company not found with id: " + invalidId);
//        }
//
//        @Test
//        void deleteCompany_ShouldDeleteCompany_WhenExists() {
//            when(repository.findById(1L)).thenReturn(Optional.of(companyEntity));
//
//            service.deleteCompany(1L);
//
//            verify(repository).delete(companyEntity);
//        }
//
//        @Test
//        void deleteCompany_ShouldThrowNotFoundException_WhenNotExists() {
//            Long invalidId = 999L;
//            when(repository.findById(invalidId)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> service.deleteCompany(invalidId))
//                    .isInstanceOf(NotFoundException.class)
//                    .hasMessageContaining("Company not found with id: " + invalidId);
//
//            verify(repository, never()).delete(any());
//        }
//
//        @Test
//        void getCompanies_ShouldReturnPageOfCompanies_WhenValidPageable() {
//            Pageable pageable = PageRequest.of(0, 10);
//            Page<CompanyEntity> page = new PageImpl<>(List.of(companyEntity), pageable, 1);
//
//            when(repository.findAll(pageable)).thenReturn(page);
//            when(mapper.toCompanyDTOList(List.of(companyEntity))).thenReturn(List.of(companyDTO));
//
//            List<CompanyDTO> result = service.getCompanies(0, 10);
//
//            assertThat(result)
//                    .hasSize(1)
//                    .containsExactly(companyDTO);
//        }
//
//        @Test
//        void getCompanies_ShouldReturnEmptyList_WhenNoCompanies() {
//            Pageable pageable = PageRequest.of(0, 10);
//            when(repository.findAll(pageable)).thenReturn(Page.empty());
//            when(mapper.toCompanyDTOList(anyList())).thenReturn(Collections.emptyList());
//
//            List<CompanyDTO> result = service.getCompanies(0, 10);
//
//            assertThat(result).isEmpty();
//        }
//
//        @Test
//        void getCompanies_ShouldThrowException_WhenInvalidPageable() {
//            assertThatThrownBy(() -> service.getCompanies(-1, 10))
//                    .isInstanceOf(IllegalArgumentException.class);
//
//            assertThatThrownBy(() -> service.getCompanies(0, 0))
//                    .isInstanceOf(IllegalArgumentException.class);
//        }
//    }
