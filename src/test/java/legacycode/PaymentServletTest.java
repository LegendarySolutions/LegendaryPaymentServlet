package legacycode;

import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServletTest {

    @Mock
    private HttpServletResponse response;

    private PaymentServlet servlet;

    private long currentTime = 0;

    @Before
    public void setUp() {
        
        servlet = new PaymentServlet(null, new SignatureValidator(){
            
            @Override
            protected long currentTime() {
                return currentTime;
            }
        });
    }

    @After
    public void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void characterization_test_1() throws Exception {

        servlet.handle(response, "", "", "", "", "");

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature do not match!");
    }

    @Test
    public void characterization_test_2() throws Exception {

        servlet.handle(response, "", "", "", "100000", "5f142f02085b27c938897385782563f6");

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Timestamp do not match!");
    }

    @Test
    public void characterization_test_3() throws Exception {
        
        currentTime = 100000;
        
        servlet.handle(response, "", "", "", "100000", "5f142f02085b27c938897385782563f6");
        
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unrecognized format of payload!");
    }
    
    @Test
    public void characterization_test_bug() throws Exception {
        
        currentTime = 1411390522346L;
        
        servlet.handle(response, "10000", "OK", "order_id:6792", "1411390522344", "01b931f2c4aa0b0bc24e18c88eeec21b");
        
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "No pending oder with id: 6792!");
        verify(response, Mockito.never()).sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature do not match!");
    }
    
}
