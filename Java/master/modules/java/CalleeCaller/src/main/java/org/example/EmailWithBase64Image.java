package org.example;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.util.Base64;

public class EmailWithBase64Image {

    public static void main(String[] args) {
        /*String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAYAAABWdVznAAAACXBIWXMAAAsSAAALEgHS3X78AAAA\n" +
                "GXRFWWHRTGluenMgU1ZHLWlDRW8gdmVyc2lvbj1MMS4zAAAAB3RJTUUH5QgUDQMb99GAygAAAAZi\n" +
                "S0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAA10RVh0U29mdHdhcmUATWFjcm9t\n" +
                "ZWRpYSBQYWludCAtIEV4cHJlc3MtZi/ANRThAAAAJXRFWHRDb21tZW50AHRlc3QgcmVkIGRvdCBl\n" +
                "bmNvZGVkIGltYWdlIGZpbGUQb34MAAAAaklEQVQoU2NkwAD+EADEOBBSFnQAhtIZeARyBk+BVoF9\n" +
                "ABIQoMKxEAs5JLBcB5DmgGBDGgVoKAZ8KYZKD8Rw2A14qMEcLEwFgmgCBJCMwII8nAEw0KpwhgAA\n" +
                "AP//aOR/w4dgAh9IAAAAAElFTkSuQmCC\n";*/
        String base64String = "iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAYAAABWdVznAAAACXBIWXMAAAsSAAALEgHS3X78AAAA"
                + "GXRFWWHRTGluenMgU1ZHLWlDRW8gdmVyc2lvbj1MMS4zAAAAB3RJTUUH5QgUDQMb99GAygAAAAZi"
                + "S0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAA10RVh0U29mdHdhcmUATWFjcm9t"
                + "ZWRpYSBQYWludCAtIEV4cHJlc3MtZi/ANRThAAAAJXRFWHRDb21tZW50AHRlc3QgcmVkIGRvdCB0"
                + "ZW50IGltYWdlIGZpbGUQb34MAAAAaklEQVQoU2NkwAD+EADEOBBSFnQAhtIZeARyBk+BVoF9ABIQ"
                + "oMKxEAs5JLBcB5DmgGBDGgVoKAZ8KYZKD8Rw2A14qMEcLEwFgmgCBJCMwII8nAEw0KpwhgAAAP//"
                + "aOR/w4dgAh9IAAAAAElFTkSuQmCC";
        byte[] imageBytes = Base64.getDecoder().decode(base64String);

        try {
            sendEmail(
                    "smtp.mail.yahoo.com",
                    587,
                    "nlkishore@yahoo.com",
                    "ieckwxxbqispozme",
                    "nlaxmikishore@gmail.com",
                    "Test Email from Gmail"
            );

           /* sendEmail(
                    "smtp.office365.com",
                    587,
                    "your_outlook_address@outlook.com",
                    "your_outlook_password",
                    "recipient_email@outlook.com",
                    "Test Email from Outlook"
            );*/

        } catch (EmailException e) {
            e.printStackTrace();
        }
    }

    private static void sendEmail(String smtpHostName, int smtpPort, String senderEmail, String senderPassword, String recipientEmail, String subject) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setHostName(smtpHostName);
        email.setSmtpPort(smtpPort);
        email.setAuthentication(senderEmail, senderPassword);
        email.setSSLOnConnect(smtpPort == 465); // Use SSL for port 465
        email.setStartTLSEnabled(smtpPort == 587); // Use TLS for port 587
        email.setFrom(senderEmail, "Your Name");
        email.addTo(recipientEmail);
        email.setSubject(subject);

        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAwAAAAMCAYAAABWdVznAAAACXBIWXMAAAsSAAALEgHS3X78AAAA"
                + "GXRFWWHRTGluenMgU1ZHLWlDRW8gdmVyc2lvbj1MMS4zAAAAB3RJTUUH5QgUDQMb99GAygAAAAZi"
                + "S0dEAP8A/wD/oL2nkwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAA10RVh0U29mdHdhcmUATWFjcm9t"
                + "ZWRpYSBQYWludCAtIEV4cHJlc3MtZi/ANRThAAAAJXRFWHRDb21tZW50AHRlc3QgcmVkIGRvdCB0"
                + "ZW50IGltYWdlIGZpbGUQb34MAAAAaklEQVQoU2NkwAD+EADEOBBSFnQAhtIZeARyBk+BVoF9ABIQ"
                + "oMKxEAs5JLBcB5DmgGBDGgVoKAZ8KYZKD8Rw2A14qMEcLEwFgmgCBJCMwII8nAEw0KpwhgAAAP//"
                + "aOR/w4dgAh9IAAAAAElFTkSuQmCC";
        String htmlContent = "<html><body>"
                + "<h1>Here is an image encoded in Base64</h1>"
                + "<img src='data:image/png;base64," + base64Image + "' alt='Base64 Image' />"
                + "</body></html>";

        email.setHtmlMsg(htmlContent);
        email.send();

        System.out.println("Email sent successfully to " + recipientEmail + " using " + smtpHostName);
    }
}
