package security.keyexchange;

import org.junit.jupiter.api.Test;
import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import static org.junit.jupiter.api.Assertions.*;

public class ECDHExampleTest {

    private KeyPair generateECKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        return kpg.generateKeyPair();
    }

    @Test
    void ecdhKeyAgreementProducesMatchingSecrets() throws Exception {
        KeyPair aliceKp = generateECKeyPair();
        KeyPair bobKp = generateECKeyPair();

        KeyAgreement aliceKa = KeyAgreement.getInstance("ECDH");
        aliceKa.init(aliceKp.getPrivate());
        aliceKa.doPhase(bobKp.getPublic(), true);
        byte[] aliceSecret = aliceKa.generateSecret();

        KeyAgreement bobKa = KeyAgreement.getInstance("ECDH");
        bobKa.init(bobKp.getPrivate());
        bobKa.doPhase(aliceKp.getPublic(), true);
        byte[] bobSecret = bobKa.generateSecret();

        assertArrayEquals(aliceSecret, bobSecret, "Alice and Bob should derive the same shared secret");
    }

    @Test
    void differentKeyPairsProduceDifferentSecrets() throws Exception {
        KeyPair aliceKp = generateECKeyPair();
        KeyPair bobKp = generateECKeyPair();
        KeyPair charlieKp = generateECKeyPair();

        KeyAgreement aliceBobKa = KeyAgreement.getInstance("ECDH");
        aliceBobKa.init(aliceKp.getPrivate());
        aliceBobKa.doPhase(bobKp.getPublic(), true);
        byte[] aliceBobSecret = aliceBobKa.generateSecret();

        KeyAgreement aliceCharlieKa = KeyAgreement.getInstance("ECDH");
        aliceCharlieKa.init(aliceKp.getPrivate());
        aliceCharlieKa.doPhase(charlieKp.getPublic(), true);
        byte[] aliceCharlieSecret = aliceCharlieKa.generateSecret();

        assertFalse(java.util.Arrays.equals(aliceBobSecret, aliceCharlieSecret),
            "Different parties should produce different shared secrets");
    }

    @Test
    void ecKeyPairHasCorrectAlgorithm() throws Exception {
        KeyPair kp = generateECKeyPair();
        assertEquals("EC", kp.getPublic().getAlgorithm());
        assertEquals("EC", kp.getPrivate().getAlgorithm());
    }

    @Test
    void ecPublicKeyIsSmallerThanRSA() throws Exception {
        KeyPair ecKp = generateECKeyPair();

        KeyPairGenerator rsaKpg = KeyPairGenerator.getInstance("RSA");
        rsaKpg.initialize(2048);
        KeyPair rsaKp = rsaKpg.generateKeyPair();

        assertTrue(ecKp.getPublic().getEncoded().length < rsaKp.getPublic().getEncoded().length,
            "EC public key should be smaller than RSA-2048 public key");
    }
}
