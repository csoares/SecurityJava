---
marp: true
theme: default
paginate: true
style: |
  section { font-size: 1.4rem; }
  section.lead h1 { font-size: 2.8rem; }
  code { font-size: 1.1rem; }
---

<!-- _class: lead -->

# Lecture 3
## Block Cipher Modes

ECB · CBC · GCM

---

## The Problem: AES is a Block Cipher

AES encrypts exactly 16 bytes at a time.

What if your message is longer? Or shorter?
What if two blocks of plaintext are identical?

**Block cipher modes** define how AES is applied to arbitrary-length data.

Choosing the wrong mode is a critical security mistake — even with a perfect key.

---

## ECB — Electronic Codebook (NEVER USE)

Each 16-byte block encrypted independently with the same key.

```
Block 1: [plaintext_1] → AES(key) → [cipher_1]
Block 2: [plaintext_2] → AES(key) → [cipher_2]
Block 3: [plaintext_1] → AES(key) → [cipher_1]  ← SAME output!
```

**Fatal flaw:** identical plaintext blocks → identical ciphertext blocks.
Patterns in the data survive encryption.

---

## The ECB Penguin — A Famous Demonstration

The Linux Tux penguin image encrypted with ECB:

```
Original image:        ECB encrypted:         CBC encrypted:
  [Penguin]              [Penguin outline]       [Random noise]
  Clearly visible!       Still recognisable!     Completely hidden
```

ECB preserves the visual structure because identical pixel blocks produce identical encrypted blocks.

**ECB must never be used for real data.**

---

## CBC — Cipher Block Chaining

Each plaintext block is XOR'd with the previous ciphertext block before encryption.

```
IV → XOR(P1) → AES → C1
C1 → XOR(P2) → AES → C2
C2 → XOR(P3) → AES → C3
```

- Identical plaintext blocks → different ciphertext (due to chaining)
- Requires a random IV for the first block
- **Weakness:** no built-in integrity — an attacker can flip bits in ciphertext to predictably alter plaintext

---

## CBC Padding Oracle Attack

CBC requires padding to fill the last block to 16 bytes.

If the server reveals whether padding is valid (even via timing), an attacker can:
1. Flip bytes in the ciphertext
2. Ask the server to decrypt
3. Learn the plaintext byte by byte — **without the key**

**Famous victims:** Lucky 13 (TLS), POODLE (SSL 3.0), many web frameworks.

**Lesson:** encryption alone is not enough — you need integrity too.

---

## GCM — Galois/Counter Mode (Use This)

GCM = AES-CTR encryption + GHASH authentication tag

```
Plaintext → AES-CTR(key, IV) → Ciphertext
Ciphertext + AAD → GHASH(key) → Authentication Tag (16 bytes)
```

- **Confidentiality:** CTR mode (stream cipher, no padding needed)
- **Integrity:** GHASH tag — any modification to ciphertext is detected
- **Authenticated Encryption with Associated Data (AEAD)**

---

## GCM in Java

```java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128-bit tag

// Encrypt
cipher.init(Cipher.ENCRYPT_MODE, key, spec);
cipher.updateAAD(associatedData); // optional but recommended
byte[] ciphertext = cipher.doFinal(plaintext);
// ciphertext includes the 16-byte auth tag appended

// Decrypt (throws AEADBadTagException if tampered)
cipher.init(Cipher.DECRYPT_MODE, key, spec);
byte[] plaintext = cipher.doFinal(ciphertext);
```

---

## GCM: The IV Rule — Critical

**Never reuse an IV with the same key in GCM.**

```
If IV is reused:
  C1 = P1 XOR keystream
  C2 = P2 XOR keystream   ← same keystream!
  C1 XOR C2 = P1 XOR P2   ← attacker recovers XOR of plaintexts
  AND the authentication key is fully recovered
```

GCM IV reuse is far more catastrophic than CBC IV reuse.
Use a random 96-bit IV for every message.

---

## Mode Comparison

| Mode | Confidentiality | Integrity | Parallel | Notes |
|------|----------------|-----------|----------|-------|
| ECB | Weak (patterns) | None | Yes | **Never use** |
| CBC | Good | None | Decrypt only | Padding oracle risk |
| CTR | Good | None | Yes | Used inside GCM |
| **GCM** | **Good** | **Yes** | **Yes** | **Use this** |

GCM is the modern standard. TLS 1.3 only allows AEAD modes.

---

## Associated Data in GCM

GCM can authenticate additional data (AAD) that is not encrypted:

```java
cipher.updateAAD("sender=alice,recipient=bob".getBytes());
```

The AAD is integrity-protected but not confidential.
Use it for: headers, metadata, routing information.

If the AAD is modified, decryption throws `AEADBadTagException`.

---

## Running the Example

```bash
mvn exec:java -Dexec.mainClass="security.encryption.modes.CipherModesComparison"
```

Observe:
- ECB: encrypt an image or repeated blocks — patterns survive
- CBC: patterns disappear but no integrity check
- GCM: encrypted + tamper detection via auth tag

---

<!-- _class: lead -->

## Next: Lecture 4
# Hashing & Integrity
### One-way functions and the avalanche effect
