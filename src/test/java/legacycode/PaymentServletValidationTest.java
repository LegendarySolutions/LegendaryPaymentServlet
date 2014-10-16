package legacycode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServletValidationTest {

    @Mock
    private HttpServletResponse response;
    @Mock
    private SignatureValidator validator;

    private PaymentServlet sut;

    @Before
    public void init() {
        sut = new PaymentServlet(null, validator);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(response);
    }

    @Test
    public void shouldKeepValidationErrorReasonInResponse() throws IOException {
        //given
        willThrow(new IllegalArgumentException("MD5 signature do not match!")).given(validator).assertValidRequest("", "", "", "", "");
        //when
        sut.handle(response, "", "", "", "", "");
        //then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature do not match!");
    }

    @Test
    public void shouldDoBusinessLogicOnValidRequest() throws IOException {
        //when
        sut.handle(response, "", "", "", "100000", "5f142f02085b27c938897385782563f6");
        //then
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unrecognized format of payload!");
        verify(validator).assertValidRequest("", "", "", "100000", "5f142f02085b27c938897385782563f6");
    }
}