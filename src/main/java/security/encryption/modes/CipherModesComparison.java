package security.encryption.modes;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Arrays;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * Cipher Block Mode Comparison: ECB vs CBC vs GCM
 *
 * This example demonstrates why cipher mode selection is critical to security.
 * The algorithm (AES) is the same in all three — only the MODE changes.
 *
 * - ECB (Electronic Code Book):   each block encrypted independently → leaks patterns
 * - CBC (Cipher Block Chaining):  each block XORed with previous ciphertext → hides patterns
 * - GCM (Galois/Counter Mode):    authenticated encryption → encrypts AND authenticates
 */
public class CipherModesComparison {

    public static void main(String[] args) throws Exception {
        // Generate a single AES-256 key used for all three modes
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();

        // Use three identical 16-byte blocks to expose ECB's pattern leakage
        // Each 16-byte block is exactly one AES block
        String block = "YELLOW SUBMARINE"; // exactly 16 bytes
        String plaintext = block + block + block; // 3 identical blocks

        System.out.println("=== Cipher Block Mode Comparison ===");
        System.out.println("Plaintext: \"" + plaintext + "\"");
        System.out.println("(Three identical 16-byte blocks — one AES block each)");
        System.out.println();

        // ─── ECB Mode ───────────────────────────────────────────────────────────
        System.out.println("--- ECB: Electronic Code Book (INSECURE) ---");
        byte[] ecbOut = encryptECB(plaintext.getBytes(), key);
        System.out.println("Ciphertext (hex): " + bytesToHex(ecbOut));
        System.out.println("Block 1: " + bytesToHex(Arrays.copyOfRange(ecbOut, 0, 16)));
        System.out.println("Block 2: " + bytesToHex(Arrays.copyOfRange(ecbOut, 16, 32)));
        System.out.println("Block 3: " + bytesToHex(Arrays.copyOfRange(ecbOut, 32, 48)));
        System.out.println("PROBLEM: Identical plaintext blocks → IDENTICAL ciphertext blocks!");
        System.out.println("An attacker can detect repeated data and infer message structure.");
        System.out.println("(Famous example: encrypting a bitmap image with ECB still shows the picture)");
        System.out.println();

        // ─── CBC Mode ───────────────────────────────────────────────────────────
        System.out.println("--- CBC: Cipher Block Chaining (GOOD) ---");
        byte[] cbcIv = new byte[16];
        new SecureRandom().nextBytes(cbcIv);
        byte[] cbcOut = encryptCBC(plaintext.getBytes(), key, cbcIv);
        System.out.println("Ciphertext (hex): " + bytesToHex(cbcOut));
        System.out.println("Block 1: " + bytesToHex(Arrays.copyOfRange(cbcOut, 0, 16)));
        System.out.println("Block 2: " + bytesToHex(Arrays.copyOfRange(cbcOut, 16, 32)));
        System.out.println("Block 3: " + bytesToHex(Arrays.copyOfRange(cbcOut, 32, 48)));
        System.out.println("FIX: Each block is XORed with the previous ciphertext block before encryption.");
        System.out.println("Identical plaintext blocks → completely different ciphertext blocks.");
        System.out.println("LIMITATION: Provides confidentiality only; tampering is undetected.");
        System.out.println();

        // ─── GCM Mode ───────────────────────────────────────────────────────────
        System.out.println("--- GCM: Galois/Counter Mode (BEST — authenticated encryption) ---");
        byte[] gcmNonce = new byte[12]; // GCM standard: 12-byte nonce
        new SecureRandom().nextBytes(gcmNonce);
        // GCM output = ciphertext + 16-byte authentication tag (appended by JCE)
        byte[] gcmOut = encryptGCM(plaintext.getBytes(), key, gcmNonce);
        int cipherLen = gcmOut.length - 16;
        byte[] gcmCipher = Arrays.copyOfRange(gcmOut, 0, cipherLen);
        byte[] authTag   = Arrays.copyOfRange(gcmOut, cipherLen, gcmOut.length);
        System.out.println("Ciphertext (hex): " + bytesToHex(gcmCipher));
        System.out.println("Auth tag  (hex):  " + bytesToHex(authTag));
        System.out.println("ADVANTAGE 1: No repeated-block patterns (uses counter mode internally).");
        System.out.println("ADVANTAGE 2: Authentication tag detects ANY tampering with ciphertext.");
        System.out.println("Equivalent to AES-CBC + HMAC, but faster and in one step.");
        System.out.println();

        // Verify GCM decryption succeeds
        String decrypted = decryptGCM(gcmOut, key, gcmNonce);
        System.out.println("GCM round-trip decryption: " + plaintext.equals(decrypted));

        // Demonstrate GCM tamper detection
        System.out.println();
        System.out.println("--- GCM Tamper Detection ---");
        byte[] tampered = gcmOut.clone();
        tampered[0] ^= 0xFF; // flip bits in first byte of ciphertext
        try {
            decryptGCM(tampered, key, gcmNonce);
            System.out.println("Decryption succeeded (unexpected).");
        } catch (AEADBadTagException e) {
            System.out.println("Tampered ciphertext rejected: AEADBadTagException (authentication failed).");
        }

        System.out.println();
        System.out.println("--- Summary ---");
        System.out.println("ECB: Never use for encryption of more than one block.");
        System.out.println("CBC: Use with HMAC for authenticated encryption (or avoid in new designs).");
        System.out.println("GCM: Preferred modern mode — confidentiality + integrity in one pass.");
    }

    /**
     * ECB mode: each 16-byte block encrypted independently with the same key.
     * No IV required. Deterministic — same input always produces same output.
     */
    public static byte[] encryptECB(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * CBC mode: each block is XORed with the previous ciphertext block before encryption.
     * Requires a random IV for the first block.
     */
    public static byte[] encryptCBC(byte[] data, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    /**
     * GCM mode: authenticated encryption. Output includes ciphertext followed by
     * a 16-byte (128-bit) authentication tag appended by the JCE implementation.
     */
    public static byte[] encryptGCM(byte[] data, SecretKey key, byte[] nonce) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
        return cipher.doFinal(data);
    }

    /**
     * GCM decryption: authenticates the tag before returning plaintext.
     * Throws AEADBadTagException if ciphertext was tampered with.
     */
    public static String decryptGCM(byte[] cipherTextWithTag, SecretKey key, byte[] nonce) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, nonce));
        return new String(cipher.doFinal(cipherTextWithTag));
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
