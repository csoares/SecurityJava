package security.pki;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * X.509 Certificate Chain (PKI — Public Key Infrastructure)
 *
 * PKI establishes trust through a chain of certificates:
 *
 *   Root CA  →  Intermediate CA  →  End-entity (server/user) certificate
 *
 * Each certificate contains:
 *   - Subject:    who this certificate identifies
 *   - Issuer:     who vouches for the subject
 *   - Public key: the subject's public key
 *   - Signature:  the issuer's digital signature over the above
 *
 * Chain of trust validation:
 *   1. Server sends its cert and any intermediate certs
 *   2. Client verifies each cert's signature using the issuer's public key
 *   3. Chain terminates at a Root CA pre-installed in the OS/browser trust store
 *   4. If valid → connection proceeds; otherwise → "Not Secure" warning
 *
 * This example uses BouncyCastle (bcpkix-jdk15on) to generate self-signed
 * X.509 certificates and build a three-level chain.
 */
public class CertificateChainExample {

    static {
        // Register BouncyCastle as a JCE security provider
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== X.509 Certificate Chain (PKI) ===");
        System.out.println();
        System.out.println("Trust model: Root CA signs Intermediate CA, Intermediate CA signs server cert.");
        System.out.println("Your browser trusts ~50 Root CAs pre-installed by the OS/browser vendor.");
        System.out.println("Those Root CAs sign Intermediate CAs, which sign the site certificates.");
        System.out.println();

        // ─── Step 1: Generate key pairs ──────────────────────────────────────────
        System.out.println("--- Step 1: Generate RSA-2048 key pairs for each CA level ---");
        KeyPair rootKp         = generateKeyPair();
        KeyPair intermediateKp = generateKeyPair();
        KeyPair serverKp       = generateKeyPair();
        System.out.println("Root CA key pair:         generated.");
        System.out.println("Intermediate CA key pair: generated.");
        System.out.println("Server key pair:          generated.");
        System.out.println();

        // ─── Step 2: Root CA certificate (self-signed) ───────────────────────────
        System.out.println("--- Step 2: Root CA Certificate (self-signed) ---");
        X509Certificate rootCert = createCertificate(
                "CN=Example Root CA, O=Example Corp, C=US",   // subject DN
                "CN=Example Root CA, O=Example Corp, C=US",   // issuer DN = self
                rootKp.getPublic(),                            // subject's public key
                rootKp.getPrivate(),                           // signed by own private key
                BigInteger.ONE                                 // serial number
        );
        printCert("Root CA", rootCert);
        System.out.println("  Self-signed: subject == issuer.");
        System.out.println("  Pre-installed in browser/OS trust stores by Root CA vendors.");
        System.out.println();

        // ─── Step 3: Intermediate CA certificate ─────────────────────────────────
        System.out.println("--- Step 3: Intermediate CA Certificate ---");
        X509Certificate intermediateCert = createCertificate(
                "CN=Example Intermediate CA, O=Example Corp, C=US",
                "CN=Example Root CA, O=Example Corp, C=US",   // issuer = Root CA
                intermediateKp.getPublic(),
                rootKp.getPrivate(),                           // signed by Root CA's private key
                BigInteger.TWO
        );
        printCert("Intermediate CA", intermediateCert);
        System.out.println("  Signed by Root CA — trust flows from Root through this cert.");
        System.out.println("  Intermediate CAs are kept online; Root CA is kept offline for safety.");
        System.out.println();

        // ─── Step 4: Server (end-entity) certificate ─────────────────────────────
        System.out.println("--- Step 4: Server Certificate (end-entity) ---");
        X509Certificate serverCert = createCertificate(
                "CN=www.example.com, O=Example Corp, C=US",
                "CN=Example Intermediate CA, O=Example Corp, C=US", // issuer = Intermediate CA
                serverKp.getPublic(),
                intermediateKp.getPrivate(),                        // signed by Intermediate CA
                BigInteger.valueOf(3)
        );
        printCert("Server (www.example.com)", serverCert);
        System.out.println("  Signed by Intermediate CA. Presented to browsers during TLS handshake.");
        System.out.println();

        // ─── Step 5: Validate the chain ───────────────────────────────────────────
        System.out.println("--- Step 5: Validate Certificate Chain ---");

        System.out.print("Server cert signed by Intermediate CA public key? ");
        System.out.println(verifyCertSignature(serverCert, intermediateCert.getPublicKey()));

        System.out.print("Intermediate CA signed by Root CA public key?     ");
        System.out.println(verifyCertSignature(intermediateCert, rootCert.getPublicKey()));

        System.out.print("Root CA is self-signed (signed by own public key)? ");
        System.out.println(verifyCertSignature(rootCert, rootCert.getPublicKey()));

        System.out.println();
        System.out.println("Full chain validated: Root CA → Intermediate CA → Server.");
        System.out.println();

        // ─── Step 6: Tamper detection ─────────────────────────────────────────────
        System.out.println("--- Step 6: Tamper Detection ---");
        System.out.print("Server cert verified with ROOT CA key directly (skipping intermediate)? ");
        System.out.println(verifyCertSignature(serverCert, rootCert.getPublicKey()));
        System.out.println("(false — server cert was not signed by Root CA, only by Intermediate CA)");
        System.out.println();

        // ─── HTTPS mental model ───────────────────────────────────────────────────
        System.out.println("--- Real-World HTTPS Handshake (simplified) ---");
        System.out.println("1. Browser → Server: \"Hello, I want to connect securely.\"");
        System.out.println("2. Server → Browser: server cert + intermediate cert(s)");
        System.out.println("3. Browser: chains up to a trusted Root CA in its trust store");
        System.out.println("4. Browser: verifies each signature in the chain");
        System.out.println("5. Browser: checks cert expiry, hostname match (CN/SAN)");
        System.out.println("6. If all OK: ECDHE/DHE key exchange for session key, then AES-GCM");
        System.out.println("7. If any check fails: 'Your connection is not private' warning");
        System.out.println();
        System.out.println("Certificate Authorities (CAs) like DigiCert, Let's Encrypt, and Sectigo");
        System.out.println("issue certificates after verifying domain ownership (DV) or organization (OV/EV).");
    }

    /**
     * Generates a 2048-bit RSA key pair using the BouncyCastle provider.
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "BC");
        gen.initialize(2048, new SecureRandom());
        return gen.generateKeyPair();
    }

    /**
     * Creates an X.509 v3 certificate.
     *
     * @param subjectDN  distinguished name of the certificate subject
     * @param issuerDN   distinguished name of the signing CA (equals subjectDN for self-signed)
     * @param subjectKey the public key to embed in the certificate
     * @param issuerKey  the private key used to sign the certificate (the issuer's)
     * @param serial     unique serial number (must be unique per CA)
     */
    public static X509Certificate createCertificate(
            String subjectDN, String issuerDN,
            PublicKey subjectKey, PrivateKey issuerKey,
            BigInteger serial) throws Exception {

        long now = System.currentTimeMillis();
        Date notBefore = new Date(now - 1000);                         // 1 sec ago (avoid clock skew)
        Date notAfter  = new Date(now + 365L * 24 * 60 * 60 * 1000);  // valid for 1 year

        X500Name subject = new X500Name(subjectDN);
        X500Name issuer  = new X500Name(issuerDN);

        // Build the certificate structure
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                issuer, serial, notBefore, notAfter, subject, subjectKey
        );

        // Sign with the issuer's private key using SHA-256 with RSA
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(issuerKey);

        X509CertificateHolder holder = builder.build(signer);

        return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(holder);
    }

    /**
     * Verifies that a certificate's signature is valid under the given public key.
     * Returns true if the cert was signed by the entity holding signerPublicKey.
     */
    public static boolean verifyCertSignature(X509Certificate cert, PublicKey signerPublicKey) {
        try {
            cert.verify(signerPublicKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void printCert(String label, X509Certificate cert) {
        System.out.println(label + " certificate:");
        System.out.println("  Subject: " + cert.getSubjectX500Principal().getName());
        System.out.println("  Issuer:  " + cert.getIssuerX500Principal().getName());
        System.out.println("  Valid:   " + cert.getNotBefore() + " → " + cert.getNotAfter());
        System.out.println("  Serial:  " + cert.getSerialNumber());
    }
}
