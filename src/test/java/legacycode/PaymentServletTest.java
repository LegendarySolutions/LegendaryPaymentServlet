package legacycode;

import org.assertj.core.api.WithAssertions;
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
    private PaymentService paymentService;
    @Mock
    private HttpServletResponse response;

    @Test
    public void should1() throws IOException {
        //given
        PaymentServlet paymentServlet = new PaymentServlet(paymentService);
        //when
        paymentServlet.process(response, "", "", "", "", "");
        //then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature do not match!");
    }
}












