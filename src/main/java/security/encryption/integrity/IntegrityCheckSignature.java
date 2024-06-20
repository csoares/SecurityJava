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
            // Generate RSA key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            // Original data
            String originalData = "Hello, this is a sample text!";
            byte[] signature = signData(originalData.getBytes(), privateKey);

            System.out.println("Signature: " + Base64.getEncoder().encodeToString(signature));

            // Let's assume the received data is as follows
            String receivedData = "Hello, this is a sample text!";

            // Verify the signature with the public key
            boolean isCorrect = verifySignature(receivedData.getBytes(), signature, publicKey);
            if (isCorrect) {
                System.out.println("Data Integrity and Authenticity Verified: Signature matches.");
            } else {
                System.out.println("Verification Failed: Data or signature tampered.");
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

    public static byte[] signData(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature rsa = Signature.getInstance("SHA256withRSA");
        rsa.initSign(privateKey);
        rsa.update(data);
        return rsa.sign();
    }

    public static boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }
}
