package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.mapper.CompanyMapper;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplUnitTest {

    @Mock
    private CompanyRepository repository;

    @Mock
    private CompanyMapper mapper;

    @InjectMocks
    private CompanyServiceImpl service;

    private CompanyEntity entity;
    private CompanyDTO dto;
    private CreateCompanyDTO createDto;
    private UpdateCompanyDTO updateDto;

    @BeforeEach
    void setUp() {
        // Инициализация тестовых данных
        entity = new CompanyEntity(1L, "Test Company", BigDecimal.valueOf(1000));

        dto = new CompanyDTO();
        dto.setId(1L);
        dto.setName("Test Company");
        dto.setBudget(BigDecimal.valueOf(1000));

        createDto = new CreateCompanyDTO();
        createDto.setName("Test Company");
        createDto.setBudget(BigDecimal.valueOf(1000));

        updateDto = new UpdateCompanyDTO();
        updateDto.setName("Updated Company");
        updateDto.setBudget(BigDecimal.valueOf(2000));
    }

    // region AddCompany Tests
    @Test
    void addCompany_ShouldReturnSavedCompany_WhenValidInput() {
        when(mapper.toCompanyEntity(createDto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toCompanyDTO(entity)).thenReturn(dto);

        CompanyDTO result = service.addCompany(createDto);

        assertThat(result).isEqualTo(dto);
        verify(repository).save(entity);
        verify(mapper).toCompanyEntity(createDto);
    }

    @Test
    void addCompany_ShouldThrowException_WhenRepositoryFails() {
        when(mapper.toCompanyEntity(createDto)).thenReturn(entity);
        when(repository.save(entity)).thenThrow(new DataAccessException("DB error") {});

        assertThatThrownBy(() -> service.addCompany(createDto))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("DB error");
    }
    // endregion

    // region GetCompanyById Tests
    @Test
    void getCompanyById_ShouldReturnCompany_WhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toCompanyDTO(entity)).thenReturn(dto);

        CompanyDTO result = service.getCompanyById(1L);

        assertThat(result).isEqualTo(dto);
        verify(repository).findById(1L);
    }

    @Test
    void getCompanyById_ShouldThrowNotFoundException_WhenNotExists() {
        Long invalidId = 999L;
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCompanyById(invalidId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found with id: " + invalidId);
    }
    // endregion

    // region UpdateCompany Tests
    @Test
    void updateCompany_ShouldUpdateAndReturnCompany_WhenValidInput() {
        CompanyEntity updatedEntity = new CompanyEntity(1L, "Updated Company", BigDecimal.valueOf(2000));
        CompanyDTO updatedDto = new CompanyDTO();
        updatedDto.setId(1L);
        updatedDto.setName("Updated Company");
        updatedDto.setBudget(BigDecimal.valueOf(2000));

        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(updatedEntity);
        when(mapper.toCompanyDTO(updatedEntity)).thenReturn(updatedDto);

        CompanyDTO result = service.updateCompany(1L, updateDto);

        assertThat(result).isEqualTo(updatedDto);
        verify(mapper).updateEntityFromDTO(updateDto, entity);
        verify(repository).save(entity);
    }

    @Test
    void updateCompany_ShouldThrowNotFound_WhenCompanyNotExists() {
        Long invalidId = 999L;
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCompany(invalidId, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found with id: " + invalidId);
    }

    @Test
    void updateCompany_ShouldUpdateOnlyName_WhenPartialUpdate() {
        UpdateCompanyDTO partialUpdate = new UpdateCompanyDTO();
        partialUpdate.setName("New Name Only");
        CompanyEntity updatedEntity = new CompanyEntity();
        updatedEntity.setId(1L);
        updatedEntity.setName("New Name Only"); // Новое имя
        updatedEntity.setBudget(BigDecimal.valueOf(1000)); // Бюджет остался прежним
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(any(CompanyEntity.class))).thenReturn(updatedEntity);
        CompanyDTO expectedDto = new CompanyDTO();
        expectedDto.setId(1L);
        expectedDto.setName("New Name Only");
        expectedDto.setBudget(BigDecimal.valueOf(1000));
        when(mapper.toCompanyDTO(updatedEntity)).thenReturn(expectedDto);
        CompanyDTO result = service.updateCompany(1L, partialUpdate);

        assertThat(result.getName()).isEqualTo("New Name Only");
        assertThat(result.getBudget()).isEqualTo(BigDecimal.valueOf(1000));

        verify(mapper).updateEntityFromDTO(
                argThat(dto ->
                        dto.getName().equals("New Name Only") &&
                                dto.getBudget() == null
                ),
                any()
        );
        verify(repository).save(any(CompanyEntity.class));
    }
    // endregion

    // region DeleteCompany Tests
    @Test
    void deleteCompany_ShouldDeleteCompany_WhenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        service.deleteCompany(1L);

        verify(repository).delete(entity);
    }

    @Test
    void deleteCompany_ShouldThrowNotFound_WhenNotExists() {
        Long invalidId = 999L;
        when(repository.findById(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteCompany(invalidId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Company not found with id: " + invalidId);

        verify(repository, never()).delete(any());
    }
    // endregion

    // region GetCompanies Tests
    @Test
    void getCompanies_ShouldReturnPageOfCompanies_WhenValidPageable() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CompanyEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toCompanyDTOList(List.of(entity))).thenReturn(List.of(dto));

        List<CompanyDTO> result = service.getCompanies(0, 10);

        assertThat(result)
                .hasSize(1)
                .containsExactly(dto);
    }

    @Test
    void getCompanies_ShouldReturnEmptyList_WhenNoCompanies() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(pageable)).thenReturn(Page.empty());
        when(mapper.toCompanyDTOList(anyList())).thenReturn(Collections.emptyList());

        List<CompanyDTO> result = service.getCompanies(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getCompanies_ShouldThrowException_WhenInvalidPageable() {
        assertThatThrownBy(() -> service.getCompanies(-1, 10))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.getCompanies(0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
    // endregion
}
