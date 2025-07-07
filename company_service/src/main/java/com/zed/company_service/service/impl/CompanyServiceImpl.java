package com.zed.company_service.service.impl;

import com.zed.company_service.dto.CreateCompanyDto;
import com.zed.company_service.dto.PatchCompanyDto;
import com.zed.company_service.dto.UpdateCompanyDto;
import com.zed.company_service.dto.CompanyResponseDto;
import com.zed.company_service.dto.PagedCompanyResponseDto;
import com.zed.company_service.dto.UserInfoDto;
import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.exception.AlreadyExistsException;
import com.zed.company_service.exception.NotFoundException;
import com.zed.company_service.feign.UserClient;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserClient userClient;

    @Override
    public CompanyResponseDto createCompany(CreateCompanyDto dto) {
        ensureCompanyNameIsUnique(dto.getName());
        validateEmployeeIds(dto.getEmployeeIds());
        CompanyEntity entity = companyMapper.toCompanyEntity(dto);
        CompanyEntity saved = companyRepository.save(entity);
        syncEmployeesWithUserService(saved.getId(), dto.getEmployeeIds());
        List<UserInfoDto> employees = fetchEmployees(dto.getEmployeeIds());
        CompanyResponseDto result = companyMapper.toCompanyResponseDto(saved, employees);
        log.info("Created company with id: {}", result.getId());
        return result;
    }

    @Override
    public CompanyResponseDto updateCompany(Long id, UpdateCompanyDto dto) {
        CompanyEntity entity = getCompanyEntityById(id);
        validateNameChange(entity.getName(), dto.getName());
        companyMapper.updateEntityFromUpdateDto(dto, entity);
        CompanyEntity updated = companyRepository.save(entity);
        List<UserInfoDto> employees = fetchEmployees(entity.getEmployeeIds());
        CompanyResponseDto result = companyMapper.toCompanyResponseDto(updated, employees);
        log.info("Updated company with id: {}", result.getId());
        return result;
    }

    @Override
    public CompanyResponseDto patchCompany(Long id, PatchCompanyDto dto) {
        CompanyEntity entity = getCompanyEntityById(id);
        validatePatchNameChange(entity.getName(), dto.getName());
        companyMapper.updateEntityFromPatchDto(dto, entity);
        CompanyEntity patched = companyRepository.save(entity);
        List<UserInfoDto> employees = fetchEmployees(patched.getEmployeeIds());
        CompanyResponseDto result = companyMapper.toCompanyResponseDto(patched, employees);
        log.info("Patched company with id: {}", result.getId());
        return result;
    }

    @Override
    public void deleteCompany(Long id) {
        checkCompanyExists(id);
        userClient.deleteUsersByCompanyId(id);
        companyRepository.deleteById(id);
        log.info("Deleted company with id: {}", id);
    }

    @Override
    public CompanyResponseDto getCompanyById(Long id) {
        CompanyEntity entity = getCompanyEntityById(id);
        List<UserInfoDto> employees = fetchEmployees(entity.getEmployeeIds());
        CompanyResponseDto result = companyMapper.toCompanyResponseDto(entity, employees);
        log.info("Fetched company with id: {}", result.getId());
        return result;
    }

    @Override
    public PagedCompanyResponseDto getAllCompanies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CompanyEntity> pageResult = companyRepository.findAll(pageable);
        List<CompanyResponseDto> companies = pageResult.getContent().stream()
                .map(entity -> {
                    List<UserInfoDto> employees = fetchEmployees(entity.getEmployeeIds());
                    return companyMapper.toCompanyResponseDto(entity, employees);
                })
                .collect(Collectors.toList());
        PagedCompanyResponseDto result = new PagedCompanyResponseDto(
                companies,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements());
        log.info("Fetched {} companies", result.getCompanies().size());
        return result;
    }

    @Override
    public void addEmployeeToCompany(Long companyId, Long employeeId) {
        CompanyEntity company = getCompanyEntityById(companyId);
        validateEmployeeIds(List.of(employeeId));
        removeEmployeeFromPreviousCompany(employeeId, companyId);
        addEmployee(company, employeeId);
        companyRepository.save(company);
        userClient.updateUserCompany(employeeId, companyId);
        log.info("Added employee {} to company {}", employeeId, companyId);
    }

    @Override
    public void removeEmployeeFromCompany(Long companyId, Long employeeId) {
        CompanyEntity company = getCompanyEntityById(companyId);
        removeEmployee(company, employeeId);
        companyRepository.save(company);
        userClient.updateUserCompany(employeeId, null);
        log.info("Removed employee {} from company {}", employeeId, companyId);
    }

    private CompanyEntity getCompanyEntityById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with id " + id));
    }

    private void checkCompanyExists(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new NotFoundException("Company not found with id " + id);
        }
    }

    private void ensureCompanyNameIsUnique(String name) {
        if (companyRepository.existsByNameIgnoreCase(name)) {
            throw new AlreadyExistsException("Company name already exists: " + name);
        }
    }

    private void validateNameChange(String oldName, String newName) {
        if (newName != null && !oldName.equalsIgnoreCase(newName)) {
            ensureCompanyNameIsUnique(newName);
        }
    }

    private void validatePatchNameChange(String oldName, String newName) {
        validateNameChange(oldName, newName);
    }

    private List<UserInfoDto> fetchEmployees(List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<UserInfoDto> employees = userClient.getUsersByIds(employeeIds);
            if (employees.size() != employeeIds.size()) {
                Set<Long> foundIds = employees.stream()
                        .map(UserInfoDto::getId)
                        .collect(Collectors.toSet());
                List<Long> missingIds = employeeIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .toList();
                return employees;
            }
            return employees;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void validateEmployeeIds(List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return;
        }
        try {
            List<UserInfoDto> employees = userClient.getUsersByIds(employeeIds);
            if (employees.size() != employeeIds.size()) {
                Set<Long> foundIds = employees.stream()
                        .map(UserInfoDto::getId)
                        .collect(Collectors.toSet());
                List<Long> missingIds = employeeIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .toList();
                throw new NotFoundException("Employees not found with ids: " + missingIds);
            }
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate employee IDs", e);
        }
    }

    private void syncEmployeesWithUserService(Long companyId, List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return;
        }
        try {
            for (Long employeeId : employeeIds) {
                removeEmployeeFromPreviousCompany(employeeId, companyId);
                userClient.updateUserCompany(employeeId, companyId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync employees with company " + companyId, e);
        }
    }

    private void removeEmployeeFromPreviousCompany(Long employeeId, Long newCompanyId) {
        List<CompanyEntity> companies = companyRepository.findByEmployeeId(employeeId);
        for (CompanyEntity company : companies) {
            if (!company.getId().equals(newCompanyId)) {
                removeEmployee(company, employeeId);
                companyRepository.save(company);
            }
        }
    }

    private void addEmployee(CompanyEntity company, Long employeeId) {
        List<Long> employeeIds = company.getEmployeeIds();
        if (!employeeIds.contains(employeeId)) {
            employeeIds.add(employeeId);
        }
    }

    private void removeEmployee(CompanyEntity company, Long employeeId) {
        company.getEmployeeIds().remove(employeeId);
    }
}