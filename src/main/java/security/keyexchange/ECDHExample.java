package security.keyexchange;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * Elliptic Curve Diffie-Hellman (ECDH) Key Exchange
 *
 * Same goal as classic DH: two parties derive the same shared secret over
 * an insecure channel without ever transmitting it.
 *
 * Difference: uses elliptic curve math instead of modular exponentiation.
 *
 * Key advantage:
 *   DH 2048-bit   ≈ ECDH 224-bit   (same security, 8-9× smaller keys)
 *   ECDH-P256 keys are ~32 bytes vs DH-2048 keys at ~256 bytes
 *
 * Used in: TLS 1.3, Signal Protocol, WhatsApp, iMessage, HTTPS everywhere.
 */
public class ECDHExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Elliptic Curve Diffie-Hellman (ECDH) Key Exchange ===");
        System.out.println();
        System.out.println("Same goal as classic DH: agree on a shared secret over an insecure channel.");
        System.out.println("Uses elliptic curve group operations instead of modular exponentiation.");
        System.out.println();

        // ─── Key size comparison ─────────────────────────────────────────────────
        System.out.println("--- Key Size Comparison: Classic DH vs ECDH ---");
        System.out.println("Security level  |  Classic DH key size  |  ECDH key size");
        System.out.println("112-bit         |  2048 bits (~256 B)   |  224 bits (~28 B)");
        System.out.println("128-bit         |  3072 bits (~384 B)   |  256 bits (~32 B)");
        System.out.println("256-bit         |  15360 bits (~1920 B) |  521 bits (~66 B)");
        System.out.println("Smaller keys = faster handshakes, less bandwidth, important for mobile/IoT.");
        System.out.println();

        // ─── Alice generates EC key pair ─────────────────────────────────────────
        System.out.println("--- Step 1: Alice generates EC key pair on curve P-256 ---");
        KeyPairGenerator aliceKpg = KeyPairGenerator.getInstance("EC");
        aliceKpg.initialize(new ECGenParameterSpec("secp256r1")); // NIST P-256
        KeyPair aliceKp = aliceKpg.generateKeyPair();
        System.out.println("Alice private key: " + aliceKp.getPrivate().getEncoded().length + " bytes [SECRET]");
        System.out.println("Alice public key:  " + aliceKp.getPublic().getEncoded().length + " bytes [Sent to Bob]");
        System.out.println();

        // ─── Bob generates EC key pair ────────────────────────────────────────────
        System.out.println("--- Step 2: Bob generates EC key pair on the same curve ---");
        KeyPairGenerator bobKpg = KeyPairGenerator.getInstance("EC");
        bobKpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair bobKp = bobKpg.generateKeyPair();
        System.out.println("Bob private key:  " + bobKp.getPrivate().getEncoded().length + " bytes [SECRET]");
        System.out.println("Bob public key:   " + bobKp.getPublic().getEncoded().length + " bytes [Sent to Alice]");
        System.out.println();

        // ─── Compute shared secrets ───────────────────────────────────────────────
        System.out.println("--- Step 3: Both compute the shared secret independently ---");
        System.out.println("Alice: ECDH(alicePrivate, bobPublic)");
        System.out.println("Bob:   ECDH(bobPrivate, alicePublic)");
        System.out.println("Math guarantee: both arrive at the same point on the elliptic curve.");
        System.out.println();

        KeyAgreement aliceKa = KeyAgreement.getInstance("ECDH");
        aliceKa.init(aliceKp.getPrivate());
        aliceKa.doPhase(bobKp.getPublic(), true);
        byte[] aliceSecret = aliceKa.generateSecret();

        KeyAgreement bobKa = KeyAgreement.getInstance("ECDH");
        bobKa.init(bobKp.getPrivate());
        bobKa.doPhase(aliceKp.getPublic(), true);
        byte[] bobSecret = bobKa.generateSecret();

        System.out.println("Alice's shared secret: " + bytesToHex(aliceSecret));
        System.out.println("Bob's shared secret:   " + bytesToHex(bobSecret));
        System.out.println("Shared secrets match:  " + Arrays.equals(aliceSecret, bobSecret));
        System.out.println();

        System.out.println("--- Why Eve cannot compute the shared secret ---");
        System.out.println("Eve sees: Alice's public key (a point P on the curve),");
        System.out.println("         Bob's public key   (a point Q on the curve).");
        System.out.println("To compute the secret, Eve needs Alice's private scalar a such that P = a*G.");
        System.out.println("This is the Elliptic Curve Discrete Logarithm Problem (ECDLP).");
        System.out.println("No efficient algorithm exists for properly chosen curves (e.g., P-256).");
        System.out.println();

        System.out.println("--- Practical usage ---");
        System.out.println("The raw ECDH output (a curve point x-coordinate) is input to a KDF.");
        System.out.println("TLS 1.3 uses HKDF to derive session keys from the ECDH shared secret.");
        System.out.println("Ephemeral ECDH (ECDHE) generates a fresh key pair per session for");
        System.out.println("Forward Secrecy: past sessions stay private even if a long-term key leaks.");
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
