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
        KPG["KeyPairGenerator\n(RSA, 2048 bits)"]
        PUB["🔓 Public Key\nShare with the world\nUsed to ENCRYPT"]
        PRIV["🔑 Private Key\nNEVER share\nUsed to DECRYPT"]
        KPG --> PUB
        KPG --> PRIV
    end

    subgraph EncFlow ["2️⃣ Encryption  (anyone with the public key can do this)"]
        PT["Plaintext\n'Hello World'"]
        ENC["Cipher.encrypt\n(PUBLIC key)"]
        CT["Ciphertext\n(Base64-encoded)"]
        PT --> ENC --> CT
    end

    subgraph DecFlow ["3️⃣ Decryption  (only the private key holder can do this)"]
        CT2["Ciphertext\n(Base64-encoded)"]
        DEC["Cipher.decrypt\n(PRIVATE key)"]
        PT2["Plaintext\n'Hello World'"]
        CT2 --> DEC --> PT2
    end

    PUB --> EncFlow
    PRIV --> DecFlow
```

### Symmetric vs Asymmetric

```mermaid
flowchart LR
    subgraph Comparison ["Symmetric vs Asymmetric"]
        SYM["🔒 Symmetric (AES)\n✅ Fast — GB/sec\n✅ Simple\n❌ Key sharing problem\n→ Use for bulk data"]
        ASYM["🔑 Asymmetric (RSA)\n✅ No key sharing problem\n✅ Public key safe to share\n❌ Slow — KB/sec\n❌ Limited payload size\n→ Use for key exchange only"]
    end
```

### How They Work Together — Hybrid Encryption

In practice (TLS, PGP, Signal), RSA and AES are always combined:

```mermaid
flowchart LR
    H1["1. Generate random AES-256 key"] --> H2["2. RSA-encrypt the AES key\nwith recipient's public key"]
    H2 --> H3["3. AES-encrypt the actual data"]
    H3 --> H4["4. Send: encrypted AES key\n+ AES ciphertext"]
    H4 --> H5["5. Recipient: RSA-decrypt AES key\n→ AES-decrypt the data"]
```

> RSA handles the key exchange problem. AES handles the speed problem. Together they solve both.
