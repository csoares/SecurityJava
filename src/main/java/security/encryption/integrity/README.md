# Integrity & Digital Signatures

Three levels of integrity protection, each adding a new guarantee:

| Class | Mechanism | Guarantees |
|---|---|---|
| `IntegrityCheckHash` | SHA-256 hash | Data not modified in transit |
| `IntegrityCheckSignature` | RSA + SHA-256 | Data not modified **+** came from key owner |
| `FileDigitalSignature` | RSA + SHA-256 on files | Same, applied to files on disk |

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.encryption.integrity.IntegrityCheckHash"
mvn exec:java -Dexec.mainClass="security.encryption.integrity.IntegrityCheckSignature"
mvn exec:java -Dexec.mainClass="security.encryption.integrity.FileDigitalSignature"
```

---

## IntegrityCheckHash.java

### Avalanche Effect

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph Avalanche
        T_Avalanche["⚡ Avalanche Effect — tiny change, huge difference"]
        T_Avalanche ~~~ D1
        D1["'Hello, this is a sample text!'"]
        D2["'Hello, this is a sample TEXT!'<br/>← one character changed"]
        H1["b94d27b9934d3e08...  (SHA-256)"]
        H2["9f86d081884c7d65...  (SHA-256)"]
        D1 -->|"SHA-256"| H1
        D2 -->|"SHA-256"| H2
        DIFF["Completely different hash!<br/>No way to tell how much the input changed."]
        H1 & H2 --> DIFF
    end
```

### Integrity Verification Flow

```mermaid
flowchart LR
    SEND["Sender"] -->|"data + hash"| NET["🌐 Network"]
    NET -->|"data + hash"| RECV["Receiver"]
    RECV -->|"recompute SHA-256(data)"| CMP{"Hashes<br/>equal?"}
    CMP -->|"yes"| OK["✅ Data intact"]
    CMP -->|"no"| FAIL["❌ Data was modified"]
```

### What a Hash Does NOT Prove

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph Limits
        T_Limits["⚠️ Limitations of Plain Hashing"]
        T_Limits ~~~ L1
        L1["❌ Does NOT prove WHO sent the data"]
        L2["❌ Attacker can modify data AND recompute a valid hash"]
        L3["✅ Only proves the data matches the hash — nothing more"]
        L4["→ Use HMAC to add a secret key<br/>→ Use Digital Signatures to add identity"]
        L1 --> L2 --> L3 --> L4
    end
```

---

## IntegrityCheckSignature.java

### Sign and Verify Sequence

```mermaid
sequenceDiagram
    participant Sender as Sender 🔑 (has private key)
    participant Network as 🌐 Network
    participant Receiver as Receiver 🔓 (has public key)

    Sender->>Sender: data = "Hello, this is a sample text!"
    Sender->>Sender: SHA256withRSA.sign(data, privateKey) → signature
    Note over Sender: Signature is unique to this data AND this private key
    Sender->>Network: data  +  signature
    Network->>Receiver: data  +  signature

    Receiver->>Receiver: SHA256withRSA.verify(data, signature, publicKey)

    alt Signature valid — hashes match
        Receiver->>Receiver: ✅ Authentic and unmodified
    else Signature invalid
        Receiver->>Receiver: ❌ Tampered or wrong sender
    end
```

### Under the Hood — SHA256withRSA

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph Sign
        T_Sign["Signing"]
        T_Sign ~~~ DATA
        DATA["Data bytes"] -->|"SHA-256"| HASH["Hash (32 bytes)"]
        HASH -->|"RSA-encrypt<br/>with PRIVATE key"| SIG["Signature<br/>(256 bytes)"]
    end

    subgraph Verify
        T_Verify["Verification"]
        T_Verify ~~~ SIG2
        SIG2["Signature"] -->|"RSA-decrypt<br/>with PUBLIC key"| HASH2["Original Hash"]
        DATA2["Received Data"] -->|"SHA-256"| HASH3["Recomputed Hash"]
        HASH2 & HASH3 -->|"compare"| RESULT{"Equal?"}
        RESULT -->|"yes"| VALID["✅ Valid"]
        RESULT -->|"no"| INVALID["❌ Invalid"]
    end

    SIG --> SIG2
```

### What a Valid Signature Proves

```mermaid
flowchart TD
    G1["✅ Integrity — data has not been modified"]
    G2["✅ Authenticity — signed by the holder of the private key"]
    G3["✅ Non-repudiation — sender cannot deny having signed it"]
    G4["❌ Does NOT prove the key holder's real-world identity<br/>→ Need a Certificate Authority (PKI) to bind key to identity"]
    G1 --> G2 --> G3 --> G4
```

---

## FileDigitalSignature.java

### Signing a File

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph Sign
        T_Sign["✍️ Signing  (Sender)"]
        T_Sign ~~~ FILE
        FILE["📄 document.txt"]
        HASH["SHA-256 Hash<br/>(32 bytes fixed — any file size)"]
        SIG["🔏 signature.sig<br/>(256 bytes)"]
        FILE -->|"computeSHA256()"| HASH
        HASH -->|"RSA sign<br/>(private key)"| SIG
        HASH -.->|"Why hash first?"| WHY["RSA max input ≈ 245 bytes<br/>Hash reduces any file to 32 bytes"]
    end
```

### Verifying a File

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph Verify
        T_Verify["🔍 Verification  (Receiver)"]
        T_Verify ~~~ FILE2
        FILE2["📄 document.txt<br/>(received)"]
        SIG2["🔏 signature.sig"]
        HASH2["Recomputed SHA-256"]
        ORIG["Original Hash<br/>(extracted from signature)"]
        CMP{"Match?"}
        FILE2 -->|"computeSHA256()"| HASH2
        SIG2 -->|"RSA verify<br/>(public key)"| ORIG
        HASH2 --> CMP
        ORIG --> CMP
        CMP -->|"yes"| OK["✅ Authentic<br/>& Unmodified"]
        CMP -->|"no"| FAIL["❌ Tampered<br/>or wrong key"]
    end
```

### Tamper Detection Demo

```mermaid
flowchart TD
    T1["Original: 'This is the content of the file...'"]
    T2["SHA-256 → abc123...  (stored in .sig)"]
    T3["File tampered: 'MODIFIED by attacker!'"]
    T4["SHA-256 → xyz789...  (completely different)"]
    T5["abc123 ≠ xyz789  →  🚨 ALERT: File integrity check FAILED"]
    T1 --> T2
    T3 --> T4
    T2 & T4 --> T5
```
