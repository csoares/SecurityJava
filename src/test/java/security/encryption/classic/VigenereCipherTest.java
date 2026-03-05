package security.encryption.classic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VigenereCipherTest {

    @Test
    void encryptDecryptRoundtrip() {
        String original = "Hello World! 123";
        String encrypted = VigenereCipher.encrypt(original, "KEY");
        String decrypted = VigenereCipher.decrypt(encrypted, "KEY");
        assertEquals(original, decrypted);
    }

    @Test
    void encryptPreservesNonLetters() {
        String text = "Hello, World!";
        String encrypted = VigenereCipher.encrypt(text, "KEY");
        assertEquals(',', encrypted.charAt(5));
        assertEquals(' ', encrypted.charAt(6));
        assertEquals('!', encrypted.charAt(12));
    }

    @Test
    void encryptWithSingleCharKeyword_matchesCaesarShift() {
        // keyword "D" = shift 3 (D is the 4th letter, index 3)
        String text = "HELLO";
        String vigenere = VigenereCipher.encrypt(text, "D");
        String caesar = CaesarCipher.encrypt(text, 3);
        assertEquals(caesar, vigenere);
    }

    @Test
    void wrongKeywordProducesGibberish() {
        String original = "Hello World";
        String encrypted = VigenereCipher.encrypt(original, "KEY");
        String wrongDecrypt = VigenereCipher.decrypt(encrypted, "BAD");
        assertNotEquals(original, wrongDecrypt);
    }

    @Test
    void encryptIsCaseSensitiveForKeyword() {
        // keyword "key" and "KEY" should produce the same result
        String text = "Hello";
        assertEquals(
            VigenereCipher.encrypt(text, "KEY"),
            VigenereCipher.encrypt(text, "key")
        );
    }

    @Test
    void emptyKeywordThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> VigenereCipher.encrypt("Hello", ""));
    }

    @Test
    void calculateIndexOfCoincidenceForEnglishText() {
        // IC for English text should be close to 0.067
        String english = "The quick brown fox jumps over the lazy dog and the cat";
        double ic = VigenereCipher.calculateIndexOfCoincidence(english);
        assertTrue(ic > 0.02 && ic < 0.15, "IC for English text should be in a reasonable range: " + ic);
    }

    @Test
    void calculateIndexOfCoincidenceForSingleLetter() {
        // single letter repeated -> IC = 1.0
        double ic = VigenereCipher.calculateIndexOfCoincidence("AAAAAAA");
        assertEquals(1.0, ic, 0.001);
    }

    @Test
    void encryptedTextDiffersFromOriginal() {
        String text = "Hello";
        String encrypted = VigenereCipher.encrypt(text, "KEY");
        assertNotEquals(text, encrypted);
    }
}
