package legacycode;

import org.assertj.core.api.WithAssertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PaymentServletTest implements WithAssertions {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private HttpServletResponse response;
    @Mock
    private PaymentService paymentService;

    @After
    public void tearDown() {
        verifyNoMoreInteractions(response);
    }

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