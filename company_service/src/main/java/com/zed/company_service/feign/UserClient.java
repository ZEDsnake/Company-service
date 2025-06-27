package com.zed.company_service.feign;

import com.zed.company_service.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", path = "/users")
public interface UserClient {

    @PostMapping("/by-ids")
    List<UserInfoDto> getUsersByIds(
            @RequestBody List<Long> userIds);

    @DeleteMapping("/by-company/{companyId}")
    void deleteUsersByCompanyId(
            @PathVariable("companyId") Long companyId);

    @PostMapping("/{userId}/company")
    void updateUserCompany(
            @PathVariable("userId") Long userId,
            @RequestParam(required = false) Long companyId);
}