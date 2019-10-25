package legacycode;

import org.assertj.core.api.WithAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class PaymentServletTest implements WithAssertions {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private HttpServletResponse response;

    @Test
    public void should1() throws IOException {
        //given
        PaymentServlet paymentServlet = new PaymentServlet();
        //when
        paymentServlet.process(response, "", "", "", "", "");
        //then
        //???
    }
}