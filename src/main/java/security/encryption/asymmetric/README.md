# Asymmetric Encryption — RSA-2048

Two mathematically linked keys: a public key that anyone can use to encrypt, and a private key that only the owner can use to decrypt. Solves the key distribution problem of symmetric encryption.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.encryption.asymmetric.AsymmetricEncryptionExample"
```

---

## AsymmetricEncryptionExample.java

### Key Generation and Encryption Flow

```mermaid
flowchart TD
    subgraph KeyGen ["1️⃣ Key Generation — RSA-2048"]
        KPG["KeyPairGenerator<br/>(RSA, 2048 bits)"]
        PUB["🔓 Public Key<br/>Share with the world<br/>Used to ENCRYPT"]
        PRIV["🔑 Private Key<br/>NEVER share<br/>Used to DECRYPT"]
        KPG --> PUB
        KPG --> PRIV
    end

    subgraph EncFlow ["2️⃣ Encryption  (anyone with the public key can do this)"]
        PT["Plaintext<br/>'Hello World'"]
        ENC["Cipher.encrypt<br/>(PUBLIC key)"]
        CT["Ciphertext<br/>(Base64-encoded)"]
        PT --> ENC --> CT
    end

    subgraph DecFlow ["3️⃣ Decryption  (only the private key holder can do this)"]
        CT2["Ciphertext<br/>(Base64-encoded)"]
        DEC["Cipher.decrypt<br/>(PRIVATE key)"]
        PT2["Plaintext<br/>'Hello World'"]
        CT2 --> DEC --> PT2
    end

    PUB --> EncFlow
    PRIV --> DecFlow
```

### Symmetric vs Asymmetric

```mermaid
flowchart TD
    SYM["🔒 Symmetric — AES<br/>✅ Fast: GB/sec<br/>✅ Simple to implement<br/>❌ Key sharing problem<br/>→ Use for bulk data encryption"]
    ASYM["🔑 Asymmetric — RSA<br/>✅ No key sharing problem<br/>✅ Public key safe to share<br/>❌ Slow: KB/sec<br/>→ Use only for key exchange"]
    SYM -.-|"combine in<br/>hybrid encryption"| ASYM
```

### How They Work Together — Hybrid Encryption

In practice (TLS, PGP, Signal), RSA and AES are always combined:

```mermaid
flowchart LR
    H1["1. Generate random AES-256 key"] --> H2["2. RSA-encrypt the AES key<br/>with recipient's public key"]
    H2 --> H3["3. AES-encrypt the data"]
    H3 --> H4["4. Send: encrypted AES key<br/>+ AES ciphertext"]
    H4 --> H5["5. Recipient: RSA-decrypt AES key<br/>then AES-decrypt the data"]
```

> RSA handles the key exchange problem. AES handles the speed problem. Together they solve both.
