package security.keyexchange;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.util.Arrays;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * Diffie-Hellman Key Exchange
 *
 * Problem: Alice and Bob want to communicate securely.
 * An eavesdropper (Eve) can see EVERY message on the channel.
 * How can they agree on a shared secret without ever transmitting it?
 *
 * DH solves this using modular exponentiation:
 *   - Public parameters: large prime p, generator g (Eve can see these)
 *   - Alice picks private a, sends g^a mod p
 *   - Bob picks private b, sends g^b mod p
 *   - Alice computes (g^b)^a mod p = g^ab mod p
 *   - Bob computes  (g^a)^b mod p = g^ab mod p
 *   - Both get the same value; Eve cannot compute it without solving the
 *     Discrete Logarithm Problem (computationally infeasible for large primes)
 */
public class DiffieHellmanExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Diffie-Hellman Key Exchange ===");
        System.out.println();
        System.out.println("Scenario: Alice and Bob communicate over an insecure channel.");
        System.out.println("Eve sees ALL messages. Goal: establish a shared secret Eve cannot learn.");
        System.out.println();

        // ─── Step 1: Generate shared public parameters ───────────────────────────
        // In practice, standardized DH groups (RFC 3526, RFC 7919) are used.
        // Here we generate parameters for demonstration (slow — ~5-10 seconds).
        System.out.println("--- Step 1: Agree on public DH parameters (g, p) ---");
        System.out.println("Generating 2048-bit DH parameters (this may take a moment)...");
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(2048);
        AlgorithmParameters params = paramGen.generateParameters();
        DHParameterSpec dhSpec = params.getParameterSpec(DHParameterSpec.class);
        System.out.println("Parameters generated. Both parties agree to use these (Eve can see them).");
        System.out.println("p (prime) bit length: " + dhSpec.getP().bitLength());
        System.out.println();

        // ─── Step 2: Each party generates their own key pair ─────────────────────
        System.out.println("--- Step 2: Each party independently generates a key pair ---");
        KeyPairGenerator aliceKpg = KeyPairGenerator.getInstance("DH");
        aliceKpg.initialize(dhSpec);
        KeyPair aliceKp = aliceKpg.generateKeyPair();
        System.out.println("Alice: private key [SECRET, never transmitted]");
        System.out.println("Alice: public key  [Sent to Bob — Eve can see this]");

        KeyPairGenerator bobKpg = KeyPairGenerator.getInstance("DH");
        bobKpg.initialize(dhSpec);
        KeyPair bobKp = bobKpg.generateKeyPair();
        System.out.println("Bob:   private key [SECRET, never transmitted]");
        System.out.println("Bob:   public key  [Sent to Alice — Eve can see this]");
        System.out.println();

        // ─── Step 3: Compute shared secret ───────────────────────────────────────
        System.out.println("--- Step 3: Each party computes the shared secret independently ---");

        // Alice: combine her private key with Bob's public key
        KeyAgreement aliceKa = KeyAgreement.getInstance("DH");
        aliceKa.init(aliceKp.getPrivate());
        aliceKa.doPhase(bobKp.getPublic(), true);
        byte[] aliceSecret = aliceKa.generateSecret();

        // Bob: combine his private key with Alice's public key
        KeyAgreement bobKa = KeyAgreement.getInstance("DH");
        bobKa.init(bobKp.getPrivate());
        bobKa.doPhase(aliceKp.getPublic(), true);
        byte[] bobSecret = bobKa.generateSecret();

        // Both arrive at the same value without ever transmitting it
        System.out.println("Alice's shared secret: " + bytesToHex(aliceSecret).substring(0, 32) + "...");
        System.out.println("Bob's shared secret:   " + bytesToHex(bobSecret).substring(0, 32) + "...");
        System.out.println("Shared secrets match:  " + Arrays.equals(aliceSecret, bobSecret));
        System.out.println();

        System.out.println("--- What Eve saw ---");
        System.out.println("Public parameters (g, p), Alice's public key, Bob's public key.");
        System.out.println("Eve CANNOT compute the shared secret — that requires solving the");
        System.out.println("Discrete Logarithm Problem, which is computationally infeasible at 2048 bits.");
        System.out.println();

        System.out.println("--- Important limitation ---");
        System.out.println("DH provides NO authentication. A man-in-the-middle (MITM) can intercept");
        System.out.println("and replace both public keys. In TLS, DH is combined with digital signatures");
        System.out.println("(from X.509 certificates) to authenticate the server's public key.");
        System.out.println();

        System.out.println("--- Next step in practice ---");
        System.out.println("The raw shared secret is typically fed into a Key Derivation Function (KDF)");
        System.out.println("like HKDF to produce one or more symmetric keys (e.g., AES-256 for TLS).");
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
