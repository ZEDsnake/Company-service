package com.zed.user_service.repository;

import com.zed.user_service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    List<UserEntity> findAllByCompanyId(Long companyId);
}