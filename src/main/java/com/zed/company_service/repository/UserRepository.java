package com.zed.company_service.repository;

import com.zed.company_service.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUserEntity, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
}
