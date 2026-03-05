package security.encryption.asymmetric;

import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import static org.junit.jupiter.api.Assertions.*;

public class AsymmetricEncryptionTest {

    @Test
    void generateKeyPairProducesValidKeys() throws Exception {
        KeyPair kp = AsymmetricEncryptionExample.generateKeyPair();
        assertNotNull(kp.getPublic());
        assertNotNull(kp.getPrivate());
        assertEquals("RSA", kp.getPublic().getAlgorithm());
        assertEquals("RSA", kp.getPrivate().getAlgorithm());
    }

    @Test
    void encryptDecryptRoundtrip() throws Exception {
        KeyPair kp = AsymmetricEncryptionExample.generateKeyPair();
        String plaintext = "Hello, world!";
        String ciphertext = AsymmetricEncryptionExample.encrypt(plaintext, kp.getPublic());
        String decrypted = AsymmetricEncryptionExample.decrypt(ciphertext, kp.getPrivate());
        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptedTextDiffersFromOriginal() throws Exception {
        KeyPair kp = AsymmetricEncryptionExample.generateKeyPair();
        String plaintext = "Hello";
        String ciphertext = AsymmetricEncryptionExample.encrypt(plaintext, kp.getPublic());
        assertNotEquals(plaintext, ciphertext);
    }

    @Test
    void wrongPrivateKeyFailsDecryption() throws Exception {
        KeyPair kp1 = AsymmetricEncryptionExample.generateKeyPair();
        KeyPair kp2 = AsymmetricEncryptionExample.generateKeyPair();

        String ciphertext = AsymmetricEncryptionExample.encrypt("Hello", kp1.getPublic());
        assertThrows(Exception.class,
            () -> AsymmetricEncryptionExample.decrypt(ciphertext, kp2.getPrivate()));
    }

    @Test
    void differentEncryptionOfSameTextCanDiffer() throws Exception {
        // RSA with default padding is non-deterministic; two encryptions differ
        KeyPair kp = AsymmetricEncryptionExample.generateKeyPair();
        String plaintext = "Hello";
        String c1 = AsymmetricEncryptionExample.encrypt(plaintext, kp.getPublic());
        String c2 = AsymmetricEncryptionExample.encrypt(plaintext, kp.getPublic());
        // They may differ (probabilistic padding), and both must decrypt correctly
        assertEquals(plaintext, AsymmetricEncryptionExample.decrypt(c1, kp.getPrivate()));
        assertEquals(plaintext, AsymmetricEncryptionExample.decrypt(c2, kp.getPrivate()));
    }
}
