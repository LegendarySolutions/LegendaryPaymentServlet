package legacycode;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleMailSenderPoCTest {

    private GreenMail greenMail;

    @Before
    public void init() {
        greenMail = new GreenMail(new ServerSetup(9988, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));
        greenMail.start();
    }

    @After
    public void tearDown() throws Exception {
        greenMail.stop();
    }

    //TODO: Mike: Just simple PoC to test if my sending emails code works - rewrite to test that real code in PaymentServlet works correctly - one sunny day after reaching the deadline...
    @Test
    public void should_send_email() throws Exception {
        //given
        Session session = Session.getInstance(new Properties());
        Message msg = new MimeMessage(session);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress("foo@example.com"));
        msg.setSubject("Test subject");
        msg.setText("Test body");
        msg.saveChanges();
        //and
        Transport transport = session.getTransport("smtp");
        //when
        transport.connect("127.0.0.1", 9988, "", "");
        transport.sendMessage(msg, msg.getAllRecipients());
        //then
        assertThat(greenMail.waitForIncomingEmail(1)).as("One email received").isTrue();
        MimeMessage firstMessage = greenMail.getReceivedMessages()[0];
        assertThat(dox2unix(GreenMailUtil.getBody(firstMessage))).isEqualTo("Test body");
    }

    private String dox2unix(String text) {
        return text.replaceAll("\r\n", "\n");
    }
}
