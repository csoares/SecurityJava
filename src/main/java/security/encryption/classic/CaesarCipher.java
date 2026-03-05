package security.encryption.classic;

/*
CAESAR CIPHER - One of the Oldest Known Encryption Methods

Historical Context:
- Named after Julius Caesar, who used it to protect military messages around 50 BCE
- One of the simplest substitution ciphers
- Each letter is shifted by a fixed number of positions in the alphabet

How It Works:
- Choose a shift value (key), e.g., 3
- Replace each letter with the letter 'shift' positions ahead in the alphabet
- Example with shift 3: A→D, B→E, C→F, ..., X→A, Y→B, Z→C

Security:
- EXTREMELY WEAK by modern standards - easily broken by brute force (only 25 possible keys!)
- Vulnerable to frequency analysis (common letters remain identifiable)
- Educational value: demonstrates basic substitution cipher concepts

Real-World Use:
- ROT13 (rotation by 13) is still used for obfuscating spoilers, puzzle solutions, etc.
- NOT secure for any real encryption needs

    DIAGRAM: see README.md in this package for visual diagrams of this class.
 */

public class CaesarCipher {

    // The English alphabet size (26 letters)
    private static final int ALPHABET_SIZE = 26;

    public static void main(String[] args) {
        // STEP 1: Define the message and encryption key
        String plainText = "Hello World! This is a secret message.";
        int shift = 3;  // Caesar originally used a shift of 3

        System.out.println("=== CAESAR CIPHER DEMONSTRATION ===");
        System.out.println("Original Message: " + plainText);
        System.out.println("Shift Value (Key): " + shift);
        System.out.println();

        // STEP 2: Encrypt the message
        String encryptedText = encrypt(plainText, shift);
        System.out.println("Encrypted Message: " + encryptedText);

        // STEP 3: Decrypt the message
        String decryptedText = decrypt(encryptedText, shift);
        System.out.println("Decrypted Message: " + decryptedText);
        System.out.println();

        // STEP 4: Demonstrate brute force attack
        System.out.println("=== BRUTE FORCE ATTACK (trying all 25 possible shifts) ===");
        bruteForceAttack(encryptedText);
    }

    /**
     * Encrypts plaintext using Caesar cipher
     *
     * Algorithm:
     * 1. For each character in the plaintext:
     *    - If it's a letter, shift it by 'shift' positions
     *    - If it's uppercase, keep it uppercase
     *    - If it's lowercase, keep it lowercase
     *    - If it's not a letter (space, punctuation), leave it unchanged
     * 2. Wrap around the alphabet if needed (Z + 1 = A)
     *
     * @param plainText The text to encrypt
     * @param shift The number of positions to shift (the key)
     * @return The encrypted text
     */
    public static String encrypt(String plainText, int shift) {
        StringBuilder result = new StringBuilder();

        // Normalize shift to be within 0-25 range
        // This handles negative shifts and shifts greater than 26
        shift = shift % ALPHABET_SIZE;
        if (shift < 0) {
            shift += ALPHABET_SIZE;
        }

        // Process each character
        for (int i = 0; i < plainText.length(); i++) {
            char currentChar = plainText.charAt(i);

            // Check if character is an uppercase letter (A-Z)
            if (Character.isUpperCase(currentChar)) {
                // Convert to 0-25 range, apply shift, wrap around, convert back
                char encryptedChar = (char) ((currentChar - 'A' + shift) % ALPHABET_SIZE + 'A');
                result.append(encryptedChar);
            }
            // Check if character is a lowercase letter (a-z)
            else if (Character.isLowerCase(currentChar)) {
                // Same process for lowercase
                char encryptedChar = (char) ((currentChar - 'a' + shift) % ALPHABET_SIZE + 'a');
                result.append(encryptedChar);
            }
            // Not a letter - keep it unchanged (spaces, punctuation, numbers, etc.)
            else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    /**
     * Decrypts Caesar cipher text
     *
     * Decryption is simply encryption with the negative shift
     * If encrypted with shift +3, decrypt with shift -3
     *
     * @param cipherText The encrypted text
     * @param shift The shift value used for encryption
     * @return The decrypted text
     */
    public static String decrypt(String cipherText, int shift) {
        // Decryption is just encryption with negative shift
        // If we shifted right by 3 to encrypt, we shift left by 3 to decrypt
        return encrypt(cipherText, -shift);
    }

    /**
     * Demonstrates how easy it is to break Caesar cipher
     *
     * Brute Force Attack:
     * - Since there are only 25 possible shift values (1-25)
     * - We can try ALL of them and see which produces readable text
     * - This attack takes seconds, even by hand!
     * - This is why Caesar cipher is NOT secure for real use
     *
     * @param cipherText The encrypted text to crack
     */
    public static void bruteForceAttack(String cipherText) {
        System.out.println("Trying all possible shifts:");
        System.out.println("(Look for the readable text!)");
        System.out.println();

        // Try every possible shift from 1 to 25 (shift 0 is the original text)
        for (int shift = 1; shift < ALPHABET_SIZE; shift++) {
            String attempt = decrypt(cipherText, shift);
            System.out.println("Shift " + String.format("%2d", shift) + ": " + attempt);
        }

        System.out.println();
        System.out.println("Notice how easy it is to spot the correct decryption!");
        System.out.println("This demonstrates why Caesar cipher is insecure.");
    }

    /**
     * Additional utility: Frequency analysis helper
     *
     * In real Caesar cipher attacks, frequency analysis can help:
     * - Most common letter in English is 'E'
     * - If most common letter in ciphertext is 'H', shift is probably 3
     *
     * @param text Text to analyze
     * @return The most frequent letter in the text
     */
    public static char getMostFrequentLetter(String text) {
        int[] frequency = new int[ALPHABET_SIZE];

        // Count letter frequencies (case-insensitive)
        for (char c : text.toUpperCase().toCharArray()) {
            if (Character.isLetter(c)) {
                frequency[c - 'A']++;
            }
        }

        // Find the most frequent letter
        int maxFreq = 0;
        char mostFrequent = 'A';
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (frequency[i] > maxFreq) {
                maxFreq = frequency[i];
                mostFrequent = (char) ('A' + i);
            }
        }

        return mostFrequent;
    }
}
