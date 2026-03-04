package security.encryption.symmetric;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Base64;


/*
Key Components of the Code
Key and IV Generation: The AES key is generated using KeyGenerator. The IV is random for each encryption session and is crucial for the security of CBC mode.

Encryption/Decryption Methods: These methods setup the Cipher object for encryption and decryption. The Cipher instance specifies "AES/CBC/PKCS5Padding" which denotes AES algorithm, CBC mode, and PKCS#5 padding scheme.

Base64 Encoding: The encrypted data is encoded in Base64 to ensure that it consists of printable characters and can be easily displayed or transmitted.

Exception Handling: The encryption and decryption methods include throws declarations for handling all necessary exceptions related to cryptographic operations.

This example provides a comprehensive introduction to implementing symmetric encryption in Java and can be a useful teaching aid in explaining concepts such as key management, initialization vectors, and the mechanics of block cipher modes like CBC.
 */

public class SymmetricEncryptionExample {

    public static void main(String[] args) {
        try {
            // STEP 1: Generate AES secret key
            // AES (Advanced Encryption Standard) is a symmetric encryption algorithm
            // "Symmetric" means the SAME key is used for both encryption and decryption
            // 256-bit key size provides strong security
            SecretKey secretKey = generateKey(256);

            // Wrap the key in a SecretKeySpec object for use with Cipher
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

            // STEP 2: Generate Initialization Vector (IV)
            // IV is a random value that ensures the same plaintext encrypts to different ciphertext each time
            // This prevents attackers from detecting patterns in encrypted data
            byte[] iv = generateIv();

            // STEP 3: Encrypt and Decrypt example text
            String plainText = "Hello, world! This is a test message.";

            // Encrypt the plaintext using the secret key and IV
            String cipherText = encrypt(plainText, secretKeySpec, iv);

            // Decrypt the ciphertext back to plaintext using the SAME key and IV
            String decryptedText = decrypt(cipherText, secretKeySpec, iv);

            // STEP 4: Display results
            System.out.println("Original: " + plainText);
            System.out.println("Encrypted: " + cipherText);
            System.out.println("Decrypted: " + decryptedText);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException ex) {
            // Handle various cryptographic exceptions
            ex.printStackTrace();
        }
    }

    /**
     * Generates a secret key for AES encryption
     *
     * @param n The key size in bits (128, 192, or 256)
     * @return A randomly generated secret key
     */
    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        // Create a key generator for the AES algorithm
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");

        // Initialize with the specified key size (in bits)
        keyGenerator.init(n);

        // Generate and return a random secret key
        return keyGenerator.generateKey();
    }

    /**
     * Generates a random Initialization Vector (IV)
     * IV must be 16 bytes (128 bits) for AES
     *
     * @return A random 16-byte IV
     */
    public static byte[] generateIv() {
        // AES block size is always 16 bytes (128 bits)
        byte[] newIv = new byte[16];

        // Fill the array with cryptographically secure random bytes
        // IMPORTANT: Always use SecureRandom for cryptographic operations, not Random!
        new java.security.SecureRandom().nextBytes(newIv);

        return newIv;
    }

    /**
     * Encrypts plaintext using AES encryption
     *
     * @param input The plaintext to encrypt
     * @param key The secret key for encryption
     * @param iv The initialization vector
     * @return Base64-encoded encrypted text
     */
    public static String encrypt(String input, SecretKeySpec key, byte[] iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        // Create a Cipher instance with:
        // - AES: The encryption algorithm
        // - CBC: Cipher Block Chaining mode (each block depends on the previous one)
        // - PKCS5Padding: Padding scheme to handle data that isn't a multiple of block size
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Initialize cipher in ENCRYPT mode with the secret key and IV
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        // Perform the actual encryption
        byte[] cipherText = cipher.doFinal(input.getBytes());

        // Encode the encrypted bytes to Base64 for safe transmission/storage
        // Base64 converts binary data to readable ASCII characters
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Decrypts ciphertext using AES decryption
     *
     * @param cipherText The Base64-encoded encrypted text
     * @param key The secret key for decryption (must be the same key used for encryption)
     * @param iv The initialization vector (must be the same IV used for encryption)
     * @return The decrypted plaintext
     */
    public static String decrypt(String cipherText, SecretKeySpec key, byte[] iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        // Create a Cipher instance with the SAME parameters used for encryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Initialize cipher in DECRYPT mode with the secret key and IV
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        // Decode the Base64 string back to bytes and decrypt
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));

        // Convert the decrypted bytes back to a string
        return new String(plainText);
    }
}
