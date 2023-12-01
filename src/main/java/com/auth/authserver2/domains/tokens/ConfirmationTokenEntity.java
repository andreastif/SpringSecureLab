package com.auth.authserver2.domains.tokens;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_confirmation_token")
public class EmailConfirmationToken {
}
