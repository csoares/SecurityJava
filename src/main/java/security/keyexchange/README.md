# Key Exchange Protocols

How do Alice and Bob agree on a shared secret when an eavesdropper can see every message? Key exchange protocols solve this through clever mathematics — the shared secret is never transmitted.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.keyexchange.DiffieHellmanExample"
mvn exec:java -Dexec.mainClass="security.keyexchange.ECDHExample"
```

---

## DiffieHellmanExample.java

### The Protocol — What Eve Sees vs What She Can Compute

```mermaid
sequenceDiagram
    participant Alice
    participant Eve as Eve 👁️ (sees everything)
    participant Bob

    Note over Alice,Bob: Step 1 — Agree on public parameters: prime p, generator g
    Alice->>Eve: g, p  (public — Eve can see)
    Eve->>Bob: g, p

    Note over Alice: Step 2 — Alice picks secret a (never transmitted)
    Note over Alice: Computes A = g^a mod p
    Alice->>Eve: A  (Alice's public key)
    Eve->>Bob: A

    Note over Bob: Step 2 — Bob picks secret b (never transmitted)
    Note over Bob: Computes B = g^b mod p
    Bob->>Eve: B  (Bob's public key)
    Eve->>Alice: B

    Note over Alice: Shared secret = B^a mod p = g^(ab) mod p
    Note over Bob: Shared secret = A^b mod p = g^(ab) mod p
    Note over Eve: Eve knows g, p, A = g^a, B = g^b<br/>To find a from A: solve g^a ≡ A (mod p)<br/>= Discrete Logarithm Problem — no efficient algorithm at 2048 bits
    Note over Alice,Bob: ✅ Both computed the same secret without ever transmitting it
```

### Why Eve Cannot Crack It

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph Math
        T_Math["🔢 The Discrete Logarithm Problem"]
        T_Math ~~~ M1
        M1["Eve observes: g, p, A = g^a mod p, B = g^b mod p"]
        M2["To compute the shared secret, Eve needs: a or b"]
        M3["To find a: solve  g^a ≡ A  (mod p)  — this is the DLP"]
        M4["No efficient classical algorithm exists for large prime p"]
        M5["At 2048 bits: longer than the universe's age to brute-force"]
        M1 --> M2 --> M3 --> M4 --> M5
    end
```

### Critical Limitation — No Authentication

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph MITM
        T_MITM["⚠️ Man-in-the-Middle Attack (DH alone is vulnerable)"]
        T_MITM ~~~ ALICE
        ALICE["Alice"]
        EVE["Eve (MITM)"]
        BOB["Bob"]
        ALICE -->|"sends A = g^a"| EVE
        EVE -->|"sends E1=g^e (fake Alice key)"| BOB
        BOB -->|"sends B = g^b"| EVE
        EVE -->|"sends E2=g^e (fake Bob key)"| ALICE
        EVE --> NOTE["Eve holds two separate secrets:<br/>one with Alice, one with Bob<br/>Neither Alice nor Bob suspects!"]
    end

    subgraph Fix
        T_Fix["✅ Fix: Combine DH with Digital Signatures"]
        T_Fix ~~~ F1
        F1["TLS: server signs its DH public key<br/>with its certificate private key"]
        F2["Client verifies the signature<br/>using the server's certificate"]
        F3["Certificate trusted because<br/>it is signed by a trusted CA"]
        F1 --> F2 --> F3
    end

    NOTE --> F1
```

---

## ECDHExample.java

### Classic DH vs Elliptic Curve DH

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph DH
        T_DH["Classic DH"]
        T_DH ~~~ DH1
        DH1["Math: modular exponentiation<br/>g^a mod p"]
        DH2["2048-bit keys for 112-bit security<br/>(~256 byte keys)"]
        DH1 --- DH2
    end

    subgraph ECDH
        T_ECDH["Elliptic Curve DH (ECDH)"]
        T_ECDH ~~~ EC1
        EC1["Math: point multiplication on a curve<br/>a × G  (G = generator point)"]
        EC2["256-bit keys for 128-bit security<br/>(~32 byte keys  =  8× smaller)"]
        EC1 --- EC2
    end

    DH -->|"same idea<br/>better maths"| ECDH
```

### Key Size Comparison

| Security level | Classic DH / RSA | ECDH / ECDSA |
|---|---|---|
| 112-bit | 2048 bits | 224 bits |
| 128-bit | 3072 bits | **256 bits ← P-256 used here** |
| 256-bit | 15360 bits | 521 bits |

### Forward Secrecy — Why Ephemeral Keys Matter

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph FS
        T_FS["🔒 Forward Secrecy with Ephemeral ECDH (ECDHE)"]
        T_FS ~~~ FS1
        FS1["TLS 1.3: generates a fresh ECDH key pair for EVERY session"]
        FS2["Session key exists only in RAM — deleted after handshake"]
        FS3["If server's long-term private key leaks in the future..."]
        FS4["Past sessions are still protected!"]
        FS5["The ephemeral session keys are long gone — nothing to decrypt"]
        FS1 --> FS2 --> FS3 --> FS4 --> FS5
    end
```
