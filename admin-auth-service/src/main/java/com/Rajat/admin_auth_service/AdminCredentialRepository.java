package com.Rajat.admin_auth_service;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminCredentialRepository extends JpaRepository<AdminCredential, Long> {
    Optional<AdminCredential> findByName(String name);

}
