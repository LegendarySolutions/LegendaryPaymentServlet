package legacycode.infra;

import java.util.Arrays;
import java.util.List;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class MailServer {

    private static GreenMail greenMail;

    public static void main(String[] args) {

        ServerSetup config = new ServerSetup(9988, "localhost", "smtp");

        greenMail = new GreenMail(config);
        greenMail.start();
    }

    public static List<MimeMessage> sentEmails(){
        return Arrays.asList(greenMail.getReceivedMessages());
    }
}
