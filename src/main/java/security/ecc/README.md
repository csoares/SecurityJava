# Elliptic Curve Cryptography — ECDSA

Same goals as RSA (signing, key exchange) but using elliptic curve mathematics instead of integer factorisation. The payoff: dramatically smaller keys for equivalent or better security.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.ecc.ECCSignatureExample"
```

---

## ECCSignatureExample.java

### Sign and Verify Flow

```mermaid
flowchart TD
    subgraph Sign ["✍️ Signing  (private key — signer only)"]
        MSG["Message<br/>'Transfer 100 shares of ACME Corp'"]
        HASH["SHA-256 Hash<br/>(32 bytes)"]
        RND["Random nonce k<br/>(different every time — CRITICAL)"]
        SIG["Signature (r, s)<br/>~64–70 bytes for P-256"]
        MSG -->|"SHA-256"| HASH
        HASH -->|"ECDSA sign<br/>(private key + k)"| SIG
        RND --> SIG
        SIG --> WARN["⚠️ If k is ever reused<br/>→ private key is mathematically recovered!<br/>Real case: PS3 master key stolen this way in 2010"]
    end

    subgraph Verify ["🔍 Verification  (public key — anyone can do this)"]
        MSG2["Received Message"]
        SIG2["Received Signature (r, s)"]
        HASH2["SHA-256 Hash"]
        RESULT{"Valid?"}
        MSG2 -->|"SHA-256"| HASH2
        HASH2 -->|"ECDSA verify<br/>(public key)"| RESULT
        SIG2 --> RESULT
        RESULT -->|"yes"| OK["✅ Authentic & Unmodified"]
        RESULT -->|"no"| FAIL["❌ Tampered or wrong key"]
    end
```

### RSA vs ECDSA Key Size Comparison

```mermaid
flowchart LR
    subgraph RSA ["RSA-2048"]
        R1["Private key: ~1217 bytes"]
        R2["Public key:  ~294 bytes"]
        R3["Signature:   ~256 bytes"]
        R4["Security:    ~112-bit"]
        R1 --- R2 --- R3 --- R4
    end

    subgraph ECC ["ECDSA P-256"]
        E1["Private key: ~67 bytes"]
        E2["Public key:  ~91 bytes"]
        E3["Signature:   ~64–70 bytes"]
        E4["Security:    ~128-bit  (MORE secure!)"]
        E1 --- E2 --- E3 --- E4
    end

    RSA -->|"8× smaller keys<br/>stronger security"| ECC
```

### Real-World Uses

```mermaid
flowchart TD
    subgraph Uses ["🌍 Where ECDSA / ECC Is Used"]
        U1["Bitcoin / Ethereum — secp256k1 curve, signs every transaction"]
        U2["TLS 1.3 — ECDSA certificates are smaller and faster than RSA"]
        U3["JWT ES256 — compact signed API tokens"]
        U4["SSH — ecdsa-sha2-nistp256 key type"]
        U5["Android APK signing — every app on the Play Store"]
        U6["Signal / WhatsApp / iMessage — ECDH key exchange"]
        U1 --- U2 --- U3 --- U4 --- U5 --- U6
    end
```

### ECC vs RSA — The Core Difference

```mermaid
flowchart TD
    subgraph Hardness ["Hard Problems That Provide Security"]
        RSA_H["RSA — Integer Factorisation<br/>Given n = p × q, find p and q<br/>Hard at 2048+ bits"]
        ECC_H["ECC — Elliptic Curve Discrete Log (ECDLP)<br/>Given P = k × G, find k<br/>Harder per bit — 256-bit ECC ≈ 3072-bit RSA"]
        RSA_H --- ECC_H
    end
```
