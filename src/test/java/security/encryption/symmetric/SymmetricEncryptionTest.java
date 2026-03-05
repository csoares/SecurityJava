package security.encryption.symmetric;

import org.junit.jupiter.api.Test;
import javax.crypto.spec.SecretKeySpec;
import static org.junit.jupiter.api.Assertions.*;

public class SymmetricEncryptionTest {

    @Test
    void generateKeyReturns256BitKey() throws Exception {
        var key = SymmetricEncryptionExample.generateKey(256);
        assertNotNull(key);
        assertEquals(32, key.getEncoded().length); // 256 bits = 32 bytes
    }

    @Test
    void generateKeyReturns128BitKey() throws Exception {
        var key = SymmetricEncryptionExample.generateKey(128);
        assertEquals(16, key.getEncoded().length); // 128 bits = 16 bytes
    }

    @Test
    void generateIvReturns16Bytes() {
        byte[] iv = SymmetricEncryptionExample.generateIv();
        assertEquals(16, iv.length);
    }

    @Test
    void generateIvProducesRandomBytes() {
        byte[] iv1 = SymmetricEncryptionExample.generateIv();
        byte[] iv2 = SymmetricEncryptionExample.generateIv();
        assertFalse(java.util.Arrays.equals(iv1, iv2), "Two IVs should (almost certainly) differ");
    }

    @Test
    void encryptDecryptRoundtrip() throws Exception {
        var key = SymmetricEncryptionExample.generateKey(256);
        var keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        byte[] iv = SymmetricEncryptionExample.generateIv();

        String plaintext = "Hello, world! This is a test message.";
        String ciphertext = SymmetricEncryptionExample.encrypt(plaintext, keySpec, iv);
        String decrypted = SymmetricEncryptionExample.decrypt(ciphertext, keySpec, iv);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptedTextDiffersFromOriginal() throws Exception {
        var key = SymmetricEncryptionExample.generateKey(256);
        var keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        byte[] iv = SymmetricEncryptionExample.generateIv();

        String plaintext = "Hello World";
        String ciphertext = SymmetricEncryptionExample.encrypt(plaintext, keySpec, iv);
        assertNotEquals(plaintext, ciphertext);
    }

    @Test
    void sameInputDifferentIvProducesDifferentCiphertext() throws Exception {
        var key = SymmetricEncryptionExample.generateKey(256);
        var keySpec = new SecretKeySpec(key.getEncoded(), "AES");
        byte[] iv1 = SymmetricEncryptionExample.generateIv();
        byte[] iv2 = SymmetricEncryptionExample.generateIv();

        String plaintext = "Hello World";
        String cipher1 = SymmetricEncryptionExample.encrypt(plaintext, keySpec, iv1);
        String cipher2 = SymmetricEncryptionExample.encrypt(plaintext, keySpec, iv2);

        assertNotEquals(cipher1, cipher2);
    }

    @Test
    void wrongKeyFailsDecryption() throws Exception {
        var key1 = SymmetricEncryptionExample.generateKey(256);
        var key2 = SymmetricEncryptionExample.generateKey(256);
        var keySpec1 = new SecretKeySpec(key1.getEncoded(), "AES");
        var keySpec2 = new SecretKeySpec(key2.getEncoded(), "AES");
        byte[] iv = SymmetricEncryptionExample.generateIv();

        String plaintext = "Hello World";
        String ciphertext = SymmetricEncryptionExample.encrypt(plaintext, keySpec1, iv);

        assertThrows(Exception.class,
            () -> SymmetricEncryptionExample.decrypt(ciphertext, keySpec2, iv));
    }
}
