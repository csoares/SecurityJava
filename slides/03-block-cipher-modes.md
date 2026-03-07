---
marp: true
theme: default
paginate: true
style: |
  section { font-size: 1.4rem; }
  section.lead h1 { font-size: 2.6rem; }
  section.lead h2 { font-size: 1.8rem; color: #555; }
  pre { font-size: 1rem; }
  blockquote { border-left: 4px solid #f90; padding-left: 1em; color: #555; }
---

<!-- _class: lead -->

# Lecture 3
## Block Cipher Modes
### ECB · CBC · GCM — same key, very different results

---

## The Problem

AES encrypts exactly **16 bytes** at a time.

What if your message is 1 KB? 1 MB? 1 GB?

**Block cipher modes** define how AES is applied repeatedly to long data.

Different modes give **completely different security properties** — even with the same perfect AES underneath.

> Choosing the wrong mode is like having a strong safe but leaving the door open.

---

## Mode 1 — ECB: The Stamp of Doom

**ECB = Electronic Codebook**

Each 16-byte block is encrypted **independently**:

```
┌──────────┐        ┌──────────┐
│ Block 1  │──AES──►│Cipher 1  │
└──────────┘  key   └──────────┘

┌──────────┐        ┌──────────┐
│ Block 2  │──AES──►│Cipher 2  │
└──────────┘  key   └──────────┘

┌──────────┐        ┌──────────┐
│ Block 1  │──AES──►│Cipher 1  │  ← SAME output as first block!
└──────────┘  key   └──────────┘
```

**Fatal flaw:** identical plaintext blocks → identical ciphertext blocks.

---

## ECB Visualised — The Penguin Problem

Encrypt a bitmap image of a penguin with ECB:

```
Original image         ECB encrypted          GCM encrypted
┌─────────────┐       ┌─────────────┐        ┌─────────────┐
│             │       │             │        │▓▒░▓▒░▒▓░▒▓▒░│
│   🐧        │  →    │   🐧        │   →    │░▓▒░▓▒░▓░▓▒░▓│
│             │       │             │        │▒░▓▒░▓▒░▓▒░▓▒│
└─────────────┘       └─────────────┘        └─────────────┘
  Recognisable!       Still recognisable!     Looks random ✓
```

ECB preserves patterns because pixels in the background are all the same colour → identical blocks → identical encrypted blocks.

**NEVER use ECB for real data.**

---

## Mode 2 — CBC: Chain the Blocks

**CBC = Cipher Block Chaining**

Each block is XOR'd with the previous ciphertext before encryption:

```
IV──►XOR──►AES──►Cipher1──►XOR──►AES──►Cipher2──►XOR──►AES──►Cipher3
     ▲              │              ▲              │
  Block1            └──────────►Block2            └──────────►Block3
```

Now identical blocks produce different ciphertext — patterns are hidden.

**But CBC has a problem...**

---

## CBC's Hidden Weakness — Padding Oracle

CBC needs padding to fill the last block to 16 bytes.

```
Message: "Hello"  (5 bytes)
Padding: "Hello" + [0x0B 0x0B 0x0B 0x0B 0x0B 0x0B 0x0B 0x0B 0x0B 0x0B 0x0B]
                   ← 11 bytes of padding to reach 16 bytes
```

**Padding Oracle Attack:**
If an app returns different errors for "bad padding" vs "bad data",
an attacker can decrypt messages **without the key** by asking the server
to decrypt modified ciphertext and watching which error comes back.

Affected: TLS, many web frameworks (2011).

---

## Mode 3 — GCM: The Right Answer

**GCM = Galois/Counter Mode**

GCM does two things at once:
```
┌──────────────────────────────────────────────────────┐
│  1. ENCRYPT the message (CTR mode — stream cipher)   │
│     → confidentiality: message is unreadable         │
├──────────────────────────────────────────────────────┤
│  2. AUTHENTICATE with a tag (GHASH)                  │
│     → integrity: any modification is detected        │
└──────────────────────────────────────────────────────┘
```

GCM appends a **16-byte authentication tag** to the ciphertext.
If anyone modifies even one bit of the ciphertext, decryption fails immediately.

---

## GCM Authentication Tag — What It Does

```
Alice encrypts:  "Transfer $1000"
                         │
                    AES-GCM(key, iv)
                         │
               ciphertext + tag(XXXX)

Mallory intercepts and changes one byte of ciphertext

Bob decrypts:
  recomputes tag → YYYY
  YYYY ≠ XXXX → 💥 AEADBadTagException — REJECTED
```

**GCM gives you both encryption AND tamper detection in one step.**

---

## GCM in Java

```java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

// Encrypt — tag is automatically appended to ciphertext
GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128-bit tag
cipher.init(Cipher.ENCRYPT_MODE, key, spec);
byte[] ciphertext = cipher.doFinal(plaintext);
// ciphertext is 16 bytes longer than plaintext (the tag)

// Decrypt — throws AEADBadTagException if tampered
cipher.init(Cipher.DECRYPT_MODE, key, spec);
byte[] plaintext = cipher.doFinal(ciphertext); // safe ✓
```

---

## GCM — The IV Reuse Catastrophe

⚠️ GCM has one critical rule: **never reuse an IV with the same key**.

```
If you encrypt two messages with the same key AND same IV:

  C1 = P1 XOR keystream
  C2 = P2 XOR keystream  ← same keystream!

  C1 XOR C2 = P1 XOR P2  ← attacker recovers XOR of both plaintexts

  AND the authentication key is completely recovered.
  Every past and future message is broken.
```

Fix: generate a fresh random 12-byte IV for **every** message.

---

## Mode Comparison — Decision Chart

```
Do you need encryption?
  │
  ├─ No  → use HMAC (Lecture 6)
  │
  └─ Yes ──► Use AES
               │
               ├─ Do you need tamper detection too?
               │    └─ Yes (almost always) → GCM ✓
               │    └─ No → CTR
               │
               └─ Are you tempted to use ECB?
                    └─ STOP. Never use ECB.
```

**Default answer: AES-256-GCM with a random 12-byte IV.**

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.encryption.modes.CipherModesComparison"
```

**What to observe:**
- ECB: encrypt repeated blocks → see the identical ciphertext blocks
- CBC: patterns disappear, but no tamper detection
- GCM: tamper a single byte of ciphertext → exception thrown
- GCM with wrong IV: decryption produces garbage

---

<!-- _class: lead -->

## Next: Lecture 4
# Hashing & Integrity
### Digital fingerprints — one way only
