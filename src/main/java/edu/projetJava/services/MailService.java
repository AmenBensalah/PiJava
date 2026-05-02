package edu.projetJava.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {
    // TODO: À remplacer par le vrai e-mail Gmail
    private static final String SENDER_EMAIL = "aminejouini230@gmail.com"; 
    // TODO: À remplacer par le "Mot de passe d'application" généré sur votre compte Google (Sécurité > Validation en deux étapes > Mots de passe des applications)
    private static final String SENDER_PASSWORD = "aypb mqbu wacu uzqw"; 

    public static void sendEmail(String recipient, String subject, String content) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Remove spaces in case user pasted the App Password with spaces
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD.replace(" ", ""));
            }
        });

        // Debug optionnel
        // session.setDebug(true);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        
        // Support HTML content
        message.setContent(content, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
