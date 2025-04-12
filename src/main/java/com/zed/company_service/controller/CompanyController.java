package com.zed.company_service.controller;

import com.zed.company_service.dto.CompanyDTO;
import com.zed.company_service.dto.CompanyEmployeesDTO;
import com.zed.company_service.dto.CreateCompanyDTO;
import com.zed.company_service.dto.UpdateCompanyDTO;
import com.zed.company_service.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyDTO addCompany(@Valid @RequestBody CreateCompanyDTO createCompanyDTO) {
        log.info("POST request received: \"/companies\" with body: {}", createCompanyDTO);
        return companyService.addCompany(createCompanyDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyDTO getCompanyById(@PathVariable @Min(value = 1, message = "ID must be a positive number and not less than 1") Long id) {
        log.info("GET request received: \"/companies/{}\"", id);
        return companyService.getCompanyById(id);
    }

    @GetMapping("/{id}/employees")
    @ResponseStatus(HttpStatus.OK)
    public CompanyEmployeesDTO getCompanyWithEmployees(@PathVariable @Min(value = 1, message = "ID must be a positive number and not less than 1") Long id) {
        log.info("GET request received: \"/companies/{}/employees\"", id);
        return companyService.getCompanyWithEmployees(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CompanyDTO updateCompanyDTO(@PathVariable @Min(value = 1, message = "ID must be a positive number and not less than 1") Long id,
                                       @Valid @RequestBody UpdateCompanyDTO updateCompanyDTO) {
        log.info("Put request received: \"/companies/{}\" with body: {}", id, updateCompanyDTO);
        return companyService.updateCompany(id, updateCompanyDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompany(@PathVariable @Min(value = 1, message = "ID must be a positive number and not less than 1") Long id) {
        log.info("DELETE request received: \"/companies/{}\"", id);
        companyService.deleteCompany(id);
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompanyDTO> getCompanies(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be greater than or equal to 1") int size) {
        log.info("GET request received: \"/companies\" with pagination: page={}, size={}", page, size);
        return companyService.getCompanies(page, size);
    }
}


