package zed.company_service.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import zed.company_service.dto.CompanyDTO;
import zed.company_service.dto.CreateCompanyDTO;
import zed.company_service.dto.UpdateCompanyDTO;
import zed.company_service.dto.UserInfoDTO;
import zed.company_service.entity.CompanyEntity;
import zed.company_service.exception.AlreadyExistsException;
import zed.company_service.exception.NotFoundException;
import zed.company_service.feign.UserClient;
import zed.company_service.mapper.CompanyMapper;
import zed.company_service.repository.CompanyRepository;
import zed.company_service.service.impl.CompanyServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplUnitTest {

    @Mock
    private UserClient userClient;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private CompanyEntity companyEntity;
    private CompanyDTO companyDTO;
    private CreateCompanyDTO createCompanyDTO;
    private UpdateCompanyDTO updateCompanyDTO;

    private UserInfoDTO userInfo1, userInfo2;

    @BeforeEach
    void setup() {
        companyEntity = new CompanyEntity();
        companyEntity.setId(1L);
        companyEntity.setName("Test Company");
        companyEntity.setBudget(BigDecimal.valueOf(1000));

        companyDTO = new CompanyDTO(1L, "Test Company", BigDecimal.valueOf(1000));

        createCompanyDTO = new CreateCompanyDTO("Test Company", BigDecimal.valueOf(1000));
        updateCompanyDTO = new UpdateCompanyDTO("Updated Company", BigDecimal.valueOf(2000));

        userInfo1 = new UserInfoDTO("John", "Doe", "+72345678908");
        userInfo2 = new UserInfoDTO("Jane", "Smith", "+78765432103");
    }

    @Test
    void addCompany_ShouldSaveAndReturnCompanyDTO() {
        when(companyRepository.existsByName(createCompanyDTO.getName())).thenReturn(false);
        when(companyMapper.toCompanyEntity(createCompanyDTO)).thenReturn(companyEntity);
        when(companyRepository.save(companyEntity)).thenReturn(companyEntity);
        when(companyMapper.toCompanyDTO(companyEntity)).thenReturn(companyDTO);

        CompanyDTO result = companyService.addCompany(createCompanyDTO);

        assertThat(result).isEqualTo(companyDTO);
        verify(companyRepository).existsByName(createCompanyDTO.getName());
        verify(companyMapper).toCompanyEntity(createCompanyDTO);
        verify(companyRepository).save(companyEntity);
        verify(companyMapper).toCompanyDTO(companyEntity);
    }

    @Test
    void addCompany_ShouldThrowAlreadyExistsException_WhenNameExists() {
        when(companyRepository.existsByName(createCompanyDTO.getName())).thenReturn(true);

        assertThatThrownBy(() -> companyService.addCompany(createCompanyDTO))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Company with name \"" + createCompanyDTO.getName() + "\" already exists");

        verify(companyRepository).existsByName(createCompanyDTO.getName());
        verify(companyRepository, never()).save(any());
        verify(companyMapper, never()).toCompanyEntity(any());
    }

    @Test
    void getCompanyById_ShouldReturnCompanyDTO_WhenCompanyExists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(companyEntity));
        when(companyMapper.toCompanyDTO(companyEntity)).thenReturn(companyDTO);

        CompanyDTO result = companyService.getCompanyById(1L);

        assertThat(result).isEqualTo(companyDTO);
        verify(companyRepository).findById(1L);
        verify(companyMapper).toCompanyDTO(companyEntity);
    }

    @Test
    void getCompanyById_ShouldThrowNotFoundException_WhenCompanyNotFound() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompanyById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found with id: 99");

        verify(companyRepository).findById(99L);
        verify(companyMapper, never()).toCompanyDTO(any());
    }

    @Test
    void updateCompany_ShouldCallUpdateEntityFromDTO() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(companyEntity));
        when(companyRepository.existsByName(updateCompanyDTO.getName())).thenReturn(false);

        companyService.updateCompany(1L, updateCompanyDTO);

        verify(companyMapper).updateEntityFromDTO(updateCompanyDTO, companyEntity);
    }

    @Test
    void updateCompany_ShouldUpdateAndReturnUpdatedCompanyDTO() {
        Long companyId = 1L;
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(companyEntity));
        when(companyRepository.existsByName(updateCompanyDTO.getName())).thenReturn(false);
        doNothing().when(companyMapper).updateEntityFromDTO(updateCompanyDTO, companyEntity);
        when(companyRepository.save(companyEntity)).thenReturn(companyEntity);

        CompanyDTO updatedDTO = new CompanyDTO(companyId, updateCompanyDTO.getName(), updateCompanyDTO.getBudget());
        when(companyMapper.toCompanyDTO(companyEntity)).thenReturn(updatedDTO);

        CompanyDTO result = companyService.updateCompany(companyId, updateCompanyDTO);

        assertThat(result.getName()).isEqualTo(updateCompanyDTO.getName());
        assertThat(result.getBudget()).isEqualTo(updateCompanyDTO.getBudget());
        assertThat(result.getId()).isEqualTo(companyId);

        verify(companyRepository).findById(companyId);
        verify(companyRepository).existsByName(updateCompanyDTO.getName());
        verify(companyMapper).updateEntityFromDTO(updateCompanyDTO, companyEntity);
        verify(companyRepository).save(companyEntity);
        verify(companyMapper).toCompanyDTO(companyEntity);
    }

    @Test
    void updateCompany_ShouldThrowAlreadyExistsException_WhenNewNameExists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(companyEntity));
        when(companyRepository.existsByName(updateCompanyDTO.getName())).thenReturn(true);

        assertThatThrownBy(() -> companyService.updateCompany(1L, updateCompanyDTO))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessageContaining("Company with name \"" + updateCompanyDTO.getName() + "\" already exists");

        verify(companyRepository).findById(1L);
        verify(companyRepository).existsByName(updateCompanyDTO.getName());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void updateCompany_ShouldThrowNotFoundException_WhenCompanyNotFound() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.updateCompany(99L, updateCompanyDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found with id: 99");

        verify(companyRepository).findById(99L);
        verify(companyRepository, never()).existsByName(any());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void deleteCompany_ShouldDeleteCompanyAndCallUserClient() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(companyEntity));

        companyService.deleteCompany(1L);

        verify(userClient).deleteUsersByCompanyId(1L);
        verify(companyRepository).delete(companyEntity);
    }

    @Test
    void deleteCompany_ShouldThrowNotFoundException_WhenCompanyNotFound() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.deleteCompany(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found with id: 99");

        verify(companyRepository).findById(99L);
        verify(userClient, never()).deleteUsersByCompanyId(anyLong());
        verify(companyRepository, never()).delete(any());
    }

    @Test
    void getCompanies_ShouldReturnListOfCompanyDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CompanyEntity> page = new PageImpl<>(List.of(companyEntity), pageable, 1);

        when(companyRepository.findAll(pageable)).thenReturn(page);
        when(companyMapper.toCompanyDTOList(page.getContent())).thenReturn(List.of(companyDTO));

        List<CompanyDTO> result = companyService.getCompanies(0, 10);

        assertThat(result).hasSize(1).containsExactly(companyDTO);
        verify(companyRepository).findAll(pageable);
        verify(companyMapper).toCompanyDTOList(page.getContent());
    }

    @Test
    void getCompanyEmployees_ShouldReturnListOfUserInfoDTO() {
        List<UserInfoDTO> employees = List.of(userInfo1, userInfo2);

        when(userClient.getUsersByCompanyId(1L, 0, 10)).thenReturn(employees);

        List<UserInfoDTO> result = companyService.getCompanyEmployees(1L, 0, 10);

        assertThat(result).isEqualTo(employees);
        verify(userClient).getUsersByCompanyId(1L, 0, 10);
    }
}



