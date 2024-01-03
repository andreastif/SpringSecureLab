package com.SpringSecureLab.services;

public interface EmailSenderService {

    void sendEmailToNewUser(String userEmail, String confirmationToken);
}
