package com.auth.authserver2.services.impl;


import com.auth.authserver2.services.EmailSenderService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service("emailSenderService")
public class EmailSenderServiceImpl implements EmailSenderService {


    @Value("${email.username}")
    private String gmail;

    @Value("${email.password}")
    private String password;

    @Override
        public void sendEmailToNewUser(String userEmail) {
            Email email = EmailBuilder.startingBlank()
                    .from("Auth Server 2.0", "arch.portfolio.supp@gmail.com")
                    .to(userEmail)
                    .withSubject("Welcome!")
                    .withPlainText(
                            "Welcome to the Auth Server 2.0. " +
                                    "Please finish your registration by activating your account by following this link: " +
                                    "<link placeholder>")
                    .buildEmail();

            MailerBuilder
                    .withSMTPServer("smtp.gmail.com", 587, gmail, password)
                    .buildMailer()
                    .sendMail(email);

            log.info("Sent email to: " + userEmail);
        }


}
