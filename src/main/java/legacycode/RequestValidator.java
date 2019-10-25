package legacycode;

import org.apache.commons.codec.digest.DigestUtils;

public class RequestValidator {
    private static final String secret = "15c84df6-bfa3-46c1-8929-a5dedaeab4a4";

    void validateRequest(String amount, String status, String payload, String timestamp, String md5) {
        String expectedMd5 = DigestUtils.md5Hex(amount + status + payload + timestamp + secret);
        System.out.println("Expected MD5: " + expectedMd5);

        if (!expectedMd5.equals(md5)) {
            throw new ValidationException2("MD5 signature do not match!");
        }

        if (Math.abs(currentTime() - Long.valueOf(timestamp)) > 60000) {
            throw new ValidationException2("Timestamp do not match!");
        }
    }

    protected long currentTime() {
        return System.currentTimeMillis();
    }
}