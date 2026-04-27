package com.uob.mail;

import org.apache.commons.mail.*;

public class SendEmailWithEmbeddedImage {
    public static void main(String[] args) throws EmailException {
        // Create the email message
        HtmlEmail email = new HtmlEmail();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("your-email@gmail.com", "your-password"));
        email.setSSLOnConnect(true);
        email.setFrom("your-email@gmail.com", "Your Name");
        email.addTo("recipient-email@gmail.com", "Recipient Name");
        email.setSubject("Email with Embedded Image");

        // Set the HTML message
        String htmlMessage = "<html><body><h1>Here is an embedded image:</h1><img src='https://yourwebsite.com/path/to/image.jpg'></body></html>";
        email.setHtmlMsg(htmlMessage);

        // Set the alternative text message
        email.setTextMsg("Your email client does not support HTML messages.");

        // Send the email
        try {
            email.send();
            System.out.println("Email sent successfully!");
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }
}

