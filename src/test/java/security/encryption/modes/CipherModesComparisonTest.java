package security.encryption.modes;

import org.junit.jupiter.api.Test;
import javax.crypto.AEADBadTagException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class CipherModesComparisonTest {

    private SecretKey generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        return kg.generateKey();
    }

    @Test
    void ecbIdenticalBlocksProduceIdenticalCiphertext() throws Exception {
        SecretKey key = generateKey();
        // Two identical 16-byte blocks
        String block = "YELLOW SUBMARINE"; // exactly 16 bytes
        byte[] input = (block + block).getBytes();

        byte[] ciphertext = CipherModesComparison.encryptECB(input, key);

        // ECB: block 1 and block 2 should be identical
        byte[] block1 = Arrays.copyOfRange(ciphertext, 0, 16);
        byte[] block2 = Arrays.copyOfRange(ciphertext, 16, 32);
        assertArrayEquals(block1, block2, "ECB should produce identical ciphertext for identical plaintext blocks");
    }

    @Test
    void cbcIdenticalBlocksProduceDifferentCiphertext() throws Exception {
        SecretKey key = generateKey();
        String block = "YELLOW SUBMARINE";
        byte[] input = (block + block).getBytes();
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        byte[] ciphertext = CipherModesComparison.encryptCBC(input, key, iv);

        byte[] block1 = Arrays.copyOfRange(ciphertext, 0, 16);
        byte[] block2 = Arrays.copyOfRange(ciphertext, 16, 32);
        assertFalse(Arrays.equals(block1, block2), "CBC should produce different ciphertext for identical plaintext blocks");
    }

    @Test
    void gcmEncryptDecryptRoundtrip() throws Exception {
        SecretKey key = generateKey();
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);

        String plaintext = "Hello, GCM World!";
        byte[] ciphertext = CipherModesComparison.encryptGCM(plaintext.getBytes(), key, nonce);
        String decrypted = CipherModesComparison.decryptGCM(ciphertext, key, nonce);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void gcmRejectsTamperedCiphertext() throws Exception {
        SecretKey key = generateKey();
        byte[] nonce = new byte[12];
        new SecureRandom().nextBytes(nonce);

        byte[] ciphertext = CipherModesComparison.encryptGCM("Hello World".getBytes(), key, nonce);

        // Flip a bit in the ciphertext
        byte[] tampered = ciphertext.clone();
        tampered[0] ^= 0xFF;

        assertThrows(AEADBadTagException.class,
            () -> CipherModesComparison.decryptGCM(tampered, key, nonce));
    }

    @Test
    void ecbEncryptionIsConsistentForSameInput() throws Exception {
        SecretKey key = generateKey();
        byte[] input = "YELLOW SUBMARINE".getBytes();

        byte[] c1 = CipherModesComparison.encryptECB(input, key);
        byte[] c2 = CipherModesComparison.encryptECB(input, key);

        assertArrayEquals(c1, c2, "ECB should be deterministic");
    }

    @Test
    void bytesToHexReturnsLowercaseHex() {
        byte[] bytes = {(byte) 0xAB, (byte) 0xCD};
        assertEquals("abcd", CipherModesComparison.bytesToHex(bytes));
    }
}
