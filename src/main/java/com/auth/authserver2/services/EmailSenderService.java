package com.auth.authserver2.services;

public interface EmailSenderService {

    void sendEmailToNewUser(String userEmail, String confirmationToken);
}
