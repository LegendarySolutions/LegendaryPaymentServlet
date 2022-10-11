package legacycode;

import org.assertj.core.api.WithAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("FieldMayBeFinal")
@RunWith(MockitoJUnitRunner.class)
public class PaymentServletTest implements WithAssertions {

    @Mock
    private HttpServletResponse response;
    @Mock
    private PaymentService paymentService;

    private PaymentServlet paymentServlet;
    private long currentTime = 100_000L;

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
        Throwable thrown = catchThrowable(() ->
                paymentServlet.process(response, "", "", "", "", "ba76a036471586d9417a0cee2fc78ee2"));
        //then
        assertThat(thrown).isInstanceOf(NumberFormatException.class);
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
        paymentServlet.process(response, "", "", "", "100001", "2303ad9f2ef11e722cfa7da72308803c");
        //then
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unrecognized format of payload!");
    }

    @Test   //https://github.com/LegendarySolutions/LegendaryPaymentServlet/issues/1
    public void shouldReproduceProblemWithMd5() throws IOException {
        //POST http://legacy-solutions.com/api/payments HTTP/1.1 403
        //amount=10000&status=OK&payload=order_id%3A6792&ts=1411677303294&md5=0c672178b3ce4ddc5404833b94cf5982
        currentTime = 1411677303290L;
        //when
        paymentServlet.process(response, "10000", "OK", "order_id:6792", "1411677303294", "0c672178b3ce4ddc5404833b94cf5982");
        //then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature do not match!");
    }

}