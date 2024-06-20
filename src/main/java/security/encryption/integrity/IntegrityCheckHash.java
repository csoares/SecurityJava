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
            String originalData = "Hello, this is a sample text!";
            byte[] originalHash = computeSHA256(originalData);

            System.out.println("Original Hash: " + bytesToHex(originalHash));

            // Simulating data received which is the same as original
            String receivedData = "Hello, this is a sample text!";
            byte[] receivedHash = computeSHA256(receivedData);

            System.out.println("Received Hash: " + bytesToHex(receivedHash));

            // Integrity check
            if (Arrays.equals(originalHash, receivedHash)) {
                System.out.println("Data Integrity Verified: Data is intact.");
            } else {
                System.out.println("Data Integrity Verification Failed: Data has been altered.");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] computeSHA256(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data.getBytes());
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
