# Cipher Block Modes — ECB vs CBC vs GCM

AES encrypts exactly 16 bytes at a time. The **mode** determines how consecutive 16-byte blocks are connected. The algorithm is identical in all three — only the mode changes — but the security difference is dramatic.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.encryption.modes.CipherModesComparison"
```

---

## CipherModesComparison.java

### ECB — The Broken Mode

```mermaid
flowchart LR
    subgraph ECB ["❌ ECB — Electronic Code Book  (NEVER USE)"]
        P1["Block 1<br/>'YELLOW SUB'"] --> AES1["AES"] --> C1["CipherBlock1"]
        P2["Block 2<br/>'YELLOW SUB'"] --> AES2["AES"] --> C2["CipherBlock2"]
        P3["Block 3<br/>'YELLOW SUB'"] --> AES3["AES"] --> C3["CipherBlock3"]
    end
    PROB["⚠️ C1 == C2 == C3<br/>Identical input → identical output<br/>An attacker can see repeated data and infer structure<br/>Famous example: encrypting a bitmap still shows the image"]
```

### CBC — Chains Blocks Together

```mermaid
flowchart LR
    subgraph CBC ["✅ CBC — Cipher Block Chaining"]
        IV1["Random IV"] -->|"XOR ⊕"| XOR1[" "]
        P4["Block 1<br/>'YELLOW SUB'"] --> XOR1 --> AES4["AES"] --> CC1["CipherBlock1"]
        CC1 -->|"XOR ⊕"| XOR2[" "]
        P5["Block 2<br/>'YELLOW SUB'"] --> XOR2 --> AES5["AES"] --> CC2["CipherBlock2"]
        CC2 -->|"XOR ⊕"| XOR3[" "]
        P6["Block 3<br/>'YELLOW SUB'"] --> XOR3 --> AES6["AES"] --> CC3["CipherBlock3"]
    end
    FIX["✅ CC1 ≠ CC2 ≠ CC3 even though all plaintext blocks are identical<br/>Each block depends on all previous ciphertext<br/>Limitation: provides confidentiality only — tampering goes undetected"]
```

### GCM — Encryption + Authentication in One Pass

```mermaid
flowchart LR
    subgraph GCM ["🏆 GCM — Galois/Counter Mode  (RECOMMENDED)"]
        NONCE["12-byte Nonce<br/>(unique per message)"] --> CTR["Counter Mode<br/>Encryption"]
        P7["Plaintext"] --> CTR --> CT7["Ciphertext"]
        CT7 --> GHASH["GHASH<br/>(Galois field MAC)"] --> TAG["Auth Tag<br/>(16 bytes)"]
    end
    BEST["🏆 Confidentiality + Integrity in ONE pass<br/>No separate HMAC step needed<br/>Tampering → AEADBadTagException on decrypt<br/>Counter mode: no identical block problem (like ECB)"]
```

### Mode Comparison Summary

```mermaid
flowchart TD
    subgraph Summary ["Mode Decision Guide"]
        D1["Using ECB anywhere? → Stop. Replace with GCM immediately."]
        D2["Need encryption only (legacy system)?  → CBC  with separate HMAC"]
        D3["Building something new?  → AES/GCM/NoPadding  (preferred)"]
        D4["Need to detect tampering automatically?  → GCM  (auth tag does it)"]
    end
```
