package com.zed.company_service.service;

import com.zed.company_service.dto.CompanyResponseDto;
import com.zed.company_service.dto.CreateCompanyDto;
import com.zed.company_service.dto.PagedCompanyResponseDto;
import com.zed.company_service.dto.PatchCompanyDto;
import com.zed.company_service.dto.UpdateCompanyDto;
import com.zed.company_service.dto.UserInfoDto;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.feign.UserClient;
import com.zed.company_service.mapper.CompanyMapper;
import com.zed.company_service.repository.CompanyRepository;
import com.zed.company_service.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplUnitTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private CompanyEntity exampleEntity;
    private CreateCompanyDto createDto;
    private UpdateCompanyDto updateDto;
    private PatchCompanyDto patchDto;
    private UserInfoDto user1;
    private UserInfoDto user2;

    @BeforeEach
    void setUp() {
        exampleEntity = new CompanyEntity();
        exampleEntity.setId(1L);
        exampleEntity.setName("TestCompany");
        exampleEntity.setBudget(new BigDecimal("1000"));
        exampleEntity.setEmployeeIds(new ArrayList<>(List.of(1L, 2L)));

        createDto = new CreateCompanyDto("TestCompany", new BigDecimal("1000"), List.of(1L, 2L));
        updateDto = new UpdateCompanyDto("UpdatedCompany", new BigDecimal("2000"), List.of(1L));
        patchDto = new PatchCompanyDto();
        patchDto.setName("PatchedCompany");
        patchDto.setBudget(new BigDecimal("3000"));
        patchDto.setEmployeeIds(List.of(2L));

        user1 = new UserInfoDto(1L, "John", "Doe", "+123456789");
        user2 = new UserInfoDto(2L, "Jane", "Doe", "+987654321");
    }

    @Test
    void createCompany_Success() {
        when(companyRepository.existsByNameIgnoreCase(createDto.getName())).thenReturn(false);
        when(userClient.getUsersByIds(anyList())).thenReturn(List.of(user1, user2));
        when(companyMapper.toCompanyEntity(createDto)).thenReturn(exampleEntity);
        when(companyRepository.save(exampleEntity)).thenReturn(exampleEntity);
        when(companyMapper.toCompanyResponseDto(exampleEntity, List.of(user1, user2)))
                .thenReturn(new CompanyResponseDto(1L, "TestCompany", new BigDecimal("1000"), List.of(user1, user2)));

        CompanyResponseDto response = companyService.createCompany(createDto);

        assertEquals("TestCompany", response.getName());
        verify(companyRepository).existsByNameIgnoreCase(createDto.getName());
        verify(userClient, times(2)).getUsersByIds(anyList());  // ожидаем 2 вызова
        verify(companyRepository).save(exampleEntity);
        verify(userClient, times(createDto.getEmployeeIds().size())).updateUserCompany(anyLong(), eq(1L));
        verify(companyMapper).toCompanyResponseDto(exampleEntity, List.of(user1, user2));
    }


    @Test
    void createCompany_AlreadyExistsException() {
        when(companyRepository.existsByNameIgnoreCase(createDto.getName())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> companyService.createCompany(createDto));
        verify(companyRepository).existsByNameIgnoreCase(createDto.getName());
        verifyNoMoreInteractions(userClient, companyRepository, companyMapper);
    }

    @Test
    void createCompany_EmployeeNotFoundException() {
        when(companyRepository.existsByNameIgnoreCase(createDto.getName())).thenReturn(false);
        when(userClient.getUsersByIds(createDto.getEmployeeIds())).thenReturn(List.of(user1)); // Один сотрудник отсутствует

        NotFoundException ex = assertThrows(NotFoundException.class, () -> companyService.createCompany(createDto));
        assertTrue(ex.getMessage().contains("Employees not found"));
    }

    @Test
    void updateCompany_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(exampleEntity));
        when(companyRepository.existsByNameIgnoreCase(updateDto.getName())).thenReturn(false);
        doNothing().when(companyMapper).updateEntityFromUpdateDto(updateDto, exampleEntity);
        when(companyRepository.save(exampleEntity)).thenReturn(exampleEntity);
        when(userClient.getUsersByIds(exampleEntity.getEmployeeIds())).thenReturn(List.of(user1));
        when(companyMapper.toCompanyResponseDto(exampleEntity, List.of(user1)))
                .thenReturn(new CompanyResponseDto(1L, "UpdatedCompany", new BigDecimal("2000"), List.of(user1)));

        CompanyResponseDto response = companyService.updateCompany(1L, updateDto);

        assertEquals("UpdatedCompany", response.getName());
        verify(companyRepository).findById(1L);
        verify(companyRepository).existsByNameIgnoreCase(updateDto.getName());
        verify(companyMapper).updateEntityFromUpdateDto(updateDto, exampleEntity);
        verify(companyRepository).save(exampleEntity);
        verify(userClient).getUsersByIds(exampleEntity.getEmployeeIds());
    }

    @Test
    void updateCompany_NotFoundException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> companyService.updateCompany(1L, updateDto));
        verify(companyRepository).findById(1L);
    }

    @Test
    void patchCompany_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(exampleEntity));
        when(companyRepository.existsByNameIgnoreCase(patchDto.getName())).thenReturn(false);
        doNothing().when(companyMapper).updateEntityFromPatchDto(patchDto, exampleEntity);
        when(companyRepository.save(exampleEntity)).thenReturn(exampleEntity);
        when(userClient.getUsersByIds(exampleEntity.getEmployeeIds())).thenReturn(List.of(user2));
        when(companyMapper.toCompanyResponseDto(exampleEntity, List.of(user2)))
                .thenReturn(new CompanyResponseDto(1L, "PatchedCompany", new BigDecimal("3000"), List.of(user2)));

        CompanyResponseDto response = companyService.patchCompany(1L, patchDto);

        assertEquals("PatchedCompany", response.getName());
        verify(companyRepository).findById(1L);
        verify(companyRepository).existsByNameIgnoreCase(patchDto.getName());
        verify(companyMapper).updateEntityFromPatchDto(patchDto, exampleEntity);
        verify(companyRepository).save(exampleEntity);
        verify(userClient).getUsersByIds(exampleEntity.getEmployeeIds());
    }

    @Test
    void patchCompany_NotFoundException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> companyService.patchCompany(1L, patchDto));
        verify(companyRepository).findById(1L);
    }

    @Test
    void deleteCompany_Success() {
        when(companyRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userClient).deleteUsersByCompanyId(1L);
        doNothing().when(companyRepository).deleteById(1L);

        companyService.deleteCompany(1L);

        verify(companyRepository).existsById(1L);
        verify(userClient).deleteUsersByCompanyId(1L);
        verify(companyRepository).deleteById(1L);
    }

    @Test
    void deleteCompany_NotFoundException() {
        when(companyRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> companyService.deleteCompany(1L));
        verify(companyRepository).existsById(1L);
        verifyNoMoreInteractions(userClient, companyRepository);
    }

    @Test
    void getCompanyById_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(exampleEntity));
        when(userClient.getUsersByIds(exampleEntity.getEmployeeIds())).thenReturn(List.of(user1, user2));
        when(companyMapper.toCompanyResponseDto(exampleEntity, List.of(user1, user2)))
                .thenReturn(new CompanyResponseDto(1L, "TestCompany", new BigDecimal("1000"), List.of(user1, user2)));

        CompanyResponseDto response = companyService.getCompanyById(1L);

        assertEquals(1L, response.getId());
        verify(companyRepository).findById(1L);
        verify(userClient).getUsersByIds(exampleEntity.getEmployeeIds());
        verify(companyMapper).toCompanyResponseDto(exampleEntity, List.of(user1, user2));
    }

    @Test
    void getCompanyById_NotFoundException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.getCompanyById(1L));
        verify(companyRepository).findById(1L);
    }

    @Test
    void getAllCompanies_Success() {
        Page<CompanyEntity> page = new PageImpl<>(List.of(exampleEntity));
        when(companyRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(userClient.getUsersByIds(exampleEntity.getEmployeeIds())).thenReturn(List.of(user1, user2));
        when(companyMapper.toCompanyResponseDto(exampleEntity, List.of(user1, user2)))
                .thenReturn(new CompanyResponseDto(1L, "TestCompany", new BigDecimal("1000"), List.of(user1, user2)));

        PagedCompanyResponseDto response = companyService.getAllCompanies(0, 10);

        assertEquals(1, response.getCompanies().size());
        verify(companyRepository).findAll(any(Pageable.class));
        verify(userClient).getUsersByIds(exampleEntity.getEmployeeIds());
        verify(companyMapper).toCompanyResponseDto(exampleEntity, List.of(user1, user2));
    }

    @Test
    void addEmployeeToCompany_Success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(exampleEntity));
        when(userClient.getUsersByIds(List.of(3L))).thenReturn(List.of(new UserInfoDto(3L, "Alice", "Wonder", "+111111")));
        when(companyRepository.findByEmployeeId(3L)).thenReturn(List.of());
        when(companyRepository.save(exampleEntity)).thenReturn(exampleEntity);

        companyService.addEmployeeToCompany(1L, 3L);

        verify(companyRepository).findById(1L);
        verify(userClient).getUsersByIds(List.of(3L));
        verify(companyRepository).findByEmployeeId(3L);
        verify(companyRepository).save(exampleEntity);
        verify(userClient).updateUserCompany(3L, 1L);
    }

    @Test
    void addEmployeeToCompany_EmployeeNotFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(exampleEntity));
        when(userClient.getUsersByIds(List.of(3L))).thenReturn(List.of()); // пусто - сотрудник не найден

        assertThrows(NotFoundException.class, () -> companyService.addEmployeeToCompany(1L, 3L));

        verify(companyRepository).findById(1L);
        verify(userClient).getUsersByIds(List.of(3L));
    }

    @Test
    void removeEmployeeFromCompany_Success() {
        exampleEntity.setEmployeeIds(new ArrayList<>(List.of(1L, 3L)));
        when(companyRepository.findById(1L)).thenReturn(Optional.of(exampleEntity));
        when(companyRepository.save(exampleEntity)).thenReturn(exampleEntity);

        companyService.removeEmployeeFromCompany(1L, 3L);

        assertFalse(exampleEntity.getEmployeeIds().contains(3L));
        verify(companyRepository).findById(1L);
        verify(companyRepository).save(exampleEntity);
        verify(userClient).updateUserCompany(3L, null);
    }

    @Test
    void removeEmployeeFromCompany_NotFoundException() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> companyService.removeEmployeeFromCompany(1L, 3L));
        verify(companyRepository).findById(1L);
    }
}