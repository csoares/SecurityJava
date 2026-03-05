package security.attacks;

import java.security.MessageDigest;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * Timing Attack Demonstration
 *
 * A timing attack is a side-channel attack that extracts secret information
 * by measuring how long an operation takes to execute.
 *
 * Classic vulnerability: string comparison with early exit.
 *   "abc".equals("abd") → checks 'a'==>'a', 'b'==>'b', 'c'==>'d' → returns false at position 2
 *   "abc".equals("xyz") → checks 'a'==>'x' → returns false immediately at position 0
 *
 * An attacker who can measure execution time can infer:
 *   - How many characters of their guess matched
 *   - Progressively narrow down the correct value character by character
 *
 * Fix: constant-time comparison that always examines every byte.
 */
public class TimingAttackExample {

    // A simulated stored secret token (32 hex chars = 128-bit token)
    private static final String STORED_TOKEN = "a3f8c1d2e4b5067891abcdef01234567";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Timing Attack on String Comparison ===");
        System.out.println();
        System.out.println("Secret token stored on server: \"" + STORED_TOKEN + "\"");
        System.out.println("An attacker submits token guesses and measures response times.");
        System.out.println();

        // ─── Vulnerable comparison ───────────────────────────────────────────────
        System.out.println("--- Vulnerable: String.equals() (early exit) ---");
        System.out.println("Returns false as soon as any character mismatch is found.");
        System.out.println("Comparing more characters takes more time → timing leak.");
        System.out.println();

        // Candidate A: wrong at position 0 (exits immediately)
        String wrongAtStart = "0" + STORED_TOKEN.substring(1);
        // Candidate B: wrong at position 31 (compares all but last char)
        String wrongAtEnd = STORED_TOKEN.substring(0, 31) + "0";

        int rounds = 500_000;

        long vulnTimeStart = measureVulnerable(STORED_TOKEN, wrongAtStart, rounds);
        long vulnTimeEnd   = measureVulnerable(STORED_TOKEN, wrongAtEnd,   rounds);

        System.out.printf("Vulnerable comparison (%,d rounds):%n", rounds);
        System.out.printf("  Wrong at position  0: %,d ns%n", vulnTimeStart);
        System.out.printf("  Wrong at position 31: %,d ns%n", vulnTimeEnd);
        System.out.printf("  Difference:           %,d ns%n", vulnTimeEnd - vulnTimeStart);
        System.out.println("  Larger time for 'wrong-at-end' reveals more characters matched.");
        System.out.println("  An attacker can exploit this difference to guess one character at a time.");
        System.out.println();

        // ─── Constant-time comparison ────────────────────────────────────────────
        System.out.println("--- Secure: MessageDigest.isEqual() (constant time) ---");
        System.out.println("Examines ALL bytes regardless of where mismatch occurs.");
        System.out.println("Execution time is independent of input content.");
        System.out.println();

        long safeTimeStart = measureSecure(STORED_TOKEN.getBytes(), wrongAtStart.getBytes(), rounds);
        long safeTimeEnd   = measureSecure(STORED_TOKEN.getBytes(), wrongAtEnd.getBytes(),   rounds);

        System.out.printf("Secure comparison (%,d rounds):%n", rounds);
        System.out.printf("  Wrong at position  0: %,d ns%n", safeTimeStart);
        System.out.printf("  Wrong at position 31: %,d ns%n", safeTimeEnd);
        System.out.printf("  Difference:           %,d ns%n", Math.abs(safeTimeEnd - safeTimeStart));
        System.out.println("  Times should be approximately equal — no exploitable timing signal.");
        System.out.println();

        // ─── Rule ────────────────────────────────────────────────────────────────
        System.out.println("--- Rule ---");
        System.out.println("ALWAYS use MessageDigest.isEqual() when comparing:");
        System.out.println("  - Password hashes");
        System.out.println("  - HMAC tags");
        System.out.println("  - Session tokens, API keys, CSRF tokens");
        System.out.println("  - Any security-sensitive byte sequence");
        System.out.println();
        System.out.println("NEVER use String.equals(), Arrays.equals(), or == for secret comparison.");
        System.out.println("Even indirect leaks (HTTP response time) can be measured by a remote attacker.");
    }

    /**
     * Measures total time for 'rounds' comparisons using the vulnerable equals() method.
     * This leaks timing information proportional to the number of matching characters.
     */
    public static long measureVulnerable(String reference, String candidate, int rounds) {
        // Warm up JIT
        for (int i = 0; i < 10_000; i++) reference.equals(candidate);

        long start = System.nanoTime();
        for (int i = 0; i < rounds; i++) {
            reference.equals(candidate); // early exit on first mismatch — vulnerable
        }
        return System.nanoTime() - start;
    }

    /**
     * Measures total time for 'rounds' comparisons using constant-time MessageDigest.isEqual().
     * Time is independent of where the mismatch occurs.
     */
    public static long measureSecure(byte[] reference, byte[] candidate, int rounds) {
        // Warm up JIT
        for (int i = 0; i < 10_000; i++) MessageDigest.isEqual(reference, candidate);

        long start = System.nanoTime();
        for (int i = 0; i < rounds; i++) {
            MessageDigest.isEqual(reference, candidate); // constant-time — secure
        }
        return System.nanoTime() - start;
    }
}
