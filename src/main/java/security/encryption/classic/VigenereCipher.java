package security.encryption.classic;

/*
VIGENÈRE CIPHER - The "Unbreakable" Cipher (or so they thought!)

Historical Context:
- Invented by Giovan Battista Bellaso in 1553, later attributed to Blaise de Vigenère
- Considered unbreakable for 300 years! (until Charles Babbage cracked it in 1854)
- Used by Confederate forces in the American Civil War
- Even used in the 20th century before computers made cryptanalysis easier

How It Works:
- Uses a keyword instead of a single shift value
- Each letter of the keyword determines a different shift
- The keyword repeats to match the length of the plaintext
- This creates a polyalphabetic substitution cipher (multiple Caesar ciphers)

Example:
  Plaintext:  HELLO WORLD
  Keyword:    KEYKE YKEYK  (KEY repeated)
  Encryption: Each letter uses different Caesar shift based on keyword letter
  - H with K (shift 10) → R
  - E with E (shift 4)  → I
  - L with Y (shift 24) → J
  And so on...

Security:
- Much stronger than Caesar cipher (can't use simple brute force)
- Still vulnerable to:
  * Frequency analysis (if keyword is short or repeats often)
  * Kasiski examination (finds keyword length)
  * Index of coincidence analysis
- NOT secure by modern standards, but educational and historically significant

Real-World Use:
- Basis for one-time pad (theoretically unbreakable if key is truly random and used once)
- Influenced modern stream ciphers
 */

public class VigenereCipher {

    // The English alphabet size (26 letters)
    private static final int ALPHABET_SIZE = 26;

    public static void main(String[] args) {
        // STEP 1: Define the message and encryption keyword
        String plainText = "Hello World! This is a secret message from cryptography class.";
        String keyword = "KEY";  // The secret keyword (can be any length)

        System.out.println("=== VIGENÈRE CIPHER DEMONSTRATION ===");
        System.out.println("Original Message: " + plainText);
        System.out.println("Keyword: " + keyword);
        System.out.println();

        // STEP 2: Encrypt the message
        String encryptedText = encrypt(plainText, keyword);
        System.out.println("Encrypted Message: " + encryptedText);
        System.out.println();

        // STEP 3: Show how the keyword repeats
        demonstrateKeywordRepeating(plainText, keyword);
        System.out.println();

        // STEP 4: Decrypt the message
        String decryptedText = decrypt(encryptedText, keyword);
        System.out.println("Decrypted Message: " + decryptedText);
        System.out.println();

        // STEP 5: Compare with wrong keyword
        System.out.println("=== ATTEMPTING DECRYPTION WITH WRONG KEYWORD ===");
        String wrongKeyword = "BAD";
        String wrongDecryption = decrypt(encryptedText, wrongKeyword);
        System.out.println("Wrong Keyword: " + wrongKeyword);
        System.out.println("Result: " + wrongDecryption);
        System.out.println("(Notice the gibberish - wrong keyword produces unreadable text)");
    }

    /**
     * Encrypts plaintext using Vigenère cipher
     *
     * Algorithm:
     * 1. Convert keyword to uppercase and get its length
     * 2. For each letter in plaintext:
     *    - Determine which keyword letter to use (cycling through keyword)
     *    - Use that keyword letter's position as the Caesar shift
     *    - Apply the shift to encrypt the plaintext letter
     * 3. Non-letter characters pass through unchanged
     *
     * @param plainText The text to encrypt
     * @param keyword The keyword used for encryption (the key)
     * @return The encrypted text
     */
    public static String encrypt(String plainText, String keyword) {
        StringBuilder result = new StringBuilder();

        // Convert keyword to uppercase and remove non-letters
        String normalizedKeyword = keyword.toUpperCase().replaceAll("[^A-Z]", "");

        // Validate keyword
        if (normalizedKeyword.isEmpty()) {
            throw new IllegalArgumentException("Keyword must contain at least one letter");
        }

        int keywordIndex = 0;  // Tracks position in keyword

        // Process each character in plaintext
        for (int i = 0; i < plainText.length(); i++) {
            char currentChar = plainText.charAt(i);

            // Only encrypt letters; skip spaces, punctuation, numbers
            if (Character.isLetter(currentChar)) {
                // Get the current keyword letter and calculate shift
                char keywordChar = normalizedKeyword.charAt(keywordIndex % normalizedKeyword.length());
                int shift = keywordChar - 'A';  // A=0, B=1, C=2, ..., Z=25

                // Encrypt uppercase letters
                if (Character.isUpperCase(currentChar)) {
                    char encryptedChar = (char) ((currentChar - 'A' + shift) % ALPHABET_SIZE + 'A');
                    result.append(encryptedChar);
                }
                // Encrypt lowercase letters
                else {
                    char encryptedChar = (char) ((currentChar - 'a' + shift) % ALPHABET_SIZE + 'a');
                    result.append(encryptedChar);
                }

                // Move to next position in keyword (only for letters)
                keywordIndex++;
            }
            // Non-letter characters pass through unchanged
            else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    /**
     * Decrypts Vigenère cipher text
     *
     * Decryption uses the same process as encryption, but with negative shifts
     * Each keyword letter indicates how much to shift BACKWARD
     *
     * @param cipherText The encrypted text
     * @param keyword The keyword used for encryption
     * @return The decrypted text
     */
    public static String decrypt(String cipherText, String keyword) {
        StringBuilder result = new StringBuilder();

        // Convert keyword to uppercase and remove non-letters
        String normalizedKeyword = keyword.toUpperCase().replaceAll("[^A-Z]", "");

        // Validate keyword
        if (normalizedKeyword.isEmpty()) {
            throw new IllegalArgumentException("Keyword must contain at least one letter");
        }

        int keywordIndex = 0;  // Tracks position in keyword

        // Process each character in ciphertext
        for (int i = 0; i < cipherText.length(); i++) {
            char currentChar = cipherText.charAt(i);

            // Only decrypt letters
            if (Character.isLetter(currentChar)) {
                // Get the current keyword letter and calculate shift
                char keywordChar = normalizedKeyword.charAt(keywordIndex % normalizedKeyword.length());
                int shift = keywordChar - 'A';

                // Decrypt uppercase letters (shift backward)
                if (Character.isUpperCase(currentChar)) {
                    char decryptedChar = (char) ((currentChar - 'A' - shift + ALPHABET_SIZE) % ALPHABET_SIZE + 'A');
                    result.append(decryptedChar);
                }
                // Decrypt lowercase letters (shift backward)
                else {
                    char decryptedChar = (char) ((currentChar - 'a' - shift + ALPHABET_SIZE) % ALPHABET_SIZE + 'a');
                    result.append(decryptedChar);
                }

                // Move to next position in keyword (only for letters)
                keywordIndex++;
            }
            // Non-letter characters pass through unchanged
            else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    /**
     * Demonstrates how the keyword repeats to match the plaintext length
     *
     * This visualization helps students understand the polyalphabetic nature
     * of the Vigenère cipher - different letters use different Caesar shifts
     *
     * @param plainText The original message
     * @param keyword The encryption keyword
     */
    public static void demonstrateKeywordRepeating(String plainText, String keyword) {
        System.out.println("=== HOW THE KEYWORD REPEATS ===");

        String normalizedKeyword = keyword.toUpperCase().replaceAll("[^A-Z]", "");
        StringBuilder repeatedKey = new StringBuilder();
        StringBuilder shiftsUsed = new StringBuilder();

        int keywordIndex = 0;

        // Build the repeated keyword to match plaintext
        for (int i = 0; i < plainText.length(); i++) {
            char currentChar = plainText.charAt(i);

            if (Character.isLetter(currentChar)) {
                char keyChar = normalizedKeyword.charAt(keywordIndex % normalizedKeyword.length());
                repeatedKey.append(keyChar);
                shiftsUsed.append(String.format("%2d ", (keyChar - 'A')));
                keywordIndex++;
            } else {
                repeatedKey.append(" ");
                shiftsUsed.append("   ");
            }
        }

        System.out.println("Plaintext:  " + plainText);
        System.out.println("Keyword:    " + repeatedKey.toString());
        System.out.println("Shifts:     " + shiftsUsed.toString());
        System.out.println();
        System.out.println("Each letter uses a different shift based on the keyword!");
    }

    /**
     * Calculates the Index of Coincidence (IC) for cryptanalysis
     *
     * Index of Coincidence helps determine if text is:
     * - Random (IC ≈ 0.038)
     * - English plaintext (IC ≈ 0.067)
     * - Vigenère encrypted (IC between 0.038 and 0.067, depending on key length)
     *
     * Lower IC suggests longer keyword or better encryption
     * This is one method cryptanalysts use to attack Vigenère cipher
     *
     * @param text The text to analyze
     * @return The Index of Coincidence value
     */
    public static double calculateIndexOfCoincidence(String text) {
        // Count frequency of each letter
        int[] frequency = new int[ALPHABET_SIZE];
        int totalLetters = 0;

        for (char c : text.toUpperCase().toCharArray()) {
            if (Character.isLetter(c)) {
                frequency[c - 'A']++;
                totalLetters++;
            }
        }

        // Calculate IC using the formula: Σ(fi * (fi-1)) / (N * (N-1))
        // where fi is frequency of letter i, N is total letters
        double ic = 0.0;
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            ic += frequency[i] * (frequency[i] - 1);
        }

        if (totalLetters > 1) {
            ic /= (totalLetters * (totalLetters - 1));
        }

        return ic;
    }

    /**
     * Estimates the keyword length using Kasiski examination
     *
     * Kasiski Examination:
     * - Finds repeated sequences in the ciphertext
     * - Measures distances between repetitions
     * - The keyword length likely divides these distances
     * - This helps cryptanalysts break the cipher
     *
     * This is a simplified demonstration - real Kasiski examination is more complex
     *
     * @param cipherText The encrypted text to analyze
     */
    public static void demonstrateKasiskiExamination(String cipherText) {
        System.out.println("=== KASISKI EXAMINATION (Simplified) ===");
        System.out.println("Looking for repeated 3-letter sequences...");
        System.out.println("(Repetitions suggest keyword pattern)");
        System.out.println();

        // This is a simplified example - real implementation would be more sophisticated
        String normalized = cipherText.toUpperCase().replaceAll("[^A-Z]", "");

        // Look for repeated trigrams (3-letter sequences)
        for (int i = 0; i < normalized.length() - 2; i++) {
            String trigram = normalized.substring(i, i + 3);
            int nextOccurrence = normalized.indexOf(trigram, i + 3);

            if (nextOccurrence != -1) {
                int distance = nextOccurrence - i;
                System.out.println("Found '" + trigram + "' repeated at distance: " + distance);
                System.out.println("  Possible keyword lengths: factors of " + distance);
            }
        }
    }
}
