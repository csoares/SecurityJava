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

    DIAGRAM: see README.md in this package for visual diagrams of this class.
 */

public class AsymmetricEncryptionExample {

    public static void main(String[] args) {
        try {
            // STEP 1: Generate RSA key pair
            // RSA is an asymmetric encryption algorithm using TWO different keys:
            // - Public key: can be shared with anyone, used to ENCRYPT data
            // - Private key: kept secret, used to DECRYPT data
            // Key difference from symmetric: different keys for encryption and decryption!
            KeyPair keyPair = generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();      // Share this with others
            PrivateKey privateKey = keyPair.getPrivate();   // Keep this secret!

            // STEP 2: Prepare the message to encrypt
            String plainText = "Hello, world! This is a test message.";

            // STEP 3: Encrypt using the PUBLIC key
            // Anyone with the public key can encrypt data
            // But only the holder of the private key can decrypt it
            String cipherText = encrypt(plainText, publicKey);
            System.out.println("Encrypted (cipherText): " + cipherText);

            // STEP 4: Decrypt using the PRIVATE key
            // Only the private key can decrypt data that was encrypted with the public key
            String decryptedText = decrypt(cipherText, privateKey);
            System.out.println("Decrypted (plainText): " + decryptedText);

        } catch (Exception e) {
            // Handle any cryptographic exceptions
            e.printStackTrace();
        }
    }

    /**
     * Generates an RSA public-private key pair
     *
     * @return A KeyPair containing both public and private keys
     * @throws NoSuchAlgorithmException if RSA algorithm is not available
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        // Create a key pair generator for the RSA algorithm
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

        // Initialize with 2048-bit key size
        // 2048 bits is the current industry standard for security
        // Larger keys (4096) are more secure but slower
        generator.initialize(2048);

        // Generate and return the key pair (public + private keys)
        return generator.generateKeyPair();
    }

    /**
     * Encrypts plaintext using the PUBLIC key
     *
     * @param plainText The text to encrypt
     * @param publicKey The public key used for encryption
     * @return Base64-encoded encrypted text
     * @throws Exception if encryption fails
     */
    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        // Create a Cipher instance for RSA encryption
        // Note: In production, use "RSA/ECB/OAEPWithSHA-256AndMGF1Padding" for better security
        Cipher encryptCipher = Cipher.getInstance("RSA");

        // Initialize cipher in ENCRYPT mode with the PUBLIC key
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Convert plaintext to bytes and encrypt it
        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes());

        // Encode to Base64 for safe transmission/storage
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Decrypts ciphertext using the PRIVATE key
     *
     * @param cipherText The Base64-encoded encrypted text
     * @param privateKey The private key used for decryption
     * @return The decrypted plaintext
     * @throws Exception if decryption fails
     */
    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        // Decode the Base64 string back to encrypted bytes
        byte[] bytes = Base64.getDecoder().decode(cipherText);

        // Create a Cipher instance for RSA decryption
        Cipher decriptCipher = Cipher.getInstance("RSA");

        // Initialize cipher in DECRYPT mode with the PRIVATE key
        // Only the private key can decrypt data encrypted with the public key
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        // Decrypt the bytes and convert back to a string
        return new String(decriptCipher.doFinal(bytes));
    }
}
