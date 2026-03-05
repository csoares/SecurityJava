package security.encryption.integrity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrityCheckHashTest {

    @Test
    void computeSHA256Returns32Bytes() throws Exception {
        byte[] hash = IntegrityCheckHash.computeSHA256("hello");
        assertEquals(32, hash.length);
    }

    @Test
    void computeSHA256IsDeterministic() throws Exception {
        byte[] hash1 = IntegrityCheckHash.computeSHA256("hello");
        byte[] hash2 = IntegrityCheckHash.computeSHA256("hello");
        assertArrayEquals(hash1, hash2);
    }

    @Test
    void differentInputsProduceDifferentHashes() throws Exception {
        byte[] hash1 = IntegrityCheckHash.computeSHA256("hello");
        byte[] hash2 = IntegrityCheckHash.computeSHA256("Hello");
        assertFalse(java.util.Arrays.equals(hash1, hash2));
    }

    @Test
    void smallChangeProducesCompletelyDifferentHash() throws Exception {
        byte[] hash1 = IntegrityCheckHash.computeSHA256("The quick brown fox");
        byte[] hash2 = IntegrityCheckHash.computeSHA256("The quick brown fox.");
        assertFalse(java.util.Arrays.equals(hash1, hash2));
    }

    @Test
    void bytesToHexReturns64CharsForSHA256() throws Exception {
        byte[] hash = IntegrityCheckHash.computeSHA256("test");
        String hex = IntegrityCheckHash.bytesToHex(hash);
        assertEquals(64, hex.length()); // 32 bytes * 2 hex chars each
    }

    @Test
    void bytesToHexProducesValidHexString() throws Exception {
        byte[] hash = IntegrityCheckHash.computeSHA256("test");
        String hex = IntegrityCheckHash.bytesToHex(hash);
        assertTrue(hex.matches("[0-9a-f]+"), "Hex string should contain only hex characters");
    }

    @Test
    void bytesToHexKnownValue() {
        // Known SHA-256 of "abc" is ba7816bf8f01cfea414140de5dae2ec73b00361bbef0469fa72a883e37dd8...
        byte[] bytes = {(byte) 0xba, (byte) 0x78};
        assertEquals("ba78", IntegrityCheckHash.bytesToHex(bytes));
    }

    @Test
    void emptyStringHashIsConsistent() throws Exception {
        // SHA-256 of empty string is well-known
        byte[] hash = IntegrityCheckHash.computeSHA256("");
        String hex = IntegrityCheckHash.bytesToHex(hash);
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hex);
    }
}
