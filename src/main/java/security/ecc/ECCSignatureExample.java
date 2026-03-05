package security.ecc;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * Elliptic Curve Digital Signature Algorithm (ECDSA)
 *
 * Same purpose as RSA signatures: prove that a message was created by the
 * holder of a specific private key, and that it has not been altered.
 *
 * Key advantage over RSA:
 *   ECDSA P-256  ≈ RSA-3072 in security, with ~8x smaller keys
 *   Faster to generate keys, sign, and verify
 *
 * Used in: Bitcoin/Ethereum (secp256k1), TLS, JWT (ES256), SSH, code signing.
 */
public class ECCSignatureExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Elliptic Curve Digital Signature Algorithm (ECDSA) ===");
        System.out.println();
        System.out.println("Goal: Sign data with a private key so anyone with the public key");
        System.out.println("      can verify authenticity and detect tampering.");
        System.out.println();

        // ─── Key size comparison ─────────────────────────────────────────────────
        System.out.println("--- Key Size Comparison: RSA-2048 vs ECDSA P-256 ---");

        KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
        rsaGen.initialize(2048);
        KeyPair rsaKp = rsaGen.generateKeyPair();

        KeyPairGenerator ecGen = KeyPairGenerator.getInstance("EC");
        ecGen.initialize(new ECGenParameterSpec("secp256r1")); // NIST P-256
        KeyPair ecKp = ecGen.generateKeyPair();

        System.out.println("RSA-2048  private key: " + rsaKp.getPrivate().getEncoded().length + " bytes");
        System.out.println("RSA-2048  public key:  " + rsaKp.getPublic().getEncoded().length + " bytes");
        System.out.println("ECDSA P-256 private:   " + ecKp.getPrivate().getEncoded().length + " bytes");
        System.out.println("ECDSA P-256 public:    " + ecKp.getPublic().getEncoded().length + " bytes");
        System.out.println("ECC keys are significantly smaller with equivalent or better security.");
        System.out.println();

        // ─── Sign a message ──────────────────────────────────────────────────────
        String message = "This document certifies the transfer of 100 shares of ACME Corp.";
        System.out.println("--- Signing with ECDSA (private key) ---");
        System.out.println("Message: " + message);
        System.out.println();

        byte[] signature = ecSign(message, ecKp.getPrivate());
        System.out.println("Signature (Base64): " + Base64.getEncoder().encodeToString(signature));
        System.out.println("Signature size:     " + signature.length + " bytes");
        System.out.println("(ECDSA signature size ≈ 2 × key size = ~64 bytes for P-256)");
        System.out.println();

        // ─── Verify correct message ──────────────────────────────────────────────
        System.out.println("--- Verification with public key ---");
        boolean valid = ecVerify(message, signature, ecKp.getPublic());
        System.out.println("Original message:     valid=" + valid); // true

        // ─── Verify tampered message ─────────────────────────────────────────────
        String tampered = "This document certifies the transfer of 9999 shares of ACME Corp.";
        boolean tamperedResult = ecVerify(tampered, signature, ecKp.getPublic());
        System.out.println("Tampered message:     valid=" + tamperedResult); // false
        System.out.println("Tampered message correctly rejected by signature verification.");
        System.out.println();

        // ─── Verify with wrong key ───────────────────────────────────────────────
        KeyPair wrongKp = ecGen.generateKeyPair();
        boolean wrongKeyResult = ecVerify(message, signature, wrongKp.getPublic());
        System.out.println("Correct message, wrong public key: valid=" + wrongKeyResult); // false
        System.out.println();

        // ─── How it works ────────────────────────────────────────────────────────
        System.out.println("--- How ECDSA Works ---");
        System.out.println("Sign:   hash(message) → ECDSA(hash, privateKey) → (r, s) signature pair");
        System.out.println("Verify: hash(message) → ECDSA_verify(hash, (r,s), publicKey) → true/false");
        System.out.println("The private key is never transmitted. Only the (r,s) pair is shared.");
        System.out.println();

        System.out.println("--- Real-world uses ---");
        System.out.println("Bitcoin/Ethereum: secp256k1 curve (similar to P-256) signs transactions");
        System.out.println("TLS certificates:  ECDSA certs are smaller and faster than RSA");
        System.out.println("JWT (ES256):        compact, signed tokens for APIs");
        System.out.println("SSH:                ecdsa-sha2-nistp256 key type");
        System.out.println("Android APK:        code signing uses ECDSA");
    }

    /**
     * Signs a message using ECDSA with SHA-256.
     * The message is hashed with SHA-256 first, then the hash is signed.
     *
     * @param message    the plaintext message to sign
     * @param privateKey the signer's EC private key
     * @return DER-encoded signature bytes (r, s pair)
     */
    public static byte[] ecSign(String message, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(message.getBytes());
        return signer.sign();
    }

    /**
     * Verifies an ECDSA signature using the corresponding public key.
     *
     * @param message   the plaintext message that was signed
     * @param signature the DER-encoded signature to verify
     * @param publicKey the signer's EC public key
     * @return true if the signature is valid for this message and key
     */
    public static boolean ecVerify(String message, byte[] signature, PublicKey publicKey) throws Exception {
        Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(publicKey);
        verifier.update(message.getBytes());
        return verifier.verify(signature);
    }
}
