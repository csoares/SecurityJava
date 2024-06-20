package security.encryption.asymmetric;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;


/*

Key Components of the Code
KeyPair Generation:

KeyPairGenerator is used to generate a public and private key for RSA. The key size of 2048 bits is recommended as it offers good security.
Encryption:

The public key is used to encrypt the plaintext. The Cipher instance is initialized in encrypt mode with the public key.
The plaintext is converted to bytes and encrypted. The result is then encoded in Base64 to ensure it consists only of printable characters.
Decryption:

The private key is used to decrypt the ciphertext. The Cipher instance is initialized in decrypt mode with the private key.
The ciphertext, which is in Base64 format, is decoded back into bytes and then decrypted.
Exception Handling:

The methods include throws declarations for handling exceptions related to cryptographic operations.
This Java program effectively demonstrates the use of RSA for asymmetric encryption and decryption, which is pivotal for securing communications over untrusted networks. It encapsulates the core concepts of key management, public key infrastructure (PKI), and the mechanics of RSA encryption and decryption processes. This example will be valuable in an educational context, especially in illustrating the differences between symmetric and asymmetric encryption.


 */

public class AsymmetricEncryptionExample {

    public static void main(String[] args) {
        try {
            // Generate RSA key pair
            KeyPair keyPair = generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // Example text to be encrypted and decrypted
            String plainText = "Hello, world! This is a test message.";

            // Encrypting the text using the public key
            String cipherText = encrypt(plainText, publicKey);
            System.out.println("Encrypted (cipherText): " + cipherText);

            // Decrypting the text using the private key
            String decryptedText = decrypt(cipherText, privateKey);
            System.out.println("Decrypted (plainText): " + decryptedText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes));
    }
}
