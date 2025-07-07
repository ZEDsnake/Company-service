package com.zed.user_service.feign;

import com.zed.user_service.dto.CompanyInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "company-service", path = "/companies")
public interface CompanyClient {

    @GetMapping("/{id}")
    CompanyInfoDto getCompanyById(@PathVariable("id") Long id);

    @PostMapping("/{companyId}/employees/add")
    void addEmployee(@PathVariable Long companyId, @RequestParam Long employeeId);

    @PostMapping("/{companyId}/employees/remove")
    void removeEmployee(@PathVariable Long companyId, @RequestParam Long employeeId);
}