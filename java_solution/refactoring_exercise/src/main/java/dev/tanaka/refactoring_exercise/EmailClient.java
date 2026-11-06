package dev.tanaka.refactoring_exercise;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailClient {

    private final JavaMailSender mailSender;

    public EmailClient(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    protected void sendEmail(String email, String subject, String body)
        throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

}
