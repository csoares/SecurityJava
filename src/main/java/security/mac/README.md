# MAC — Message Authentication Codes

HMAC (Hash-based Message Authentication Code) adds a **secret key** to hashing. Unlike a plain hash, only parties who hold the shared key can produce or verify the tag — proving both integrity and the sender's identity.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.mac.HMACExample"
```

---

## HMACExample.java

### Alice Sends a Message — Attacker Fails to Forge

```mermaid
sequenceDiagram
    participant Alice as Alice 🔑 (shared secret key)
    participant Network as 🌐 Network
    participant Bob as Bob 🔑 (same shared secret key)

    Alice->>Alice: tag = HMAC-SHA256(key, "Transfer $1000 to account 12345")
    Alice->>Network: message  +  tag

    Note over Network: 😈 Attacker modifies message:<br/>"Transfer $9999 to account 99999"<br/>Cannot produce a valid tag without the secret key!

    Network->>Bob: tampered_message  +  original_tag
    Bob->>Bob: expected = HMAC-SHA256(key, tampered_message)
    Bob->>Bob: MessageDigest.isEqual(expected, original_tag) → FALSE
    Bob->>Bob: ❌ REJECT — message was tampered
```

### HMAC Construction — Why It's Secure

```mermaid
flowchart TD
    subgraph Construction ["⚙️ HMAC(K, m) = H( (K ⊕ opad) ‖ H( (K ⊕ ipad) ‖ m ) )"]
        K["Secret Key K"]
        M["Message m"]
        IPAD["K ⊕ ipad<br/>(inner padding)"]
        OPAD["K ⊕ opad<br/>(outer padding)"]
        H1["SHA-256( ipad_key ‖ message )"]
        H2["SHA-256( opad_key ‖ inner_hash )"]
        TAG["HMAC Tag<br/>(32 bytes)"]
        K --> IPAD --> H1
        M --> H1
        K --> OPAD --> H2
        H1 --> H2 --> TAG
    end
    NOTE["Two-layer hash prevents length-extension attacks<br/>that can break naive H(key ‖ message) constructions"]
```

### Authentication Spectrum

```mermaid
flowchart LR
    HS["🔍 Hash (SHA-256)<br/>No key<br/>Integrity only<br/>Anyone can forge"]
    HM["🔑 HMAC<br/>Shared secret key<br/>Forgery requires the key<br/>Integrity + Authentication"]
    DS["✍️ Digital Signature<br/>Private key signs<br/>Public key verifies<br/>Integrity + Authentication<br/>+ Non-repudiation"]
    HS -->|"add symmetric key"| HM
    HM -->|"switch to asymmetric keys"| DS
```

### Constant-Time Comparison — Why It Matters

```mermaid
flowchart LR
    subgraph Vulnerable ["❌ tag1.equals(tag2)"]
        V1["Exits early on first mismatch"]
        V2["Timing reveals how many bytes matched"]
        V3["Timing attack can recover the expected tag bit by bit"]
        V1 --> V2 --> V3
    end

    subgraph Secure ["✅ MessageDigest.isEqual(a, b)"]
        S1["Always checks ALL bytes"]
        S2["Time is constant regardless of mismatch position"]
        S3["No exploitable timing signal"]
        S1 --> S2 --> S3
    end

    Vulnerable -.->|"always use instead"| Secure
```
