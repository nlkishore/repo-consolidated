package com.uob.mail;

import org.apache.commons.mail.HtmlEmail;

public class EmailWithExternalImage {
    public static void main(String[] args) {
        try {
            HtmlEmail email = new HtmlEmail();
            email.setHostName("smtp.example.com");
            email.setSmtpPort(587);
            email.setAuthentication("your_email@example.com", "your_password");
            email.setFrom("your_email@example.com", "Your Name");
            email.setSubject("Email with External Image");

            email.addTo("recipient@example.com");

            // Set HTML content with an external image
            String imageUrl = "https://example.com/path/to/image.jpg";
            email.setHtmlMsg("<html><body><h1>Hello!</h1><img src=\"" + imageUrl + "\"/></body></html>");

            // Send email
            email.send();
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
