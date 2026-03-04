package security.encryption.integrity;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.Base64;

/*
 * FILE DIGITAL SIGNATURE
 *
 * This example demonstrates how digital signatures are applied to files:
 *
 *   1. SIGN: Compute SHA-256 hash of the file content, then encrypt that hash
 *            with the sender's private key → produces a .sig file.
 *
 *   2. VERIFY: Recompute the SHA-256 hash of the (received) file, then decrypt
 *              the signature with the sender's public key and compare both hashes.
 *              - If they match  → file is intact AND came from the key owner.
 *              - If they differ → file was tampered OR the key is wrong.
 *
 * Algorithm: SHA256withRSA  (hash with SHA-256, sign/verify with RSA 2048-bit)
 *
 * Why hash first?
 *   RSA can only sign small payloads. Hashing reduces the file to a fixed 32-byte
 *   fingerprint, making the operation fast regardless of file size.
 */
public class FileDigitalSignature {

    public static void main(String[] args) throws Exception {

        // ── STEP 1: Generate RSA key pair ────────────────────────────────────
        // In a real scenario the sender keeps the private key and distributes
        // the public key to anyone who needs to verify their files.
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair       = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey  publicKey  = keyPair.getPublic();
        System.out.println("RSA 2048-bit key pair generated.");

        // ── STEP 2: Create a sample file to sign ────────────────────────────
        Path filePath = Files.createTempFile("document", ".txt");
        Files.writeString(filePath, "This is the content of the file that will be signed.");
        System.out.println("File created : " + filePath);
        System.out.println("Content      : " + Files.readString(filePath));

        // ── STEP 3: Compute SHA-256 hash of the file ─────────────────────────
        // The hash is a unique fingerprint — any change to the file, even a
        // single character, produces a completely different hash value.
        byte[] fileHash = computeSHA256(filePath);
        System.out.println("\nSHA-256 hash of file : " + bytesToHex(fileHash));

        // ── STEP 4: Sign the hash with the private key ───────────────────────
        // The signature proves two things:
        //   • Authenticity  – only the holder of the private key can produce it.
        //   • Integrity     – the hash embedded in the signature must match the
        //                     hash recomputed from the received file.
        byte[] signature = signHash(fileHash, privateKey);
        Path sigPath     = filePath.resolveSibling(filePath.getFileName() + ".sig");
        Files.write(sigPath, signature);
        System.out.println("Signature    : " + Base64.getEncoder().encodeToString(signature));
        System.out.println("Signature saved to: " + sigPath);

        // ── STEP 5: Verify the signature (file unchanged) ────────────────────
        System.out.println("\n--- Verification (original file) ---");
        boolean valid = verifySignature(filePath, signature, publicKey);
        System.out.println("Signature valid: " + valid);   // expected: true

        // ── STEP 6: Simulate file tampering and re-verify ────────────────────
        // An attacker modifies the file after it was signed.
        System.out.println("\n--- Tampering with the file content ---");
        Files.writeString(filePath, "This content was MODIFIED by an attacker!");
        System.out.println("New content  : " + Files.readString(filePath));

        byte[] tamperedHash = computeSHA256(filePath);
        System.out.println("SHA-256 hash of tampered file: " + bytesToHex(tamperedHash));

        System.out.println("\n--- Verification (tampered file) ---");
        boolean validAfterTamper = verifySignature(filePath, signature, publicKey);
        System.out.println("Signature valid: " + validAfterTamper);  // expected: false
        if (!validAfterTamper) {
            System.out.println("ALERT: File integrity check FAILED. The file has been tampered with!");
        }

        // Clean up temp files
        Files.deleteIfExists(filePath);
        Files.deleteIfExists(sigPath);
    }

    /**
     * Computes the SHA-256 hash of a file's content.
     *
     * @param filePath path to the file
     * @return 32-byte SHA-256 digest of the file
     */
    public static byte[] computeSHA256(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(filePath);
        return digest.digest(fileBytes);
    }

    /**
     * Signs a hash using the RSA private key.
     *
     * The Signature API internally performs SHA-256 hashing again — but since
     * we pass an already-hashed value, what matters is that we use the same
     * algorithm on both ends.  Here we use "SHA256withRSA", which hashes the
     * input with SHA-256 and then RSA-encrypts the result with the private key.
     *
     * @param hash       the file hash to sign
     * @param privateKey the RSA private key
     * @return the digital signature bytes
     */
    public static byte[] signHash(byte[] hash, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(hash);
        return signer.sign();
    }

    /**
     * Verifies the digital signature of a file using the RSA public key.
     *
     * Process:
     *   1. Recompute the SHA-256 hash of the file.
     *   2. Decrypt the signature with the public key to recover the original hash.
     *   3. Compare both hashes — they must be identical.
     *
     * @param filePath  path to the file being verified
     * @param signature the digital signature to check
     * @param publicKey the RSA public key of the signer
     * @return true if the file is intact and the signature is authentic
     */
    public static boolean verifySignature(Path filePath, byte[] signature, PublicKey publicKey)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] fileHash = computeSHA256(filePath);

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(fileHash);
        return verifier.verify(signature);
    }

    /**
     * Converts a byte array to a lowercase hexadecimal string for display.
     *
     * @param bytes the byte array to convert
     * @return hexadecimal string representation
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
