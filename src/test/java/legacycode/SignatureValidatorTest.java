package legacycode;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import legacycode.validators.SignatureValidator;

@RunWith(MockitoJUnitRunner.class)
public class SignatureValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private long currentTime = 0;

    private SignatureValidator validator;

    @Before
    public void setUp() {
        
        validator = new SignatureValidator(){
            
            @Override
            protected long currentTime() {
                return currentTime;
            }
        };
    }

    @Test
    public void shouldFailIfInvalidSignature() throws Exception {

        thrown.expectMessage("MD5 signature do not match!");

        validator.assertValidRequest("", "", "", "", "");
    }

    @Test
    public void shouldFailIfInvalidTimestamp() throws Exception {

        thrown.expectMessage("Timestamp do not match!");

        validator.assertValidRequest("", "", "", "100000", "5f142f02085b27c938897385782563f6");
    }

    @Test
    public void shouldPassIfValidSignatureAndTimestamp() throws Exception {
        
        currentTime = 100000;
        
        validator.assertValidRequest("", "", "", "100000", "5f142f02085b27c938897385782563f6");
    }
    
    @Test
    public void shouldPadShortMd5SignatureWithZeros() throws Exception {
        
        currentTime = 1411390522346L;
        
        validator.assertValidRequest("10000", "OK", "order_id:6792", "1411390522344", "01b931f2c4aa0b0bc24e18c88eeec21b");
    }
    
}
