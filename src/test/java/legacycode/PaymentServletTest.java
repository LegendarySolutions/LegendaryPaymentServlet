package legacycode;

import org.assertj.core.api.WithAssertions;
import org.h2.jdbc.JdbcSQLException;
import org.junit.After;
import org.junit.Before;
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
    public void should2() {
        //when
        Throwable thrown = catchThrowable(() -> paymentServlet.process(response, "", "", "", "", "ba76a036471586d9417a0cee2fc78ee2"));
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
    public void shouldPadMd5WithLeadingZeros() {
        //given
        currentTime = 1411677303295L;
        //when
        Throwable thrown = catchThrowable(() -> paymentServlet.process(response, "10000", "OK", "order_id:6792", "1411677303294", "0c672178b3ce4ddc5404833b94cf5982"));
        //then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(JdbcSQLException.class)
                .hasMessageContaining("[90067-190]");
    }

}