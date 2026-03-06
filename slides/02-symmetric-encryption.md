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

# Lecture 2
## Symmetric Encryption

AES · Keys · IVs · Block vs Stream

---

## What is Symmetric Encryption?

One key is used for both encryption and decryption.

```
Alice                          Bob
  |                              |
  |--- encrypt(data, key) ------>|
  |         ciphertext           |
  |                    decrypt(ciphertext, key)
  |                              |
```

Both parties must already share the key.
**Problem:** how do you securely share that key? → Lecture 7 (Key Exchange)

---

## AES — Advanced Encryption Standard

- Adopted as global standard by NIST in 2001
- Block cipher: operates on 128-bit (16-byte) blocks
- Key sizes: **128**, 192, or **256** bits
- Based on substitution-permutation network (SPN)

```java
KeyGenerator keyGen = KeyGenerator.getInstance("AES");
keyGen.init(256); // 128, 192, or 256
SecretKey key = keyGen.generateKey();
```

AES is used everywhere: TLS, disk encryption, Wi-Fi (WPA2), file encryption.

---

## How AES Works (Simplified)

AES applies 10–14 rounds (depending on key size), each doing 4 operations:

| Step | Operation | Purpose |
|------|-----------|---------|
| **SubBytes** | S-box substitution | Confusion |
| **ShiftRows** | Row permutation | Diffusion |
| **MixColumns** | Column mixing | Diffusion |
| **AddRoundKey** | XOR with key schedule | Key mixing |

After every round, the output looks completely random.

---

## Key Size vs Security

| Key size | Possible keys | Brute-force time (1 trillion guesses/sec) |
|----------|--------------|------------------------------------------|
| 56-bit (DES) | 7.2 × 10^16 | ~83 days — **cracked in 1998** |
| 128-bit AES | 3.4 × 10^38 | ~10^19 years |
| 256-bit AES | 1.2 × 10^77 | longer than universe's age |

**Use AES-256.** There is no known attack significantly better than brute force.

---

## Encryption and Decryption in Java

```java
// Encrypt
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

// Decrypt
cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
byte[] decrypted = cipher.doFinal(ciphertext);
```

Three choices in `"AES/GCM/NoPadding"`:
1. **Algorithm** — AES
2. **Mode** — GCM (→ Lecture 3)
3. **Padding** — NoPadding (GCM handles it)

---

## The IV — Initialisation Vector

A random value combined with the key to make each encryption unique.

```java
byte[] iv = new byte[12]; // 96 bits for GCM
new SecureRandom().nextBytes(iv);
```

**Rule:** never reuse the same IV with the same key.

```
Same key + Same IV + Different messages = CATASTROPHIC failure
Same key + New IV  + Different messages = Safe
```

The IV is not secret — it is sent alongside the ciphertext.

---

## Base64 — Transmitting Binary Data

Encrypted output is raw bytes. To store or transmit as text, encode in Base64.

```java
String encoded = Base64.getEncoder().encodeToString(ciphertext);
byte[] decoded  = Base64.getDecoder().decode(encoded);
```

```
Binary:  [0xFF, 0x3A, 0x9C, 0x11, ...]
Base64:  "/zqcEQ=="
```

Base64 adds ~33% size overhead. It is **not** encryption — it is encoding only.

---

## Key Storage — What NOT to Do

```java
// BAD — hardcoded key
String key = "mysecretkey12345";

// BAD — derived from weak source
SecretKey key = new SecretKeySpec("password".getBytes(), "AES");

// GOOD — generated securely
KeyGenerator gen = KeyGenerator.getInstance("AES");
gen.init(256, new SecureRandom());
SecretKey key = gen.generateKey();
```

Keys must be stored in a **KeyStore**, **HSM**, or a secrets manager — never in source code.

---

## Running the Example

```bash
mvn exec:java -Dexec.mainClass="security.encryption.symmetric.SymmetricEncryptionExample"
```

You will see:
- Random AES-256 key generated
- Random IV generated
- Plaintext encrypted → Base64 encoded ciphertext
- Ciphertext decrypted → original plaintext recovered

---

<!-- _class: lead -->

## Next: Lecture 3
# Block Cipher Modes
### Why HOW you use AES matters as much as AES itself
