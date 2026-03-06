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

# Lecture 5
## Digital Signatures

RSA · SHA256withRSA · Non-repudiation · File Signing

---

## The Problem Hashing Alone Cannot Solve

A plain hash proves data integrity, but not identity.

An attacker can:
1. Intercept the data
2. Modify it
3. Recompute the hash
4. Send the modified data + new hash

The receiver cannot tell the difference.

**We need a way to tie the hash to a specific identity — digital signatures.**

---

## Digital Signature Concept

Uses asymmetric key pair: private key (kept secret) and public key (shared).

```
Signer:                          Verifier:
  private_key (secret)             public_key (anyone can have this)

  sign(data, private_key)          verify(data, signature, public_key)
       ↓                                  ↓
  signature                         true / false
```

- **Only** the holder of the private key can produce a valid signature
- **Anyone** with the public key can verify it
- Changing even one byte of data invalidates the signature

---

## What SHA256withRSA Does

Two steps under the hood:

```
Signing:
  data → SHA-256 → hash (32 bytes)
  hash → RSA encrypt with PRIVATE key → signature (256 bytes for 2048-bit RSA)

Verification:
  signature → RSA decrypt with PUBLIC key → original hash
  data → SHA-256 → recomputed hash
  original hash == recomputed hash?  → valid / invalid
```

Why hash first? RSA can only encrypt small data (~245 bytes for 2048-bit key).
Hashing reduces any-size input to a fixed 32 bytes.

---

## Digital Signatures in Java

```java
// Generate key pair (do once, store securely)
KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
kpg.initialize(2048);
KeyPair pair = kpg.generateKeyPair();

// Sign
Signature sig = Signature.getInstance("SHA256withRSA");
sig.initSign(pair.getPrivate());
sig.update(data.getBytes());
byte[] signature = sig.sign();

// Verify
sig.initVerify(pair.getPublic());
sig.update(data.getBytes());
boolean valid = sig.verify(signature); // true if untampered
```

---

## What a Valid Signature Proves

| Guarantee | Explanation |
|-----------|-------------|
| **Integrity** | Data has not been modified since signing |
| **Authenticity** | Signed by the holder of the private key |
| **Non-repudiation** | Signer cannot later deny having signed it |

**What it does NOT prove:**

The private key holder's real-world identity. For that, you need a Certificate Authority to bind the public key to an identity → Lecture 9 (PKI).

---

## Tamper Detection

```java
// Simulate tampering
byte[] tampered = data.getBytes();
tampered[0] = (byte)(tampered[0] ^ 0xFF); // flip bits in first byte

sig.initVerify(pair.getPublic());
sig.update(tampered);
boolean valid = sig.verify(signature); // → FALSE
```

Any modification to the data — even a single bit — produces a completely different hash, which does not match the hash embedded in the signature.

---

## Signing Files

For file signing, compute the file's SHA-256 hash, then sign that:

```java
// Compute file hash
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] fileHash = md.digest(Files.readAllBytes(filePath));

// Sign the hash
Signature sig = Signature.getInstance("SHA256withRSA");
sig.initSign(privateKey);
sig.update(fileHash);
byte[] signature = sig.sign();

// Store signature alongside file
Files.write(sigPath, signature);
```

The `.sig` file is distributed with the software. Users verify before installing.

---

## Real-World Use Cases

| Use Case | How Signatures Are Used |
|----------|------------------------|
| **Software updates** | Vendor signs the binary; OS verifies before installing |
| **Code signing** | Apple/Google sign apps distributed via their stores |
| **TLS certificates** | CA signs server's public key |
| **Git commits** | Developer signs commits with GPG |
| **PDF documents** | Signatures prove document authenticity in legal contexts |
| **JWT tokens** | Server signs claims; clients verify |

---

## RSA vs ECDSA

| | RSA | ECDSA |
|--|-----|-------|
| Security at 128-bit | 3072-bit key | 256-bit key |
| Signature size | 384 bytes | 64 bytes |
| Signing speed | Slower | Faster |
| Standard | Older, widely supported | Modern, TLS 1.3 preferred |

ECDSA is covered in Lecture 7 (ECC).
For new systems, prefer **ECDSA with P-256**.

---

## Running the Examples

```bash
mvn exec:java -Dexec.mainClass="security.encryption.integrity.IntegrityCheckSignature"
mvn exec:java -Dexec.mainClass="security.encryption.integrity.FileDigitalSignature"
```

Observe:
- Key pair generation
- Signing produces a byte array (the signature)
- Verification passes on original data
- Verification fails after any modification
- File signing: `.sig` file created and verified

---

<!-- _class: lead -->

## Next: Lecture 6
# MAC / HMAC
### Authentication with a shared secret key
