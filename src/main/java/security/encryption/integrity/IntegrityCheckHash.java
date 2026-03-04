package security.encryption.integrity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
/*
This example uses the SHA-256 hash function to compute the hash of the original data and verifies it to ensure data integrity.

Hash Function (SHA-256): Used for quick integrity checks but doesn't confirm data authenticity or the sender's identity.
 */
public class IntegrityCheckHash {

    public static void main(String[] args) {
        try {
            // STEP 1: Compute hash of original data
            // A hash is a fixed-size "fingerprint" of data
            // Even a tiny change in the data produces a completely different hash
            String originalData = "Hello, this is a sample text!";
            byte[] originalHash = computeSHA256(originalData);

            // Display the hash in hexadecimal format (human-readable)
            System.out.println("Original Hash: " + bytesToHex(originalHash));

            // STEP 2: Simulate receiving data
            // In real scenarios, this data would come from an external source
            // We're using the same data to demonstrate a successful integrity check
            String receivedData = "Hello, this is a sample text!";
            byte[] receivedHash = computeSHA256(receivedData);

            System.out.println("Received Hash: " + bytesToHex(receivedHash));

            // STEP 3: Verify data integrity by comparing hashes
            // If hashes match -> data is unchanged
            // If hashes differ -> data has been modified (corrupted or tampered)
            // NOTE: Hash functions verify integrity but NOT authenticity
            // (i.e., we know IF data changed, but not WHO sent it)
            if (Arrays.equals(originalHash, receivedHash)) {
                System.out.println("Data Integrity Verified: Data is intact.");
            } else {
                System.out.println("Data Integrity Verification Failed: Data has been altered.");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Computes the SHA-256 hash of the input data
     *
     * SHA-256 properties:
     * - Deterministic: same input always produces same hash
     * - Fast to compute
     * - Irreversible: cannot recover original data from hash
     * - Collision-resistant: extremely unlikely two different inputs produce same hash
     * - Avalanche effect: small change in input = completely different hash
     *
     * @param data The string to hash
     * @return The SHA-256 hash as a byte array (32 bytes / 256 bits)
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    public static byte[] computeSHA256(String data) throws NoSuchAlgorithmException {
        // Create a MessageDigest instance for SHA-256 algorithm
        // SHA-256 produces a 256-bit (32-byte) hash value
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Compute and return the hash
        // digest() method performs the actual hashing operation
        return md.digest(data.getBytes());
    }

    /**
     * Converts a byte array to a hexadecimal string representation
     *
     * This makes binary hash values human-readable
     * Example: byte[3] {15, 255, 0} -> "0fff00"
     *
     * @param bytes The byte array to convert
     * @return Hexadecimal string representation
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        // Convert each byte to a 2-character hex string
        for (byte b : bytes) {
            // %02x formats each byte as 2-digit hexadecimal (with leading zero if needed)
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
