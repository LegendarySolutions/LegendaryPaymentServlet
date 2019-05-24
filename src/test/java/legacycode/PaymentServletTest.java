package legacycode;

import org.assertj.core.api.WithAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.BDDMockito.then;
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

    @After
    public void cleanup() {
        verifyNoMoreInteractions(response);
    }

    @Before
    public void init() {
        paymentServlet = new PaymentServlet(paymentService) {
            @Override
            protected long currentTime() {
                return 100_000L;
            }
        };
    }

    @Test
    public void should1() throws IOException {
        //when
        paymentServlet.process(response, "", "", "",
                "", "");
        //then
        then(response).should().sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature do not match!");
//        verify(response).sendError(...);
    }

    @Test
    public void should2() {
        //when
        Throwable thrown = catchThrowable(() -> paymentServlet.process(response, "", "", "",
                "", "ba76a036471586d9417a0cee2fc78ee2"));
        //then
        assertThat(thrown).isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void should3() throws IOException {
        //when
        paymentServlet.process(response, "", "", "",
                "10000", "2d9f2cff82ca49088bc4629bb288dd51");
        //then
        then(response).should().sendError(HttpServletResponse.SC_FORBIDDEN, "Timestamp do not match!");
    }

    @Test
    public void should4() throws IOException {
        //when
        paymentServlet.process(response, "", "", "",
                "100001", "2303ad9f2ef11e722cfa7da72308803c");
        //then
        then(response).should().sendError(HttpServletResponse.SC_BAD_REQUEST, "Unrecognized format of payload!");
    }
}