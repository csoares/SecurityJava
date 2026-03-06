---
marp: true
theme: default
paginate: true
style: |
  section {
    font-size: 1.4rem;
  }
  section.lead h1 {
    font-size: 2.8rem;
  }
  code {
    font-size: 1.1rem;
  }
---

<!-- _class: lead -->

# Applied Cryptography & Security

### A hands-on course with Java examples

---

## Course Overview

10 lectures covering the full spectrum of modern cryptography and security

| # | Topic |
|---|-------|
| 1 | Classical Ciphers |
| 2 | Symmetric Encryption |
| 3 | Block Cipher Modes |
| 4 | Hashing & Integrity |
| 5 | Digital Signatures |
| 6 | MAC / HMAC |
| 7 | Key Exchange (DH & ECDH) |
| 8 | Password Security |
| 9 | PKI & Certificates |
| 10 | Attacks & Defences |

---

## Why Study Cryptography?

- Every HTTPS connection uses it
- Every password system depends on it
- Every software update is verified by it
- Every online payment is secured by it

**You cannot build secure systems without understanding the primitives underneath them.**

---

## The CIA Triad

```
+----------------------+
|   CONFIDENTIALITY    |  Only authorised parties can read the data
+----------------------+
|      INTEGRITY       |  Data has not been modified in transit
+----------------------+
|    AVAILABILITY      |  Systems remain accessible to authorised users
+----------------------+
```

Cryptography primarily addresses **Confidentiality** and **Integrity**.

---

## Core Vocabulary

| Term | Meaning |
|------|---------|
| **Plaintext** | Original readable data |
| **Ciphertext** | Encrypted, unreadable data |
| **Key** | Secret value controlling the cipher |
| **Algorithm** | Mathematical procedure (e.g. AES, RSA) |
| **Encrypt** | Plaintext + Key → Ciphertext |
| **Decrypt** | Ciphertext + Key → Plaintext |

---

## Two Fundamental Problems

**Problem 1 — Confidentiality**
Alice wants to send a message to Bob that Eve cannot read.
→ Solution: **Encryption**

**Problem 2 — Integrity / Authentication**
Bob wants to verify the message truly came from Alice, unmodified.
→ Solution: **Digital Signatures / HMAC**

Both problems must be solved together in real systems.

---

## Symmetric vs Asymmetric

| | Symmetric | Asymmetric |
|--|-----------|------------|
| Keys | One shared secret | Public key + Private key |
| Speed | Very fast | Slow (10-1000x) |
| Key distribution | Hard (how to share?) | Easy (publish public key) |
| Examples | AES | RSA, ECDSA |
| Typical use | Bulk data encryption | Key exchange, signatures |

**In practice: use asymmetric to exchange a symmetric key, then encrypt with AES.**

---

## Running the Examples

All code in this course is runnable Java:

```bash
# Clone the repo
git clone https://github.com/csoares/SecurityJava

# Run any example
mvn exec:java -Dexec.mainClass="security.encryption.classic.CaesarCipher"
mvn exec:java -Dexec.mainClass="security.encryption.symmetric.SymmetricEncryptionExample"
mvn exec:java -Dexec.mainClass="security.attacks.TimingAttackExample"
```

No external libraries needed for most examples — Java's built-in `javax.crypto` covers the fundamentals.

---

<!-- _class: lead -->

## Next: Lecture 1
# Classical Ciphers
### Where it all began
