package com.Rajat.admin_auth_service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminCredential {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String name;
    
    @Column(unique = true)
    private String email;
    private String password;
}
