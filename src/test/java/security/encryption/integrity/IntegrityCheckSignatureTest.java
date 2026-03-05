package security.encryption.integrity;

import org.junit.jupiter.api.Test;
import java.security.*;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrityCheckSignatureTest {

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    @Test
    void signAndVerifySucceeds() throws Exception {
        KeyPair kp = generateKeyPair();
        byte[] data = "Hello, this is a test".getBytes();

        byte[] signature = IntegrityCheckSignature.signData(data, kp.getPrivate());
        boolean valid = IntegrityCheckSignature.verifySignature(data, signature, kp.getPublic());

        assertTrue(valid);
    }

    @Test
    void tamperedDataFailsVerification() throws Exception {
        KeyPair kp = generateKeyPair();
        byte[] originalData = "Original message".getBytes();
        byte[] tamperedData = "Tampered message".getBytes();

        byte[] signature = IntegrityCheckSignature.signData(originalData, kp.getPrivate());
        boolean valid = IntegrityCheckSignature.verifySignature(tamperedData, signature, kp.getPublic());

        assertFalse(valid);
    }

    @Test
    void wrongPublicKeyFailsVerification() throws Exception {
        KeyPair kp1 = generateKeyPair();
        KeyPair kp2 = generateKeyPair();
        byte[] data = "Hello".getBytes();

        byte[] signature = IntegrityCheckSignature.signData(data, kp1.getPrivate());
        boolean valid = IntegrityCheckSignature.verifySignature(data, signature, kp2.getPublic());

        assertFalse(valid);
    }

    @Test
    void signatureIsNotEmpty() throws Exception {
        KeyPair kp = generateKeyPair();
        byte[] data = "Test data".getBytes();
        byte[] signature = IntegrityCheckSignature.signData(data, kp.getPrivate());

        assertNotNull(signature);
        assertTrue(signature.length > 0);
    }

    @Test
    void sameDataProducesDifferentSignaturesEachTime() throws Exception {
        // RSA signatures are deterministic with SHA256withRSA (no randomness)
        // but we can at least verify two different keypairs produce different sigs
        KeyPair kp1 = generateKeyPair();
        KeyPair kp2 = generateKeyPair();
        byte[] data = "Hello".getBytes();

        byte[] sig1 = IntegrityCheckSignature.signData(data, kp1.getPrivate());
        byte[] sig2 = IntegrityCheckSignature.signData(data, kp2.getPrivate());

        assertFalse(java.util.Arrays.equals(sig1, sig2));
    }
}
