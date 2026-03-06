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

# Lecture 4
## Hashing & Integrity

SHA-256 · Avalanche Effect · Merkle Trees · Limitations

---

## What is a Hash Function?

A function that maps **any input** to a **fixed-size output** (digest).

```
SHA-256("Hello, World!")  →  dffd6021bb2bd5b0...  (64 hex chars = 32 bytes)
SHA-256("Hello, World?")  →  a0dc65ffca799873...  completely different!
SHA-256(1 GB file)        →  some 32-byte value
SHA-256(empty string)     →  e3b0c44298fc1c14...  still 32 bytes
```

**Key properties:**
- Deterministic: same input always gives same output
- Fixed output size regardless of input size
- Fast to compute

---

## Properties of a Cryptographic Hash

| Property | Meaning |
|----------|---------|
| **Pre-image resistance** | Given hash H, cannot find input M where SHA(M)=H |
| **Second pre-image resistance** | Given M, cannot find M' ≠ M where SHA(M)=SHA(M') |
| **Collision resistance** | Cannot find any M, M' where SHA(M)=SHA(M') |

These properties make hashes useful for integrity verification.
**SHA-256 satisfies all three.** MD5 and SHA-1 do not (collisions found).

---

## The Avalanche Effect

A single-bit change in input causes ~50% of output bits to flip.

```
SHA-256("Hello, this is a sample text!")
→  b94d27b9934d3e08a52e52d7da7dabfac484efe04294e576

SHA-256("Hello, this is a sample TEXT!")
→  9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822

Difference: completely different — no correlation visible
```

This prevents statistical analysis of hash outputs.

---

## Integrity Verification

```
Sender                              Receiver
  |                                    |
  | hash = SHA256(file)                |
  | send(file, hash) ─────────────────>|
  |                                    | hash2 = SHA256(received_file)
  |                                    | if hash == hash2: OK
  |                                    | else: TAMPERED
```

Used for:
- Software download verification
- Git commit IDs
- Database record checksums
- TLS record integrity (inside GCM)

---

## What a Hash Does NOT Prove

```java
// Receiver computes:
String received_hash = sha256(receivedData);
String expected_hash = sha256(originalData);
boolean intact = received_hash.equals(expected_hash);
```

**Problem:** an attacker who intercepts the transmission can:
1. Modify the data
2. Recompute the hash
3. Send both the new data and new hash

A plain hash proves **integrity only if the hash is delivered separately and securely**.

→ Solution: HMAC (Lecture 6) or Digital Signatures (Lecture 5)

---

## Hashing in Java

```java
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

// Convert to hex string for display
StringBuilder hex = new StringBuilder();
for (byte b : hash) {
    hex.append(String.format("%02x", b));
}
System.out.println(hex.toString()); // 64-char hex string
```

---

## Hash Algorithms — What to Use

| Algorithm | Output | Status |
|-----------|--------|--------|
| MD5 | 128 bits | **Broken** — collisions found (1996) |
| SHA-1 | 160 bits | **Broken** — Google SHAttered (2017) |
| **SHA-256** | 256 bits | **Safe** — use this |
| **SHA-3-256** | 256 bits | **Safe** — different construction |
| **BLAKE2** | variable | **Safe** — faster than SHA-256 |

Never use MD5 or SHA-1 for security purposes.
SHA-256 is the default choice.

---

## Merkle Trees

Hash trees allow efficient integrity verification of large datasets.

```
         Root = H(H12 + H34)
        /                  \
  H12 = H(H1+H2)     H34 = H(H3+H4)
   /         \           /        \
 H1=H(B1)  H2=H(B2)  H3=H(B3)  H4=H(B4)
    |          |          |          |
  Block1    Block2     Block3    Block4
```

To verify Block3, you only need: H3, H4, H12, and Root.
You do **not** need to download all blocks.

Used in: Git, Bitcoin, certificate transparency logs.

---

## Git Uses SHA-1 (Moving to SHA-256)

Every Git commit is identified by a SHA-1 hash of its contents:

```bash
git log --oneline
a3f2bc1 Add encryption example
7d4e8f2 Fix bug in cipher mode
...
```

Changing any file in history changes all subsequent commit hashes.
This makes the Git history tamper-evident.

GitHub is migrating to SHA-256 due to SHA-1 collision vulnerabilities.

---

## Running the Example

```bash
mvn exec:java -Dexec.mainClass="security.encryption.integrity.IntegrityCheckHash"
```

Observe:
- Hash of original string
- Hash after changing one character — completely different output (avalanche)
- Integrity check passing for unmodified data
- Integrity check failing after tampering

---

<!-- _class: lead -->

## Next: Lecture 5
# Digital Signatures
### Proving identity with asymmetric cryptography
