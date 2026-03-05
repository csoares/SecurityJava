package security.mac;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HMACExampleTest {

    private static final byte[] KEY = "MySuperSecretKey".getBytes();
    private static final String MESSAGE = "Transfer $1000 to account 12345";

    @Test
    void computeHMACReturns32Bytes() throws Exception {
        byte[] tag = HMACExample.computeHMAC(MESSAGE, KEY);
        assertEquals(32, tag.length); // HMAC-SHA256 = 256 bits = 32 bytes
    }

    @Test
    void computeHMACIsDeterministic() throws Exception {
        byte[] tag1 = HMACExample.computeHMAC(MESSAGE, KEY);
        byte[] tag2 = HMACExample.computeHMAC(MESSAGE, KEY);
        assertArrayEquals(tag1, tag2);
    }

    @Test
    void verifyHMACAcceptsCorrectMessage() throws Exception {
        byte[] tag = HMACExample.computeHMAC(MESSAGE, KEY);
        assertTrue(HMACExample.verifyHMAC(MESSAGE, KEY, tag));
    }

    @Test
    void verifyHMACRejectsTamperedMessage() throws Exception {
        byte[] tag = HMACExample.computeHMAC(MESSAGE, KEY);
        String tampered = "Transfer $9999 to account 99999";
        assertFalse(HMACExample.verifyHMAC(tampered, KEY, tag));
    }

    @Test
    void verifyHMACRejectsWrongKey() throws Exception {
        byte[] tag = HMACExample.computeHMAC(MESSAGE, KEY);
        byte[] wrongKey = "WrongKey12345678".getBytes();
        assertFalse(HMACExample.verifyHMAC(MESSAGE, wrongKey, tag));
    }

    @Test
    void differentMessagesProduceDifferentTags() throws Exception {
        byte[] tag1 = HMACExample.computeHMAC("Message A", KEY);
        byte[] tag2 = HMACExample.computeHMAC("Message B", KEY);
        assertFalse(java.util.Arrays.equals(tag1, tag2));
    }

    @Test
    void differentKeysProduceDifferentTags() throws Exception {
        byte[] key1 = "Key1234567890123".getBytes();
        byte[] key2 = "Key9876543210987".getBytes();
        byte[] tag1 = HMACExample.computeHMAC(MESSAGE, key1);
        byte[] tag2 = HMACExample.computeHMAC(MESSAGE, key2);
        assertFalse(java.util.Arrays.equals(tag1, tag2));
    }

    @Test
    void sha256Returns32Bytes() throws Exception {
        byte[] hash = HMACExample.sha256("hello");
        assertEquals(32, hash.length);
    }

    @Test
    void bytesToHexProducesCorrectLength() throws Exception {
        byte[] hash = HMACExample.sha256("test");
        String hex = HMACExample.bytesToHex(hash);
        assertEquals(64, hex.length()); // 32 bytes * 2 hex chars
    }
}
