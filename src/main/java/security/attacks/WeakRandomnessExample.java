package security.attacks;

import java.security.SecureRandom;
import java.util.Random;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * Weak Randomness in Cryptography
 *
 * Cryptography fundamentally depends on unpredictability.
 * If an attacker can predict or reproduce your random values, they can:
 *   - Recover encryption keys
 *   - Forge session tokens, CSRF tokens, API keys
 *   - Break IV/nonce uniqueness guarantees in AES-GCM
 *
 * java.util.Random / Math.random():
 *   - Linear Congruential Generator (LCG) seeded from current time
 *   - Deterministic: same seed → same sequence
 *   - If attacker knows seed (e.g., login timestamp), they reproduce all output
 *   - NOT suitable for ANY security purpose
 *
 * java.security.SecureRandom:
 *   - Seeded from OS entropy pool (hardware noise, interrupt timing, etc.)
 *   - Cryptographically secure PRNG (CSPRNG)
 *   - Output is computationally indistinguishable from true random
 *   - ALWAYS use for cryptographic operations
 */
public class WeakRandomnessExample {

    public static void main(String[] args) {
        System.out.println("=== Weak Randomness vs. Cryptographically Secure Randomness ===");
        System.out.println();

        // ─── Demo 1: java.util.Random is reproducible ────────────────────────────
        System.out.println("--- java.util.Random: Predictable (NEVER use for security) ---");
        long seed = System.currentTimeMillis();
        System.out.println("Seed (current time in ms): " + seed);
        System.out.println("If an attacker knows when the token was generated, they know the seed.");
        System.out.println();

        Random weakRng = new Random(seed);
        System.out.print("Generated sequence: ");
        int[] generated = new int[6];
        for (int i = 0; i < 6; i++) {
            generated[i] = weakRng.nextInt(10000);
            System.out.print(generated[i] + " ");
        }
        System.out.println();

        // Attacker reproduces the exact sequence with the same seed
        Random attackerRng = new Random(seed);
        System.out.print("Attacker reproduced:  ");
        boolean allMatch = true;
        for (int i = 0; i < 6; i++) {
            int v = attackerRng.nextInt(10000);
            System.out.print(v + " ");
            if (v != generated[i]) allMatch = false;
        }
        System.out.println();
        System.out.println("Sequences identical: " + allMatch);
        System.out.println("The attacker reconstructed every 'random' value perfectly.");
        System.out.println();

        // ─── Demo 2: Seed guessing window ────────────────────────────────────────
        System.out.println("--- How narrow is the seed guessing window? ---");
        System.out.println("System.currentTimeMillis() has millisecond resolution.");
        System.out.println("A user logs in at a known time → ~1000 candidates per second.");
        System.out.println("An attacker can try all seeds in a ±5 second window: ~10,000 guesses.");
        System.out.println("For a token with 10,000 possible values, that's trivially brute-forced.");
        System.out.println();

        // ─── Demo 3: SecureRandom is unpredictable ────────────────────────────────
        System.out.println("--- java.security.SecureRandom: Cryptographically Secure ---");
        System.out.println("Seeded from OS entropy (hardware noise, interrupt timing, disk I/O, etc.)");
        System.out.println("Output is computationally indistinguishable from true randomness.");
        System.out.println();

        SecureRandom sr1 = new SecureRandom();
        SecureRandom sr2 = new SecureRandom(); // different instance, different state
        System.out.print("Instance 1: ");
        for (int i = 0; i < 6; i++) System.out.print(sr1.nextInt(10000) + " ");
        System.out.println();
        System.out.print("Instance 2: ");
        for (int i = 0; i < 6; i++) System.out.print(sr2.nextInt(10000) + " ");
        System.out.println();
        System.out.println("Different each time. Cannot be reproduced by an attacker.");
        System.out.println();

        // ─── Demo 4: Practical token generation ──────────────────────────────────
        System.out.println("--- Practical: Session Token Generation ---");

        System.out.println("WEAK (never do this):");
        // Simulates the anti-pattern of using timestamp-seeded Random for tokens
        String weakToken = Long.toHexString(new Random(System.currentTimeMillis()).nextLong());
        System.out.println("  Token: " + weakToken + "  (only 64-bit space, time-seeded, predictable)");
        System.out.println();

        System.out.println("STRONG (always do this for security tokens):");
        byte[] tokenBytes = new byte[32]; // 256 bits of entropy
        new SecureRandom().nextBytes(tokenBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : tokenBytes) sb.append(String.format("%02x", b));
        System.out.println("  Token: " + sb);
        System.out.println("  32 bytes = 256 bits of entropy. Brute force: 2^256 attempts = infeasible.");
        System.out.println();

        // ─── Rule ────────────────────────────────────────────────────────────────
        System.out.println("--- Rule ---");
        System.out.println("Use SecureRandom for ALL of the following:");
        System.out.println("  - AES keys, RSA key generation");
        System.out.println("  - IVs, nonces, salts");
        System.out.println("  - Session tokens, CSRF tokens, password reset tokens, API keys");
        System.out.println("  - OTP/TOTP secrets");
        System.out.println();
        System.out.println("NEVER use Math.random(), new Random(), or ThreadLocalRandom for security.");
        System.out.println("Note: SecureRandom is thread-safe and can be shared across threads.");
    }
}
