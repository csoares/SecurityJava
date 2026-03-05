package security.mac;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * HMAC (Hash-based Message Authentication Code)
 *
 * HMAC combines a cryptographic hash function with a secret key to produce
 * an authentication tag. Unlike a plain hash, only parties with the shared
 * secret key can generate or verify the tag.
 *
 * Authentication spectrum:
 *   Plain hash       → integrity only; no key; anyone can recompute
 *   HMAC             → integrity + authentication; symmetric secret key
 *   Digital signature→ integrity + authentication + non-repudiation; asymmetric keys
 */
public class HMACExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== HMAC: Hash-based Message Authentication Code ===");
        System.out.println();

        String message = "Transfer $1000 to account 12345";
        byte[] sharedKey = "MySuperSecretKey".getBytes(); // shared between sender and receiver

        // ─── Stage 1: Plain SHA-256 ──────────────────────────────────────────────
        System.out.println("--- Stage 1: Plain SHA-256 (no key) ---");
        byte[] plainHash = sha256(message);
        System.out.println("Message:    " + message);
        System.out.println("SHA-256:    " + bytesToHex(plainHash));
        System.out.println("PROBLEM: Anyone can compute this hash.");
        System.out.println("An attacker can change the message and recompute a valid hash.");
        System.out.println();

        // ─── Stage 2: HMAC-SHA256 ───────────────────────────────────────────────
        System.out.println("--- Stage 2: HMAC-SHA256 (shared secret key) ---");
        byte[] hmacTag = computeHMAC(message, sharedKey);
        System.out.println("Message:    " + message);
        System.out.println("HMAC tag:   " + bytesToHex(hmacTag));
        System.out.println("Only holders of the secret key can generate or verify this tag.");
        System.out.println();

        // ─── Verification: correct message ──────────────────────────────────────
        System.out.println("--- Verification: Correct Message ---");
        boolean valid = verifyHMAC(message, sharedKey, hmacTag);
        System.out.println("Message accepted: " + valid); // true
        System.out.println();

        // ─── Verification: tampered message ─────────────────────────────────────
        System.out.println("--- Verification: Tampered Message ---");
        String tampered = "Transfer $9999 to account 99999";
        boolean tamperedResult = verifyHMAC(tampered, sharedKey, hmacTag);
        System.out.println("Tampered:   " + tampered);
        System.out.println("Message accepted: " + tamperedResult); // false
        System.out.println("HMAC correctly rejected the tampered message.");
        System.out.println();

        // ─── Verification: wrong key ─────────────────────────────────────────────
        System.out.println("--- Verification: Wrong Key ---");
        byte[] wrongKey = "WrongKey12345678".getBytes();
        boolean wrongKeyResult = verifyHMAC(message, wrongKey, hmacTag);
        System.out.println("Message accepted with wrong key: " + wrongKeyResult); // false
        System.out.println();

        // ─── Authentication spectrum ─────────────────────────────────────────────
        System.out.println("--- Authentication Spectrum ---");
        System.out.println("Hash              → integrity only,  no key,       anyone can forge");
        System.out.println("HMAC              → integrity + auth, symmetric key, fast");
        System.out.println("Digital Signature → integrity + auth + non-repudiation, asymmetric keys");
        System.out.println();
        System.out.println("Use HMAC when: both parties share a secret key (e.g., API authentication).");
        System.out.println("Use signatures when: receiver should not be able to forge proofs (e.g., TLS).");
    }

    /**
     * Computes HMAC-SHA256 of the message using the provided key.
     *
     * HMAC construction: HMAC(K, m) = H((K XOR opad) || H((K XOR ipad) || m))
     * This makes it resistant to length-extension attacks that affect plain hashes.
     */
    public static byte[] computeHMAC(String message, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(message.getBytes());
    }

    /**
     * Verifies HMAC using constant-time comparison to prevent timing attacks.
     * Regular equals() can leak how many bytes matched through execution time.
     */
    public static boolean verifyHMAC(String message, byte[] key, byte[] expectedTag) throws Exception {
        byte[] actualTag = computeHMAC(message, key);
        // MessageDigest.isEqual compares all bytes in constant time regardless of mismatch position
        return MessageDigest.isEqual(actualTag, expectedTag);
    }

    public static byte[] sha256(String data) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(data.getBytes());
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
