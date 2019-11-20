package legacycode;

import org.assertj.core.api.WithAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;

public class PaymentServletTest implements WithAssertions {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private HttpServletResponse response;

    @Mock
    private PaymentService paymentService;

    private PaymentServlet paymentServlet;

    @Before
    public void init() {
        paymentServlet = new PaymentServlet(paymentService) {
            @Override
            protected long currentTime() {
                return 100_001;
            }
        };
    }

    @Test
    public void should1() throws IOException {
        //when
        paymentServlet.process(response, "", "", "", "", "");
        //then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature do not match!");
    }

    @Test
    public void should2() throws IOException {
        //when
        paymentServlet.process(response, "", "", "", "10000", "2d9f2cff82ca49088bc4629bb288dd51");
        //then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Timestamp do not match!");
    }

    @Test
    public void should3() throws IOException {
        //when
        paymentServlet.process(response, "", "", "", "100000", "5f142f02085b27c938897385782563f6");
        //then
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unrecognized format of payload!");
    }
}













