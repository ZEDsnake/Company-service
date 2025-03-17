package com.zed.company_service.controller;

import com.zed.company_service.entity.CompanyEntity;
import com.zed.company_service.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/companies")
public class CompanyController {
    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public CompanyEntity createCompany(@RequestBody CompanyEntity company) {
        return companyService.createCompany(company);
    }

    @GetMapping("/{id}")
    public Optional<CompanyEntity> getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id);
    }
}
