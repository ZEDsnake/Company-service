package com.zed.company_service.controller;

import com.zed.company_service.dto.CompanyResponseDto;
import com.zed.company_service.dto.CreateCompanyDto;
import com.zed.company_service.dto.PagedCompanyResponseDto;
import com.zed.company_service.dto.PatchCompanyDto;
import com.zed.company_service.dto.UpdateCompanyDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.zed.company_service.service.CompanyService;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponseDto addCompany(
            @Valid
            @RequestBody CreateCompanyDto dto) {
        log.info("POST request to /companies with body: {}", dto);
        return companyService.createCompany(dto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponseDto updateCompany(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0") Long id,
            @Valid
            @RequestBody UpdateCompanyDto dto) {
        log.info("PUT request to /companies/{} with body: {}", id, dto);
        return companyService.updateCompany(id, dto);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponseDto patchCompany(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0") Long id,
            @Valid
            @RequestBody PatchCompanyDto dto) {
        log.info("PATCH request to /companies/{} with body: {}", id, dto);
        return companyService.patchCompany(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0") Long id) {
        log.info("DELETE request to /companies/{}", id);
        companyService.deleteCompany(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyResponseDto getCompanyById(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0") Long id) {
        log.info("GET request to /companies/{}", id);
        return companyService.getCompanyById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedCompanyResponseDto getCompanies(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be positive and greater than 0") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be positive and greater than 1") int size) {
        log.info("GET request to /companies with pagination page={} size={}", page, size);
        return companyService.getAllCompanies(page, size);
    }

    @PostMapping("/{companyId}/employees/add")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addEmployee(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0")Long companyId,
            @RequestParam
            @Min(value = 1, message = "Id must be positive and greater than 0")Long employeeId) {
        log.info("Add employee {} to company {}", employeeId, companyId);
        companyService.addEmployeeToCompany(companyId, employeeId);
    }

    @PostMapping("/{companyId}/employees/remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmployee(
            @PathVariable
            @Min(value = 1, message = "Id must be positive and greater than 0")Long companyId,
            @RequestParam
            @Min(value = 1, message = "Id must be positive and greater than 0")Long employeeId) {
        log.info("Remove employee {} from company {}", employeeId, companyId);
        companyService.removeEmployeeFromCompany(companyId, employeeId);
    }
}