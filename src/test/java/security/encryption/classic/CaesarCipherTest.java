package security.encryption.classic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CaesarCipherTest {

    @Test
    void encryptDecryptRoundtrip() {
        String original = "Hello World!";
        String encrypted = CaesarCipher.encrypt(original, 3);
        String decrypted = CaesarCipher.decrypt(encrypted, 3);
        assertEquals(original, decrypted);
    }

    @Test
    void encryptWithShiftZeroIsIdentity() {
        String text = "Hello World";
        assertEquals(text, CaesarCipher.encrypt(text, 0));
    }

    @Test
    void encryptShiftsLettersCorrectly() {
        assertEquals("DEF", CaesarCipher.encrypt("ABC", 3));
        assertEquals("abc", CaesarCipher.encrypt("xyz", 3));
    }

    @Test
    void encryptWrapsAroundAlphabet() {
        assertEquals("A", CaesarCipher.encrypt("Z", 1));
        assertEquals("a", CaesarCipher.encrypt("z", 1));
    }

    @Test
    void encryptPreservesNonLetters() {
        String text = "Hello, World! 123";
        String encrypted = CaesarCipher.encrypt(text, 5);
        // Non-letter characters unchanged
        assertEquals(',', encrypted.charAt(5));
        assertEquals(' ', encrypted.charAt(6));
        assertEquals('!', encrypted.charAt(12));
        assertEquals(' ', encrypted.charAt(13));
        assertEquals('1', encrypted.charAt(14));
    }

    @Test
    void encryptWithNegativeShift() {
        String text = "Hello";
        String encrypted = CaesarCipher.encrypt(text, 3);
        String decrypted = CaesarCipher.encrypt(encrypted, -3);
        assertEquals(text, decrypted);
    }

    @Test
    void encryptWithLargeShiftWraps() {
        // shift 29 is the same as shift 3
        assertEquals(CaesarCipher.encrypt("Hello", 3), CaesarCipher.encrypt("Hello", 29));
    }

    @Test
    void getMostFrequentLetterReturnsCorrectLetter() {
        // "AAABBC" -> most frequent is A
        char result = CaesarCipher.getMostFrequentLetter("AAABBC");
        assertEquals('A', result);
    }

    @Test
    void getMostFrequentLetterIsCaseInsensitive() {
        // mix of upper and lower, 'e' appears most
        char result = CaesarCipher.getMostFrequentLetter("eEeaaB");
        assertEquals('E', result);
    }

    @Test
    void encryptedTextDiffersFromOriginal() {
        String text = "Hello";
        String encrypted = CaesarCipher.encrypt(text, 3);
        assertNotEquals(text, encrypted);
    }
}
