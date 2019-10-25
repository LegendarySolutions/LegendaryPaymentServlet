package legacycode;

import info.solidsoft.mockito.java8.api.WithBDDMockito;
import org.assertj.core.api.WithAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PaymentServletTest implements WithAssertions, WithBDDMockito {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private HttpServletResponse response;
    @Mock
    private PaymentService paymentService;

    private PaymentServlet paymentServlet;

    @After
    public void after() {
        verifyNoMoreInteractions(response);
    }

    @Before
    public void setup() {
        paymentServlet = new PaymentServlet(paymentService) {
            @Override
            protected long currentTime() {
                return 100_001L;
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
    public void should2() {
        //expect
        assertThatThrownBy(() -> paymentServlet.process(response, "", "", "", "", "ba76a036471586d9417a0cee2fc78ee2"))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void should3() throws IOException {
        //when
        paymentServlet.process(response, "", "", "", "10000", "2d9f2cff82ca49088bc4629bb288dd51");
        //then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Timestamp do not match!");
    }

    @Test
    public void should4() throws IOException {
        //when
        paymentServlet.process(response, "", "", "", "100000", "5f142f02085b27c938897385782563f6");
        //then
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unrecognized format of payload!");
    }

}


