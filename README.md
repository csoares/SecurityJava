# SecurityJava — Cryptography & Security Curriculum

A hands-on Java 21 codebase for learning cryptography and security from the ground up.
Every class is standalone and runnable. Follow the learning path below — each topic builds on the previous one.

---

## How to Run Any Example

```bash
mvn exec:java -Dexec.mainClass="security.<package>.<ClassName>"
```

**Example:**
```bash
mvn exec:java -Dexec.mainClass="security.mac.HMACExample"
```

---

## Learning Path

```mermaid
flowchart TD
    A["🏛️ Classical Ciphers\nCaesar · Vigenère"]
    B["🔒 Symmetric Encryption\nAES-256-CBC"]
    C["📦 Cipher Modes\nECB · CBC · GCM"]
    D["🔑 Asymmetric Encryption\nRSA-2048"]
    E["📐 Elliptic Curve Crypto\nECDSA · ECDH"]
    F["🤝 Key Exchange\nDiffie-Hellman · ECDH"]
    G["🔍 Integrity & Signatures\nHash · HMAC · Digital Signatures"]
    H["🔐 Password Security\nSHA-256 → Salt → PBKDF2"]
    I["🏛️ PKI & Certificates\nX.509 Certificate Chains"]
    J["⚠️ Attack Demonstrations\nTiming · Weak RNG · Rainbow Tables"]

    A --> B --> C --> D --> E --> F --> G --> H --> I --> J

    style A fill:#e8d5b7
    style B fill:#b7d5e8
    style C fill:#b7d5e8
    style D fill:#c8e8b7
    style E fill:#c8e8b7
    style F fill:#c8e8b7
    style G fill:#e8c8b7
    style H fill:#e8e8b7
    style I fill:#d5b7e8
    style J fill:#f5b7b7
```

---

## Package Map & Detailed READMEs

Each package has its own `README.md` with rendered Mermaid diagrams and concept explanations.

```mermaid
graph LR
    subgraph security
        subgraph encryption
            classic["classic\nCaesarCipher\nVigenereCipher"]
            symmetric["symmetric\nSymmetricEncryptionExample"]
            asymmetric["asymmetric\nAsymmetricEncryptionExample"]
            modes["modes\nCipherModesComparison"]
            integrity["integrity\nIntegrityCheckHash\nIntegrityCheckSignature\nFileDigitalSignature"]
        end
        mac["mac\nHMACExample"]
        keyexchange["keyexchange\nDiffieHellmanExample\nECDHExample"]
        ecc["ecc\nECCSignatureExample"]
        passwords["passwords\nPasswordHashingExample"]
        pki["pki\nCertificateChainExample"]
        attacks["attacks\nTimingAttackExample\nWeakRandomnessExample\nRainbowTableExample"]
    end
```

### Package README Index

| Package | README | What it covers |
|---|---|---|
| `security.encryption.classic` | [📖 classic/README.md](src/main/java/security/encryption/classic/README.md) | Caesar cipher shift & brute force · Vigenère keyword repeating & Kasiski attack |
| `security.encryption.symmetric` | [📖 symmetric/README.md](src/main/java/security/encryption/symmetric/README.md) | AES-256-CBC · IV facts · CBC block chaining diagram |
| `security.encryption.asymmetric` | [📖 asymmetric/README.md](src/main/java/security/encryption/asymmetric/README.md) | RSA-2048 key generation · encrypt/decrypt flow · hybrid encryption |
| `security.encryption.modes` | [📖 modes/README.md](src/main/java/security/encryption/modes/README.md) | ECB pattern leak · CBC chaining · GCM authenticated encryption |
| `security.encryption.integrity` | [📖 integrity/README.md](src/main/java/security/encryption/integrity/README.md) | SHA-256 avalanche effect · RSA signatures · file signing & tamper detection |
| `security.mac` | [📖 mac/README.md](src/main/java/security/mac/README.md) | HMAC-SHA256 construction · hash vs HMAC vs signature · constant-time comparison |
| `security.keyexchange` | [📖 keyexchange/README.md](src/main/java/security/keyexchange/README.md) | Diffie-Hellman protocol · discrete log problem · ECDH · forward secrecy |
| `security.passwords` | [📖 passwords/README.md](src/main/java/security/passwords/README.md) | SHA-256 → salted → PBKDF2 · cracking speeds · login verification flow |
| `security.ecc` | [📖 ecc/README.md](src/main/java/security/ecc/README.md) | ECDSA sign/verify · RSA vs ECC key sizes · real-world uses |
| `security.attacks` | [📖 attacks/README.md](src/main/java/security/attacks/README.md) | Timing attack · weak PRNG · rainbow table attack & salt defence |
| `security.pki` | [📖 pki/README.md](src/main/java/security/pki/README.md) | X.509 chain of trust · browser validation · TLS handshake |

---

## 1. Classical Ciphers — `security.encryption.classic`

> **The foundations.** Before computers, people encrypted messages by hand using substitution rules. These are broken, but they teach the core vocabulary: plaintext, ciphertext, key, and attacks.

```mermaid
flowchart LR
    P["Plaintext\n'HELLO'"]
    K["Key\nshift = 3"]
    C["Ciphertext\n'KHOOR'"]
    A["Brute Force\n(26 possible keys)"]
    F["Frequency Analysis\n(E=13%, T=9%...)"]

    P -->|"encrypt: +key"| C
    K --> C
    C -->|"decrypt: -key"| P
    C --> A
    C --> F

    style P fill:#e8f4e8
    style C fill:#f4e8e8
    style A fill:#fff3cd
    style F fill:#fff3cd
```

| File | Key concepts |
|---|---|
| `CaesarCipher.java` | Substitution cipher, brute-force attack (26 tries), frequency analysis |
| `VigenereCipher.java` | Polyalphabetic cipher, Kasiski examination, index of coincidence |

**Why it matters:** Shows that security-through-obscurity fails. Even a complex-looking cipher can be broken if it has a small key space or exploitable patterns.

---

## 2. Symmetric Encryption — `security.encryption.symmetric`

> **One key, two directions.** The same secret key encrypts and decrypts. Fast enough for bulk data, but the key must be shared securely in advance.

```mermaid
sequenceDiagram
    participant Alice
    participant Network
    participant Bob

    Note over Alice,Bob: Both share the same Secret Key 🔑

    Alice->>Alice: plaintext → AES-256(key, IV) → ciphertext
    Alice->>Network: ciphertext + IV
    Network->>Bob: ciphertext + IV
    Bob->>Bob: ciphertext → AES-256⁻¹(key, IV) → plaintext

    Note over Network: Eavesdropper sees ciphertext only — useless without key
```

| File | Key concepts |
|---|---|
| `SymmetricEncryptionExample.java` | AES-256-CBC, key generation, IV (Initialization Vector), Base64 encoding |

**Key insight:** The IV must be unique per encryption but does not need to be secret. The key must never be transmitted over an insecure channel — that's solved by key exchange (see §6).

---

## 3. Cipher Block Modes — `security.encryption.modes`

> **Same algorithm, very different security.** AES encrypts 16 bytes at a time. The *mode* determines how blocks are connected. The wrong mode leaks structure even when each block is encrypted.

```mermaid
flowchart TD
    subgraph ECB ["❌ ECB — Electronic Code Book (INSECURE)"]
        direction LR
        P1["Block 1\n'YELLOW SUB'"] --> E1["AES"] --> C1["Block 1 out"]
        P2["Block 2\n'YELLOW SUB'"] --> E2["AES"] --> C2["Block 2 out"]
        P3["Block 3\n'YELLOW SUB'"] --> E3["AES"] --> C3["Block 3 out"]
        Note1["⚠️ C1 == C2 == C3\nIdentical input = identical output\nPatterns are visible!"]
    end

    subgraph CBC ["✅ CBC — Cipher Block Chaining"]
        direction LR
        IV["Random IV"] --> XOR1["XOR"]
        P4["Block 1"] --> XOR1 --> AES1["AES"] --> CC1["C1"]
        CC1 --> XOR2["XOR"]
        P5["Block 2"] --> XOR2 --> AES2["AES"] --> CC2["C2"]
        CC2 --> XOR3["XOR"]
        P6["Block 3"] --> XOR3 --> AES3["AES"] --> CC3["C3"]
    end

    subgraph GCM ["🏆 GCM — Galois/Counter Mode (BEST)"]
        direction LR
        P7["Plaintext"] --> AES4["AES-CTR"] --> CT["Ciphertext"]
        CT --> GHASH["GHASH"] --> TAG["Auth Tag"]
        Note2["Encrypts AND authenticates\nTampering detected automatically"]
    end
```

| File | Key concepts |
|---|---|
| `CipherModesComparison.java` | ECB pattern leak, CBC IV chaining, GCM authenticated encryption, `AEADBadTagException` on tamper |

**Key insight:** Always prefer AES-GCM for new code. It gives you encryption + integrity in one step, with no extra HMAC required.

---

## 4. Asymmetric Encryption — `security.encryption.asymmetric`

> **Two keys, one pair.** A public key encrypts; only the matching private key can decrypt. Solves the key distribution problem — share your public key with the world.

```mermaid
sequenceDiagram
    participant Alice
    participant Bob

    Bob->>Alice: "Here is my public key 🔓"
    Note over Alice: Encrypts with Bob's PUBLIC key
    Alice->>Bob: ciphertext
    Note over Bob: Decrypts with own PRIVATE key 🔑
    Bob->>Bob: plaintext

    Note over Alice,Bob: Even Alice cannot decrypt what she encrypted!
    Note over Alice,Bob: The private key never leaves Bob's machine.
```

| File | Key concepts |
|---|---|
| `AsymmetricEncryptionExample.java` | RSA-2048, public/private key pair, key pair generation, OAEP padding |

**Key insight:** RSA is too slow for bulk data. In practice (TLS, Signal), asymmetric crypto is used only to establish a shared symmetric key, then AES takes over.

---

## 5. Integrity & Digital Signatures — `security.encryption.integrity`

> **Proving data hasn't changed — and who sent it.** Hashes verify integrity. Digital signatures add authentication: only the holder of the private key could have produced the signature.

```mermaid
flowchart LR
    subgraph Hash ["🔍 Hash — Integrity Only"]
        M1["Message"] -->|"SHA-256"| H1["Hash"]
        H1 -->|"Compare"| V1{"Match?"}
    end

    subgraph Sig ["✍️ Digital Signature — Integrity + Authenticity"]
        M2["Message"] -->|"SHA-256"| H2["Hash"]
        H2 -->|"RSA sign\n(private key)"| S["Signature"]
        S -->|"RSA verify\n(public key)"| V2{"Valid?"}
    end

    subgraph File ["📄 File Signing"]
        F["File"] --> FS["Sign file\n(private key)"]
        FS --> SIG["signature.sig"]
        F2["File (received)"] --> FV["Verify\n(public key)"]
        SIG --> FV
        FV --> R{"Authentic\n& Unmodified?"}
    end
```

| File | Key concepts |
|---|---|
| `IntegrityCheckHash.java` | SHA-256, avalanche effect, integrity without authentication |
| `IntegrityCheckSignature.java` | RSA signing, SHA256withRSA, public key verification |
| `FileDigitalSignature.java` | Signing files to disk, detached signatures, tamper detection |

**Key insight:** A hash proves a file hasn't changed *in transit*. A signature proves *who* signed it and that it hasn't changed. HMAC sits in between — authenticated, but requires a shared key.

---

## 6. Key Exchange — `security.keyexchange`

> **The magic trick.** Alice and Bob agree on a shared secret over a channel where Eve hears everything — without ever transmitting the secret. This solves the fundamental problem of symmetric encryption.

```mermaid
sequenceDiagram
    participant Alice
    participant Eve as Eve 👁️ (sees everything)
    participant Bob

    Note over Alice,Bob: Agree on public parameters: g=2, p=(large prime)
    Alice->>Eve: Alice's public key A = g^a mod p
    Eve->>Bob: Alice's public key A
    Bob->>Eve: Bob's public key B = g^b mod p
    Eve->>Alice: Bob's public key B

    Note over Alice: secret = B^a mod p = g^ab mod p
    Note over Bob: secret = A^b mod p = g^ab mod p
    Note over Eve: Eve knows g, p, A, B but NOT a or b<br/>Computing a from A = Discrete Log Problem<br/>Computationally infeasible at 2048 bits
    Note over Alice,Bob: ✅ Both have g^ab mod p — Eve has nothing
```

```mermaid
flowchart LR
    subgraph DH ["Classic Diffie-Hellman"]
        DH1["~256-byte keys\n2048-bit security\nSlower handshake"]
    end
    subgraph ECDH ["Elliptic Curve DH (ECDH)"]
        EC1["~32-byte keys\n256-bit ECC ≈ 2048-bit DH security\nFaster, smaller — used in TLS 1.3"]
    end
    DH --> |"Same idea, better math"| ECDH
```

| File | Key concepts |
|---|---|
| `DiffieHellmanExample.java` | Classic DH, public parameters, discrete log problem, MITM vulnerability |
| `ECDHExample.java` | Elliptic curve DH, P-256 curve, forward secrecy, key size comparison |

**Key insight:** ECDH is what TLS 1.3 uses for every HTTPS connection. A fresh key pair is generated per session (Ephemeral ECDH = ECDHE) so past sessions stay private even if a long-term key leaks.

---

## 7. HMAC — `security.mac`

> **A hash with a secret key.** A plain hash can be recomputed by anyone. HMAC requires the secret key — proving both integrity and that the sender holds the key.

```mermaid
flowchart TD
    subgraph Comparison ["Authentication Spectrum"]
        direction LR
        H["🔍 Hash\nSHA-256(message)\n\nNo key\nAnyone can forge\nIntegrity only"]
        HM["🔑 HMAC\nHMAC-SHA256(key, message)\n\nShared secret key\nForgery requires the key\nIntegrity + Authentication"]
        DS["✍️ Digital Signature\nSign(privateKey, message)\n\nAsymmetric key pair\nOnly private key can sign\n+ Non-repudiation"]

        H -->|"Add symmetric key"| HM
        HM -->|"Switch to asymmetric keys"| DS
    end
```

```mermaid
sequenceDiagram
    participant Alice
    participant Network
    participant Bob

    Note over Alice,Bob: Both share secret key K

    Alice->>Alice: tag = HMAC-SHA256(K, message)
    Alice->>Network: message + tag
    Network->>Bob: message + tag
    Note over Network: Attacker modifies message → tag no longer matches
    Bob->>Bob: Recompute HMAC-SHA256(K, message')
    Bob->>Bob: Compare with received tag → REJECT if different
```

| File | Key concepts |
|---|---|
| `HMACExample.java` | HMAC-SHA256, constant-time verification, hash vs HMAC vs signature |

**Key insight:** Use HMAC when you and the other party share a secret key (e.g., API authentication, JWT with HS256). Use digital signatures when the receiver must not be able to forge the proof (e.g., TLS certificates).

---

## 8. Password Security — `security.passwords`

> **Why you can't just hash passwords.** Passwords need to be stored so you can verify them at login — but never retrievable. The solution has evolved from plain hashes to deliberately slow, salted algorithms.

```mermaid
flowchart TD
    subgraph Stage1 ["❌ Stage 1: Plain SHA-256 (INSECURE)"]
        P1["password"] -->|"SHA-256"| H1["5e884898..."]
        H1 --> RT[("Rainbow Table\n10B entries")]
        RT -->|"instant lookup"| CRACK1["💥 CRACKED"]
    end

    subgraph Stage2 ["⚠️ Stage 2: SHA-256 + Salt (BETTER)"]
        SALT["Random Salt\nper user"] --> COMBINE["salt + password"]
        P2["password"] --> COMBINE
        COMBINE -->|"SHA-256"| H2["unique hash"]
        H2 --> BRUTE["Brute Force\n(billions/sec on GPU)"]
        BRUTE --> CRACK2["💥 Still fast to crack"]
    end

    subgraph Stage3 ["✅ Stage 3: PBKDF2 / bcrypt / Argon2 (SECURE)"]
        P3["password"] --> SLOW["310,000 rounds\nof HMAC-SHA256"]
        SALT2["Unique Salt"] --> SLOW
        SLOW --> H3["derived key"]
        H3 --> HARD["GPU: ~100ms per guess\n= impractical brute force"]
    end

    Stage1 --> Stage2 --> Stage3
```

| File | Key concepts |
|---|---|
| `PasswordHashingExample.java` | Plain SHA-256, salted SHA-256, PBKDF2 (310k iterations), simulated login |

**Key insight:** Passwords need *slow* hashing. SHA-256 runs at billions of iterations per second on a GPU; PBKDF2 with 310,000 rounds takes ~100ms per guess — a factor of ~10⁸ harder to brute force.

---

## 9. Elliptic Curve Cryptography — `security.ecc`

> **Smaller keys, same security.** ECC is an alternative mathematical foundation for public-key cryptography. The same operations (signing, key exchange) work with dramatically smaller keys.

```mermaid
flowchart LR
    subgraph RSA ["RSA-2048"]
        RSAP["Private key: ~1200 bytes\nPublic key: ~294 bytes\nSecurity: ~112 bits\nMath: modular exponentiation"]
    end

    subgraph ECC ["ECDSA / ECDH (P-256)"]
        ECCP["Private key: ~67 bytes\nPublic key: ~91 bytes\nSecurity: ~128 bits\nMath: elliptic curve groups"]
    end

    RSA -->|"8× smaller\nmore secure"| ECC

    subgraph Uses ["Real-world uses of ECC"]
        U1["Bitcoin / Ethereum\nTransaction signing (secp256k1)"]
        U2["TLS 1.3\nECDHE key exchange + ECDSA certs"]
        U3["JWT ES256\nAPI token signing"]
        U4["SSH\necdsa-sha2-nistp256 key type"]
    end
```

| File | Key concepts |
|---|---|
| `ECCSignatureExample.java` | ECDSA P-256, sign/verify, RSA vs ECC key size comparison |

**Key insight:** ECC is not fundamentally different from RSA in what it achieves — it achieves the *same* things with smaller keys because the underlying hard problem (elliptic curve discrete log) is harder per bit than RSA's integer factorisation.

---

## 10. PKI & Certificates — `security.pki`

> **The chain of trust behind HTTPS.** Public keys solve the *distribution* problem but not the *authenticity* problem: how do you know a public key actually belongs to `example.com`? Certificate Authorities provide the answer.

```mermaid
flowchart TD
    ROOT["🏛️ Root CA\nCN=DigiCert Root\nSelf-signed\nPre-installed in your browser/OS"]
    INT["🏢 Intermediate CA\nCN=DigiCert TLS\nSigned by Root CA"]
    SERVER["🌐 Server Certificate\nCN=www.example.com\nSigned by Intermediate CA\nContains server's public key"]

    ROOT -->|"signs"| INT
    INT -->|"signs"| SERVER

    BROWSER["🌐 Your Browser"]
    TRUST["🔒 Trusted Root Store\n~50 built-in Root CAs"]

    BROWSER -->|"1. receives cert chain"| SERVER
    BROWSER -->|"2. validates chain"| INT
    BROWSER -->|"3. chains up to"| ROOT
    BROWSER -->|"4. checks against"| TRUST
    TRUST -->|"found! trust established"| BROWSER

    style ROOT fill:#d4edda
    style INT fill:#cce5ff
    style SERVER fill:#fff3cd
    style TRUST fill:#d4edda
```

```mermaid
sequenceDiagram
    participant Browser
    participant Server
    participant RootCA as Root CA (in trust store)

    Browser->>Server: ClientHello (TLS handshake)
    Server->>Browser: server cert + intermediate cert
    Browser->>Browser: verify intermediate cert with Root CA's public key ✅
    Browser->>Browser: verify server cert with Intermediate CA's public key ✅
    Browser->>Browser: check expiry, hostname match ✅
    Note over Browser,Server: Trust established → ECDHE key exchange → AES-GCM session
```

| File | Key concepts |
|---|---|
| `CertificateChainExample.java` | X.509 certificates (BouncyCastle), Root CA, Intermediate CA, end-entity cert, chain validation |

**Key insight:** The Root CA never signs website certs directly. The offline Root CA signs Intermediate CAs; those sign sites. If an Intermediate CA is compromised, the Root CA can revoke it without replacing the Root.

---

## 11. Attack Demonstrations — `security.attacks`

> **Understanding attacks makes you a better defender.** These examples show how real vulnerabilities work so you can recognize and avoid them in your own code.

### Timing Attack

```mermaid
sequenceDiagram
    participant Attacker
    participant Server

    Note over Attacker: Guess: "a????..." (wrong at position 0)
    Attacker->>Server: token = "aXXXXXXX..."
    Server->>Attacker: INVALID (response in 1 ms)

    Note over Attacker: Guess: "a3f8c1..." (wrong at last char)
    Attacker->>Server: token = "a3f8c1d2e4b5067891abcdef0123456X"
    Server->>Attacker: INVALID (response in 5 ms)

    Note over Attacker: Longer = more chars matched!<br/>Binary search character by character<br/>String.equals() exits early on mismatch → leak
    Note over Server: Fix: MessageDigest.isEqual() → always takes the same time
```

### Weak Randomness

```mermaid
flowchart LR
    subgraph Weak ["❌ java.util.Random"]
        SEED["Seed = currentTimeMillis()"] --> LCG["Linear Congruential\nGenerator"] --> OUT1["'Random' numbers"]
        GUESS["Attacker guesses seed\n±5 second window\n= ~10,000 tries"] --> LCG2["Same LCG"] --> SAME["Identical output\n💥 Token forged"]
    end

    subgraph Strong ["✅ java.security.SecureRandom"]
        ENTROPY["OS entropy pool\n(hardware noise, timing...)"] --> CSPRNG["CSPRNG\n(e.g., DRBG)"] --> OUT2["Truly unpredictable\n256-bit tokens"]
    end
```

### Rainbow Table Attack

```mermaid
flowchart TD
    subgraph Build ["Attacker builds rainbow table (once, offline)"]
        PW1["password"] -->|SHA-256| H1["5e884898..."]
        PW2["123456"] -->|SHA-256| H2["8d969eef..."]
        PW3["qwerty"] -->|SHA-256| H3["65e84be3..."]
        PW4["...billions more..."] --> H4["..."]
    end

    subgraph Attack ["Stolen database — unsalted hashes"]
        STOLEN["5e884898... (Alice's hash)"] -->|"O(1) lookup"| FOUND["password 💥"]
    end

    subgraph Defense ["Salted hashes — attack fails"]
        SALT["Random Salt\n(unique per user)"] --> COMBINE["salt + password"]
        COMBINE -->|SHA-256| UNIQUE["a8c3f2b1... (unique)"]
        UNIQUE -->|"lookup"| NOTFOUND["NOT IN TABLE ✅"]
    end

    Build --> Attack
    Build -.->|"table useless\nagainst salted hashes"| Defense
```

| File | Key concepts |
|---|---|
| `TimingAttackExample.java` | Early-exit string comparison, `MessageDigest.isEqual()` constant-time fix |
| `WeakRandomnessExample.java` | `java.util.Random` seed prediction, `SecureRandom` entropy pool |
| `RainbowTableExample.java` | Precomputed lookup tables, salt defeats precomputation |

---

## Security Properties Quick Reference

```mermaid
flowchart TD
    Q["What do you need?"]

    Q --> CONF["Confidentiality\n(only intended reader can read)"]
    Q --> INT["Integrity\n(detect if data was modified)"]
    Q --> AUTH["Authentication\n(prove who sent it)"]
    Q --> NONREP["Non-repudiation\n(sender cannot deny sending)"]

    CONF --> AES["AES-256-GCM\nor AES-256-CBC"]
    INT --> HASH["SHA-256 hash\n(transit integrity)"]
    INT --> HMAC2["HMAC-SHA256\n(authenticated integrity)"]
    AUTH --> HMAC2
    AUTH --> SIG["RSA / ECDSA\nDigital Signature"]
    NONREP --> SIG

    CONF --> RSA2["RSA / ECDSA\n(for key exchange)"]

    style Q fill:#f0f0f0
    style AES fill:#b7d5e8
    style HASH fill:#e8d5b7
    style HMAC2 fill:#e8c8b7
    style SIG fill:#c8e8b7
    style RSA2 fill:#c8e8b7
```

## Key Size Cheat Sheet

| Algorithm | Key size | Security level | Use for |
|---|---|---|---|
| AES | 128-bit | ✅ Good | Symmetric encryption |
| AES | 256-bit | ✅✅ Better | Symmetric encryption |
| RSA | 2048-bit | ✅ Minimum | Asymmetric (legacy) |
| RSA | 4096-bit | ✅✅ Strong | Asymmetric (high-value) |
| EC (P-256) | 256-bit | ✅✅ Strong | ECC — equiv. to RSA-3072 |
| EC (P-384) | 384-bit | ✅✅✅ Very strong | ECC — equiv. to RSA-7680 |
| HMAC-SHA256 | 256-bit | ✅✅ Strong | Authentication |
| PBKDF2-SHA256 | 310k+ iterations | ✅✅ Strong | Password hashing |

## Common Mistakes to Avoid

| ❌ Don't | ✅ Do instead |
|---|---|
| `new Random()` for keys/tokens | `new SecureRandom()` |
| `token.equals(stored)` | `MessageDigest.isEqual(a, b)` |
| AES/ECB mode | AES/GCM/NoPadding |
| `SHA-256(password)` for storage | PBKDF2 / bcrypt / Argon2 |
| Store passwords in plain text | Store `(salt, iterations, hash)` |
| Same IV for every AES encryption | Fresh random IV per encryption |
| MD5 or SHA-1 for integrity | SHA-256 or SHA-3 |
| RSA direct encryption of large data | AES for data, RSA for the AES key |

---

## Dependencies

- **Java 21**
- **BouncyCastle `bcprov-jdk15on:1.68`** — core crypto provider (ECC, extended algorithms)
- **BouncyCastle `bcpkix-jdk15on:1.68`** — PKI / X.509 certificate utilities
