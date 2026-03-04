package security.encryption.integrity;

import java.security.*;
import java.util.Base64;


/*

This example uses RSA for digital signatures which combines the use of a private key (to sign data) and a public key (to verify signatures), thereby validating both the integrity and the authenticity of the data.

Digital Signatures (RSA with SHA-256): Provides both integrity and authenticity verification using a pair of cryptographic keys.


 */
public class IntegrityCheckSignature {

    public static void main(String[] args) {
        try {
            // STEP 1: Generate RSA key pair
            // RSA is an asymmetric encryption algorithm that uses two keys:
            // - Private key: kept secret, used to sign data
            // - Public key: shared publicly, used to verify signatures
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

            // Initialize with 2048-bit key size (industry standard for security)
            // Larger key sizes = more secure but slower performance
            keyGen.initialize(2048);

            // Generate the actual key pair
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();  // Used for signing
            PublicKey publicKey = pair.getPublic();      // Used for verification

            // STEP 2: Sign the original data
            // In real scenarios, this would be the sender creating a signature
            String originalData = "Hello, this is a sample text!";

            // Convert string to bytes and sign it using the private key
            // The signature is like a "seal" that proves this data came from the owner of the private key
            byte[] signature = signData(originalData.getBytes(), privateKey);

            // Display the signature in Base64 format (makes binary data readable)
            System.out.println("Signature: " + Base64.getEncoder().encodeToString(signature));

            // STEP 3: Simulate receiving data
            // In a real application, this data and signature would come from another party
            String receivedData = "Hello, this is a sample text!";

            // STEP 4: Verify the signature with the public key
            // This checks two things:
            // 1. Data integrity: Has the data been modified?
            // 2. Authenticity: Was this signed by the owner of the private key?
            boolean isCorrect = verifySignature(receivedData.getBytes(), signature, publicKey);

            if (isCorrect) {
                System.out.println("Data Integrity and Authenticity Verified: Signature matches.");
            } else {
                System.out.println("Verification Failed: Data or signature tampered.");
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            // Handle cryptographic exceptions
            e.printStackTrace();
        }
    }

    /**
     * Signs data using a private key
     *
     * @param data The data to be signed (as byte array)
     * @param privateKey The private key used for signing
     * @return The digital signature as a byte array
     */
    public static byte[] signData(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // Create a Signature instance with SHA-256 hash function and RSA algorithm
        // SHA256withRSA means: first hash the data with SHA-256, then sign the hash with RSA
        Signature rsa = Signature.getInstance("SHA256withRSA");

        // Initialize the signature object for signing mode with the private key
        rsa.initSign(privateKey);

        // Feed the data to be signed into the signature object
        rsa.update(data);

        // Generate and return the actual signature
        // This creates a unique signature that can only be created with this private key
        return rsa.sign();
    }

    /**
     * Verifies a digital signature using a public key
     *
     * @param data The original data that was signed
     * @param signature The signature to verify
     * @param publicKey The public key used for verification
     * @return true if signature is valid, false otherwise
     */
    public static boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // Create a Signature instance with the same algorithm used for signing
        Signature sig = Signature.getInstance("SHA256withRSA");

        // Initialize the signature object for verification mode with the public key
        sig.initVerify(publicKey);

        // Feed the received data into the signature object
        sig.update(data);

        // Verify the signature against the data
        // Returns true if the signature was created by the corresponding private key
        // and the data has not been modified
        return sig.verify(signature);
    }
}
