package com.zed.user_service.feign;

import com.zed.user_service.dto.CompanyInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "company-service")
public interface CompanyClient {

    @GetMapping("/companies/{companyId}")
    CompanyInfoDTO getCompanyById(@PathVariable Long companyId);
}
