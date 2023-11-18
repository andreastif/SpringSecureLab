package com.auth.authserver2.domains;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class EntityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "registered_to_client_id")
    private String registeredToClientId;

    @Column(name = "account_non_expired")
    private String accountNonExpired;

    @Column(name = "account_non_locked")
    private String accountNonLocked;

    @Column(name = "credentials_non_expired")
    private String credentialsNonExpired;

    @Column(name = "enabled")
    private String enabled;

    @Column(length = 2000, name = "authorities")
    private String authorities;
}
