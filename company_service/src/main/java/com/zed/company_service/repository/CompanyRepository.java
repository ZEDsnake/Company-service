package com.zed.company_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.zed.company_service.entity.CompanyEntity;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM CompanyEntity c WHERE :employeeId MEMBER OF c.employeeIds")
    List<CompanyEntity> findByEmployeeId(Long employeeId);
}