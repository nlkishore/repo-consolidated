package com.uob.mail;

import java.io.File;


import org.apache.commons.mail.HtmlEmail;

public class EmailWithEmbeddedImage {
    public static void main(String[] args) {
        try {
            // Create an HtmlEmail instance
            HtmlEmail email = new HtmlEmail();
            email.setHostName("smtp.example.com");
            email.setSmtpPort(587);
            email.setAuthentication("your_email@example.com", "your_password");
            email.setFrom("your_email@example.com", "Your Name");
            email.setSubject("Email with Embedded Image");

            // Add recipient
            email.addTo("recipient@example.com");

            // Embed image
            String cid = email.embed(new File("path/to/image.jpg"), "Embedded Image");

            // Set HTML content with embedded image
            email.setHtmlMsg("<html><body><h1>Hello!</h1><img src=\"cid:" + cid + "\"/></body></html>");

            // Send email
            email.send();
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

