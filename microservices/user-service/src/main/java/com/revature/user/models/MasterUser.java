package com.revature.user.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "master_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String email;
    private String phone;

    @Column(name = "password_encrypted")
    private String passwordEncrypted;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SecurityQuestions> securityQuestions;
}
