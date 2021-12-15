package legacycode;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RequestSignatureValidator {

    private static final String secret = "15c84df6-bfa3-46c1-8929-a5dedaeab4a4";

    public RequestSignatureValidator() {
    }

    void validate(String amount, String status, String payload, String timestamp, String md5) {
        try {

            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(amount.getBytes());
            digest.update(status.getBytes());
            digest.update(payload.getBytes());
            digest.update(timestamp.getBytes());
            digest.update(secret.getBytes());

            String expectedMd5 = String.format("%x", new BigInteger(1, digest.digest()));
            System.out.println("Expected MD5: " + expectedMd5);

            if (!expectedMd5.equals(md5)) {
                throw new ValidationException("MD5 signature do not match!");
            }

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if (Math.abs(currentTime() - Long.valueOf(timestamp)) > 60000) {
            throw new ValidationException("Timestamp do not match!");
        }
    }

    protected long currentTime() {
        return System.currentTimeMillis();
    }
}
