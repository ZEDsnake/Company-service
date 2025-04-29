package com.zed.user_service.repository;

import com.zed.user_service.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByCompanyId(Long companyId, Pageable pageable);

    boolean existsByPhoneNumber(String phoneNumber);

    void deleteByCompanyId(Long companyId);
}
