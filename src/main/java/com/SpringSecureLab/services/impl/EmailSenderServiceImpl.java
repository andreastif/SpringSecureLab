package com.SpringSecureLab.services.impl;


import com.SpringSecureLab.services.EmailSenderService;
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
        public void sendEmailToNewUser(String userEmail, String confirmationToken) {

            String link = "http://localhost:8080/api/v1/members/confirm?token=" + confirmationToken;

            Email email = EmailBuilder.startingBlank()
                    .from("Auth Server 2.0", "arch.portfolio.supp@gmail.com")
                    .to(userEmail)
                    .withSubject("Welcome!")
                    .withHTMLText(
                            "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                                    "\n" +
                                    "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                                    "\n" +
                                    "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                                    "    <tbody><tr>\n" +
                                    "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                                    "        \n" +
                                    "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                                    "          <tbody><tr>\n" +
                                    "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                                    "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                                    "                  <tbody><tr>\n" +
                                    "                    <td style=\"padding-left:10px\">\n" +
                                    "                  \n" +
                                    "                    </td>\n" +
                                    "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                                    "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                                    "                    </td>\n" +
                                    "                  </tr>\n" +
                                    "                </tbody></table>\n" +
                                    "              </a>\n" +
                                    "            </td>\n" +
                                    "          </tr>\n" +
                                    "        </tbody></table>\n" +
                                    "        \n" +
                                    "      </td>\n" +
                                    "    </tr>\n" +
                                    "  </tbody></table>\n" +
                                    "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                                    "    <tbody><tr>\n" +
                                    "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                                    "      <td>\n" +
                                    "        \n" +
                                    "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                                    "                  <tbody><tr>\n" +
                                    "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                                    "                  </tr>\n" +
                                    "                </tbody></table>\n" +
                                    "        \n" +
                                    "      </td>\n" +
                                    "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                                    "    </tr>\n" +
                                    "  </tbody></table>\n" +
                                    "\n" +
                                    "\n" +
                                    "\n" +
                                    "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                                    "    <tbody><tr>\n" +
                                    "      <td height=\"30\"><br></td>\n" +
                                    "    </tr>\n" +
                                    "    <tr>\n" +
                                    "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                                    "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                                    "        \n" +
                                    "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + userEmail + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 24 hours. <p>See you soon</p>" +
                                    "        \n" +
                                    "      </td>\n" +
                                    "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                                    "    </tr>\n" +
                                    "    <tr>\n" +
                                    "      <td height=\"30\"><br></td>\n" +
                                    "    </tr>\n" +
                                    "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                                    "\n" +
                                    "</div></div>")
                    .buildEmail();

            MailerBuilder
                    .withSMTPServer("smtp.gmail.com", 587, gmail, password)
                    .buildMailer()
                    .sendMail(email);

            log.info("Sent email to: " + userEmail);
        }


}
