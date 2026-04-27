package com.uob.mail;

import org.apache.commons.mail.*;

public class SendEmailWithImage {
    public static void main(String[] args) throws EmailException {
        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("your-email@gmail.com", "your-password"));
        email.setSSLOnConnect(true);
        email.setFrom("your-email@gmail.com", "Your Name");
        email.addTo("recipient-email@gmail.com", "Recipient Name");
        email.setSubject("Email with Embedded Image");
        email.setMsg("Here is an embedded image:");

        // Create the attachment (image)
        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath("path/to/your/image.jpg");
        attachment.setDisposition(EmailAttachment.INLINE);
        attachment.setName("image.jpg");
        attachment.setDescription("Embedded Image");

        // Add the attachment to the email
        email.attach(attachment);

        // Send the email
        try {
            email.send();
            System.out.println("Email sent successfully!");
        } catch (EmailException e) {
            e.printStackTrace();
        }
    }
}
