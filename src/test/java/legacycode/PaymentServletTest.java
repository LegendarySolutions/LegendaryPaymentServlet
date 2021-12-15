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

    @Before
    public void setup() {
        paymentServlet = new PaymentServlet(paymentService);
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
        Throwable thrown = catchThrowable(() ->
                paymentServlet.process(response, "", "", "", "", "ba76a036471586d9417a0cee2fc78ee2"));
        //then
        assertThat(thrown).isInstanceOf(NumberFormatException.class);
    }

}