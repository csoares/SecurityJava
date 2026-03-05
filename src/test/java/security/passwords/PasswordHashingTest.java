package security.passwords;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHashingTest {

    @Test
    void sha256Returns32Bytes() throws Exception {
        byte[] hash = PasswordHashingExample.sha256("password");
        assertEquals(32, hash.length);
    }

    @Test
    void sha256IsDeterministic() throws Exception {
        byte[] h1 = PasswordHashingExample.sha256("password");
        byte[] h2 = PasswordHashingExample.sha256("password");
        assertArrayEquals(h1, h2);
    }

    @Test
    void sha256DifferentInputsDifferentHashes() throws Exception {
        byte[] h1 = PasswordHashingExample.sha256("password");
        byte[] h2 = PasswordHashingExample.sha256("Password");
        assertFalse(java.util.Arrays.equals(h1, h2));
    }

    @Test
    void generateSaltReturns16Bytes() {
        byte[] salt = PasswordHashingExample.generateSalt();
        assertEquals(16, salt.length);
    }

    @Test
    void generateSaltIsRandom() {
        byte[] s1 = PasswordHashingExample.generateSalt();
        byte[] s2 = PasswordHashingExample.generateSalt();
        assertFalse(java.util.Arrays.equals(s1, s2), "Two salts should (almost certainly) differ");
    }

    @Test
    void sha256WithSaltDiffersFromUnsalted() throws Exception {
        byte[] salt = PasswordHashingExample.generateSalt();
        byte[] unsalted = PasswordHashingExample.sha256("password");
        byte[] salted = PasswordHashingExample.sha256WithSalt("password", salt);
        assertFalse(java.util.Arrays.equals(unsalted, salted));
    }

    @Test
    void sha256WithSaltDependsOnSalt() throws Exception {
        byte[] salt1 = PasswordHashingExample.generateSalt();
        byte[] salt2 = PasswordHashingExample.generateSalt();
        byte[] h1 = PasswordHashingExample.sha256WithSalt("password", salt1);
        byte[] h2 = PasswordHashingExample.sha256WithSalt("password", salt2);
        assertFalse(java.util.Arrays.equals(h1, h2));
    }

    @Test
    void pbkdf2WithLowIterations() throws Exception {
        // Use low iteration count to keep test fast
        int iterations = 1000;
        byte[] salt = PasswordHashingExample.generateSalt();
        byte[] hash = PasswordHashingExample.pbkdf2("password", salt, iterations);
        assertNotNull(hash);
        assertTrue(hash.length > 0);
    }

    @Test
    void pbkdf2IsDeterministic() throws Exception {
        int iterations = 1000;
        byte[] salt = PasswordHashingExample.generateSalt();
        byte[] h1 = PasswordHashingExample.pbkdf2("password", salt, iterations);
        byte[] h2 = PasswordHashingExample.pbkdf2("password", salt, iterations);
        assertArrayEquals(h1, h2);
    }

    @Test
    void pbkdf2VerifyAcceptsCorrectPassword() throws Exception {
        int iterations = 1000;
        byte[] salt = PasswordHashingExample.generateSalt();
        byte[] hash = PasswordHashingExample.pbkdf2("MyPassword", salt, iterations);
        assertTrue(PasswordHashingExample.pbkdf2Verify("MyPassword", salt, iterations, hash));
    }

    @Test
    void pbkdf2VerifyRejectsWrongPassword() throws Exception {
        int iterations = 1000;
        byte[] salt = PasswordHashingExample.generateSalt();
        byte[] hash = PasswordHashingExample.pbkdf2("MyPassword", salt, iterations);
        assertFalse(PasswordHashingExample.pbkdf2Verify("WrongPassword", salt, iterations, hash));
    }

    @Test
    void bytesToHexProducesCorrectLength() throws Exception {
        byte[] hash = PasswordHashingExample.sha256("test");
        String hex = PasswordHashingExample.bytesToHex(hash);
        assertEquals(64, hex.length());
    }
}
