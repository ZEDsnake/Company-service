package com.zed.company_service.feign;

import com.zed.company_service.dto.CompanyInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "companyClient", url = "http://localhost:8080")
public interface CompanyClient {

    @GetMapping("/companies/{companyId}")
    CompanyInfoDTO getCompanyById(@PathVariable Long companyId);
}
