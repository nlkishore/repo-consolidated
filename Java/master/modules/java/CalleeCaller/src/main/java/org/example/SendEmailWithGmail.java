package org.example;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendEmailWithGmail {
    public static void main(String[] args) {
        final String username = "nlkishore";
        final String password = "ieckwxxbqispozme";
        final String fromEmail = "nlkishore@yahoo.com";
        final String toEmail = "nlaxmikishore@gmail.com";
        final String subject = "Test Email from Java";
        final String message = "Hello, this is a test email sent from Java using Gmail SMTP!";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // Use TLS
        properties.put("mail.smtp.host", "smtp.mail.yahoo.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(fromEmail));
            email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            email.setSubject(subject);
            email.setText(message);

            Transport.send(email);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
