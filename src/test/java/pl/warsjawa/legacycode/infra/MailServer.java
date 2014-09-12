package pl.warsjawa.legacycode.infra;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class MailServer {

    public static void main(String[] args) {

        ServerSetup config = new ServerSetup(9988, "localhost", "smtp");

        GreenMail greenMail = new GreenMail(config);
        greenMail.start();

    }

}
