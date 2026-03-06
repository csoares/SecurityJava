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
        C1 & C2 & C3 --> PROB["⚠️ C1 == C2 == C3<br/>Identical input → identical output<br/>Patterns are visible to attackers!"]
    end
```

### CBC — Chains Blocks Together

```mermaid
flowchart LR
    subgraph CBC ["✅ CBC — Cipher Block Chaining"]
        IV1["Random IV"] -->|"XOR ⊕"| XOR1["⊕"]
        P4["Block 1<br/>'YELLOW SUB'"] --> XOR1 --> AES4["AES"] --> CC1["CipherBlock1"]
        CC1 -->|"XOR ⊕"| XOR2["⊕"]
        P5["Block 2<br/>'YELLOW SUB'"] --> XOR2 --> AES5["AES"] --> CC2["CipherBlock2"]
        CC2 -->|"XOR ⊕"| XOR3["⊕"]
        P6["Block 3<br/>'YELLOW SUB'"] --> XOR3 --> AES6["AES"] --> CC3["CipherBlock3"]
        CC3 --> FIX["✅ CC1 ≠ CC2 ≠ CC3 even with identical plaintext<br/>⚠️ Confidentiality only — no tamper detection"]
    end
```

### GCM — Encryption + Authentication in One Pass

```mermaid
flowchart LR
    subgraph GCM ["🏆 GCM — Galois/Counter Mode  (RECOMMENDED)"]
        NONCE["12-byte Nonce<br/>(unique per message)"] --> CTR["Counter Mode<br/>Encryption"]
        P7["Plaintext"] --> CTR --> CT7["Ciphertext"]
        CT7 --> GHASH["GHASH<br/>(Galois field MAC)"] --> TAG["Auth Tag<br/>(16 bytes)"]
        TAG --> BEST["Confidentiality + Integrity in ONE pass<br/>Tampering → AEADBadTagException"]
    end
```

### Mode Comparison Summary

```mermaid
flowchart TD
    subgraph Summary ["Mode Decision Guide"]
        D1["Using ECB? → Stop. Replace with GCM immediately."]
        D2["Legacy system, encryption only? → CBC + separate HMAC"]
        D3["Building something new? → AES/GCM/NoPadding"]
        D4["Need tamper detection? → GCM auth tag does it automatically"]
        D1 --> D2 --> D3 --> D4
    end
```
