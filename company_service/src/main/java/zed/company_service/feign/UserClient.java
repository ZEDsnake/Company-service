package zed.company_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import zed.company_service.dto.UserInfoDTO;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/by-company/{companyId}")
    List<UserInfoDTO> getUsersByCompanyId(
            @PathVariable("companyId") Long companyId,
            @RequestParam int page,
            @RequestParam int size
    );

    @DeleteMapping("/users/by-company/{companyId}")
    void deleteUsersByCompanyId(@PathVariable("companyId") Long companyId);
}
