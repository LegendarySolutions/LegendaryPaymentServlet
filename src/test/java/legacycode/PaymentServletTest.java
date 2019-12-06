package legacycode;

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
import java.net.ConnectException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PaymentServletTest implements WithAssertions {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private HttpServletResponse response;
    @Mock
    private PaymentService paymentService;

    private PaymentServlet paymentServlet;

    private long currentTime = 100_001;

    @Before
    public void init() {
        paymentServlet = new PaymentServlet(paymentService, new SignatureValidator() {
            @Override
            protected long currentTime() {
                return currentTime;
            }
        });
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(response);
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

    @Test   //https://github.com/LegendarySolutions/LegendaryPaymentServlet/issues/1
    public void shouldPreserveLeadingZerosInMd5Signature() {
        //given
        currentTime = 1411677303295L;
        //expect
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                paymentServlet.process(response, "10000", "OK", "order_id:6792", "1411677303294",
                        "0c672178b3ce4ddc5404833b94cf5982"))
                .withRootCauseInstanceOf(ConnectException.class)
                .withMessageContaining("[90067-190]");
    }
}