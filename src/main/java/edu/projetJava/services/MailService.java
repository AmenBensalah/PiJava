package edu.projetJava.services;

import edu.esportify.config.EnvConfig;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailService {
    private static final String DEFAULT_SENDER_EMAIL = "aminejouini230@gmail.com";
    private static final String DEFAULT_SENDER_PASSWORD = "aypb mqbu wacu uzqw";

    public static void sendEmail(String recipient, String subject, String content) throws MessagingException {
        String senderEmail = EnvConfig.get("SMTP_USERNAME", DEFAULT_SENDER_EMAIL);
        String senderPassword = EnvConfig.get("SMTP_PASSWORD", DEFAULT_SENDER_PASSWORD);
        String smtpHost = EnvConfig.get("SMTP_HOST", "smtp.gmail.com");
        String smtpPort = EnvConfig.get("SMTP_PORT", "587");

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.ssl.trust", smtpHost);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword.replace(" ", ""));
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setContent(content, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
