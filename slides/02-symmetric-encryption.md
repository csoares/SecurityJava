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

# Lecture 2
## Symmetric Encryption
### AES — one key to lock, same key to unlock

---

## The Padlock Analogy

Symmetric encryption is like a **combination padlock**:

```
Alice                              Bob
  📦  +  🔒 (lock it)               🔒 + 🔑 (unlock it)
  message   key: 1234              same key: 1234
```

- Both Alice and Bob use the **same key**
- Lock = encrypt,  Unlock = decrypt
- If Eve intercepts the box, she can't open it without the key

**The challenge:** how do Alice and Bob agree on the key in the first place?
(That's Lecture 7 — Key Exchange)

---

## AES — The Global Standard

AES (Advanced Encryption Standard) was chosen by the US government in 2001 after a public competition. It replaced the aging DES.

```
AES facts:
  ✓ Used in every HTTPS connection
  ✓ Used for disk encryption (FileVault, BitLocker)
  ✓ Used in Wi-Fi (WPA2/WPA3)
  ✓ Used in messaging apps (Signal, WhatsApp)
  ✓ Built into CPUs as hardware instructions (AES-NI)
  ✓ No known practical attacks after 25 years
```

**If you use one algorithm from this course, it's AES.**

---

## How AES Works (Simplified)

AES scrambles data in **rounds** — like shuffling a deck of cards many times:

```
Plaintext (16 bytes)
     │
     ▼
┌─────────────┐
│  Round 1    │ ← substitute bytes, shift rows, mix columns, add key
├─────────────┤
│  Round 2    │ ← same operations, different key material
├─────────────┤
│    ...      │ ← AES-256 = 14 rounds total
├─────────────┤
│  Round 14   │
└─────────────┘
     │
     ▼
Ciphertext (16 bytes, looks completely random)
```

Each round adds more confusion. After 14 rounds — unbreakable.

---

## Key Size Matters — A Lot

```
┌──────────────┬──────────────────┬─────────────────────────────┐
│  Key size    │  Possible keys   │  Time to brute force        │
├──────────────┼──────────────────┼─────────────────────────────┤
│  DES 56-bit  │  72 trillion     │  Hours — BROKEN in 1998     │
│  AES 128-bit │  340 undecillion │  ~10^19 years               │
│  AES 256-bit │  10^77           │  longer than universe age   │
└──────────────┴──────────────────┴─────────────────────────────┘
```

> "If every atom in the observable universe were a computer checking a billion keys per second, you still could not brute-force AES-128 in the universe's lifetime."

**Use AES-256. It costs nothing extra.**

---

## AES in Java — Generate a Key

```java
// Step 1: Generate a random 256-bit key
KeyGenerator keyGen = KeyGenerator.getInstance("AES");
keyGen.init(256, new SecureRandom());  // 256-bit key
SecretKey key = keyGen.generateKey();

// The key is just 32 random bytes:
// [0xA3, 0x7F, 0x12, 0xBB, ... 32 bytes total]
```

⚠️ **Never do this:**
```java
SecretKey key = new SecretKeySpec("mysecretpassword".getBytes(), "AES");
// passwords are short and predictable — bad keys!
```

---

## AES in Java — Encrypt and Decrypt

```java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

// Encrypt
cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
byte[] ciphertext = cipher.doFinal("Hello, Bob!".getBytes());

// Decrypt
cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
byte[] plaintext = cipher.doFinal(ciphertext);

System.out.println(new String(plaintext)); // → "Hello, Bob!"
```

Three parts of `"AES/GCM/NoPadding"`:
1. **AES** — algorithm
2. **GCM** — mode (how to handle multiple blocks → Lecture 3)
3. **NoPadding** — GCM handles variable length automatically

---

## What is an IV?

**IV = Initialisation Vector** — a random value to make each encryption unique.

```
Problem without IV:
  encrypt("Hello", key) → X9A3...
  encrypt("Hello", key) → X9A3...   ← identical! Eve notices patterns

Solution with IV:
  encrypt("Hello", key, iv1) → X9A3...
  encrypt("Hello", key, iv2) → 7F2B...  ← completely different! ✓
```

```java
byte[] iv = new byte[12];          // 12 bytes = 96 bits
new SecureRandom().nextBytes(iv);  // random every time
```

**The IV is NOT secret** — send it alongside the ciphertext.
**Rule: never reuse the same IV with the same key.**

---

## What is Base64?

Encrypted data is raw bytes (binary). Binary data doesn't travel well in emails, JSON, or databases.

**Base64 converts binary to printable text:**

```
Binary:  [0xFF, 0x3A, 0x9C]
Base64:  "/zqc"           ← only uses A-Z, a-z, 0-9, +, /
```

```java
String encoded = Base64.getEncoder().encodeToString(ciphertext);
// → "X9A37FmB9z..." — safe to store anywhere

byte[] back = Base64.getDecoder().decode(encoded);
```

> ⚠️ Base64 is NOT encryption. It's just a way to represent bytes as text.
> Anyone can decode it.

---

## Full Flow: Encrypt → Store → Decrypt

```
ENCRYPT:
  plaintext = "Transfer $1000"
  key = random 256-bit key
  iv  = random 96-bit value
  ciphertext = AES-GCM(plaintext, key, iv)
  store: Base64(iv) + ":" + Base64(ciphertext)

DECRYPT:
  split stored value on ":"
  iv        = Base64.decode(first part)
  ciphertext = Base64.decode(second part)
  plaintext = AES-GCM-decrypt(ciphertext, key, iv)
```

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.encryption.symmetric.SymmetricEncryptionExample"
```

**What to observe:**
- The key is 32 random bytes — looks like noise
- The IV is 12 random bytes — different every run
- The same plaintext encrypts to a different ciphertext every run (because IV changes)
- Decryption restores the original perfectly

---

<!-- _class: lead -->

## Next: Lecture 3
# Block Cipher Modes
### Why HOW you use AES matters as much as AES itself
