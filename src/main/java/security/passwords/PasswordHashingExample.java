package security.passwords;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * Password Hashing: From Insecure to Secure
 *
 * Passwords must NEVER be stored in plain text or as raw hashes.
 * This example shows three stages from worst to best practice.
 *
 * Stage 1 — Plain SHA-256:    fast, vulnerable to rainbow tables and brute force
 * Stage 2 — SHA-256 + salt:   defeats precomputed tables; still too fast
 * Stage 3 — PBKDF2:           intentionally slow; current OWASP recommendation
 *
 * For new systems: prefer bcrypt or Argon2 (memory-hard, GPU/ASIC resistant).
 * PBKDF2 is the Java standard library option and is widely accepted.
 */
public class PasswordHashingExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Password Hashing: From Insecure to Secure ===");
        System.out.println();

        String password = "MySecretPassword123";

        // ─── Stage 1: Plain SHA-256 (INSECURE) ──────────────────────────────────
        System.out.println("--- Stage 1: Plain SHA-256 (INSECURE) ---");
        byte[] plainHash = sha256(password);
        System.out.println("Password:  " + password);
        System.out.println("SHA-256:   " + bytesToHex(plainHash));
        System.out.println("Problem 1: Same password → same hash. Two users with identical passwords");
        System.out.println("           are exposed when either account is cracked.");
        System.out.println("Problem 2: Precomputed rainbow tables instantly reveal common passwords.");
        System.out.println("Problem 3: SHA-256 runs billions of iterations per second on a GPU.");
        System.out.println("           Brute force of short passwords takes minutes.");
        System.out.println();

        // ─── Stage 2: SHA-256 + unique salt (BETTER) ────────────────────────────
        System.out.println("--- Stage 2: SHA-256 + random salt (BETTER, still not ideal) ---");
        byte[] salt = generateSalt();
        byte[] saltedHash = sha256WithSalt(password, salt);
        System.out.println("Salt:      " + bytesToHex(salt));
        System.out.println("Hash:      " + bytesToHex(saltedHash));
        System.out.println("Fix:       Each user gets a unique salt → rainbow tables defeated.");
        System.out.println("           Attacker must brute-force each user's password separately.");
        System.out.println("Still:     SHA-256 is still too fast. A GPU can try billions per second.");
        System.out.println();

        // ─── Stage 3: PBKDF2 (RECOMMENDED — Java standard library) ──────────────
        System.out.println("--- Stage 3: PBKDF2WithHmacSHA256 (RECOMMENDED) ---");
        byte[] pbkdf2Salt = generateSalt();
        // OWASP recommends 600,000 iterations for PBKDF2-SHA256 (2023)
        // 310,000 is the minimum. Increase over time as hardware improves.
        int iterations = 310_000;
        System.out.println("Computing PBKDF2 hash (intentionally slow — please wait)...");
        long start = System.currentTimeMillis();
        byte[] pbkdf2Hash = pbkdf2(password, pbkdf2Salt, iterations);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Salt:       " + bytesToHex(pbkdf2Salt));
        System.out.println("Iterations: " + iterations);
        System.out.println("Hash:       " + bytesToHex(pbkdf2Hash));
        System.out.printf ("Computed in %d ms on this CPU.%n", elapsed);
        System.out.println("At " + iterations + " iterations, GPU brute force drops from billions/sec");
        System.out.println("(SHA-256) to a few thousand/sec — a difference of ~10^6 in cost.");
        System.out.println();

        // ─── Simulated login ─────────────────────────────────────────────────────
        System.out.println("--- Simulated Login ---");
        System.out.println("[REGISTRATION] Stored in database: {salt, iterations, hash} — NOT the password");
        System.out.println();

        String correctAttempt = "MySecretPassword123";
        boolean login1 = pbkdf2Verify(correctAttempt, pbkdf2Salt, iterations, pbkdf2Hash);
        System.out.println("[LOGIN] '" + correctAttempt + "' → " + (login1 ? "ACCESS GRANTED" : "REJECTED"));

        String wrongAttempt = "WrongPassword";
        boolean login2 = pbkdf2Verify(wrongAttempt, pbkdf2Salt, iterations, pbkdf2Hash);
        System.out.println("[LOGIN] '" + wrongAttempt + "' → " + (login2 ? "ACCESS GRANTED" : "REJECTED"));
        System.out.println();

        System.out.println("--- Recommendations ---");
        System.out.println("PBKDF2   — Java built-in, FIPS compliant, OWASP approved.");
        System.out.println("bcrypt   — widely used, automatic work factor, good library support.");
        System.out.println("Argon2id — memory-hard, best resistance against GPU/ASIC cracking (2024 OWASP #1).");
        System.out.println("Always increase iterations/work factor as hardware gets faster.");
    }

    /**
     * Simple SHA-256 hash — never use alone for passwords.
     */
    public static byte[] sha256(String password) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(password.getBytes());
    }

    /**
     * Generates a cryptographically random 16-byte salt.
     * Must be stored alongside the hash — different per user.
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * SHA-256 with a salt prepended — defeats rainbow tables but still fast.
     */
    public static byte[] sha256WithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt); // mix salt into hash state before hashing password
        return md.digest(password.getBytes());
    }

    /**
     * PBKDF2WithHmacSHA256: applies HMAC-SHA256 'iterations' times.
     * Deliberately slow by design — each iteration adds computational cost.
     *
     * @param password   plaintext password to hash
     * @param salt       random salt unique per user
     * @param iterations number of rounds (OWASP min: 310,000 for PBKDF2-SHA256)
     * @return 256-bit derived key suitable for password storage
     */
    public static byte[] pbkdf2(String password, byte[] salt, int iterations)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * Verifies a login attempt using constant-time comparison to prevent timing attacks.
     */
    public static boolean pbkdf2Verify(String password, byte[] salt, int iterations, byte[] expectedHash)
            throws Exception {
        byte[] actualHash = pbkdf2(password, salt, iterations);
        return MessageDigest.isEqual(actualHash, expectedHash);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
