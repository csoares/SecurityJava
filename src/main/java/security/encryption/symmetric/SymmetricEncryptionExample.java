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
            // Generate AES key
            SecretKey secretKey = generateKey(256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

            // Encrypt and Decrypt example text
            String plainText = "Hello, world! This is a test message.";
            byte[] iv = generateIv(); // Generate a random IV
            String cipherText = encrypt(plainText, secretKeySpec, iv);
            String decryptedText = decrypt(cipherText, secretKeySpec, iv);

            // Output results
            System.out.println("Original: " + plainText);
            System.out.println("Encrypted: " + cipherText);
            System.out.println("Decrypted: " + decryptedText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException ex) {
            ex.printStackTrace();
        }
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    public static byte[] generateIv() {
        byte[] newIv = new byte[16];
        new java.security.SecureRandom().nextBytes(newIv);
        return newIv;
    }

    public static String encrypt(String input, SecretKeySpec key, byte[] iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String decrypt(String cipherText, SecretKeySpec key, byte[] iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}
