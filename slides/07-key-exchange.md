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

# Lecture 7
## Key Exchange

Diffie-Hellman · ECDH · Forward Secrecy · ECC

---

## The Key Distribution Problem

Symmetric encryption (AES) requires a shared key.
But how do Alice and Bob agree on a key if Eve is watching every message?

**They cannot simply send the key — Eve would intercept it.**

This was considered unsolvable for thousands of years.

In 1976, Diffie and Hellman published an elegant mathematical solution.

---

## Diffie-Hellman — The Colour Analogy

```
Public information: Yellow (everyone knows this)

Alice:                          Bob:
Secret colour: Red              Secret colour: Blue
Mix: Yellow + Red = Orange      Mix: Yellow + Blue = Teal

Exchange Orange and Teal publicly (Eve sees them)

Alice: Teal + Red = Brown       Bob: Orange + Blue = Brown
```

Both arrive at **Brown** (the shared secret) without ever sending it.
Eve sees Yellow, Orange, Teal — but cannot compute Brown.

---

## Diffie-Hellman — The Mathematics

Public parameters: prime `p`, generator `g`

```
Alice picks secret: a
Computes:           A = g^a mod p   ← sends this publicly

Bob picks secret:   b
Computes:           B = g^b mod p   ← sends this publicly

Alice:  shared = B^a mod p = g^(ab) mod p
Bob:    shared = A^b mod p = g^(ab) mod p

Eve knows: g, p, A, B
She needs: a (solve g^a ≡ A mod p)  — the Discrete Logarithm Problem
At 2048 bits: no efficient algorithm exists
```

---

## Diffie-Hellman in Java

```java
// Both agree on parameters
DHParameterSpec dhParams = /* NIST group, 2048-bit prime */;

// Alice
KeyPairGenerator aliceKpg = KeyPairGenerator.getInstance("DH");
aliceKpg.initialize(dhParams);
KeyPair aliceKp = aliceKpg.generateKeyPair();
// Alice sends aliceKp.getPublic() to Bob

// Bob
KeyPairGenerator bobKpg = KeyPairGenerator.getInstance("DH");
bobKpg.initialize(dhParams);
KeyPair bobKp = bobKpg.generateKeyPair();
// Bob sends bobKp.getPublic() to Alice

// Both compute shared secret
KeyAgreement ka = KeyAgreement.getInstance("DH");
ka.init(aliceKp.getPrivate());
ka.doPhase(bobKp.getPublic(), true);
byte[] sharedSecret = ka.generateSecret();
```

---

## DH Weakness — No Authentication

DH alone is vulnerable to a Man-in-the-Middle attack:

```
Alice ──── sends A ────> Eve ──── sends E1 ────> Bob
Alice <─── gets E2 ──── Eve <──── gets B ─────── Bob

Eve establishes:
  One shared secret with Alice (using E2, A)
  One shared secret with Bob   (using B, E1)

Alice and Bob think they're talking to each other — they're not!
```

**Fix:** authenticate the DH public keys using digital signatures → TLS certificates.

---

## Elliptic Curve Diffie-Hellman (ECDH)

Same idea as DH, but uses elliptic curve point multiplication instead of modular exponentiation.

```
Classic DH: A = g^a mod p
ECDH:       A = a × G   (point multiplication on a curve)
```

| Security Level | Classic DH | ECDH |
|---------------|------------|------|
| 112-bit | 2048-bit key | 224-bit key |
| 128-bit | 3072-bit key | 256-bit key |
| 256-bit | 15360-bit key | 521-bit key |

**8× smaller keys for the same security.** Used in TLS 1.3, Signal, WhatsApp.

---

## ECDH in Java

```java
// Use NIST P-256 curve (secp256r1)
KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
kpg.initialize(new ECGenParameterSpec("secp256r1"));

KeyPair aliceKp = kpg.generateKeyPair();
KeyPair bobKp   = kpg.generateKeyPair();

// Alice computes shared secret
KeyAgreement ka = KeyAgreement.getInstance("ECDH");
ka.init(aliceKp.getPrivate());
ka.doPhase(bobKp.getPublic(), true);
byte[] sharedSecret = ka.generateSecret();
// → derive AES key from this using HKDF
```

---

## Deriving Keys from the Shared Secret

The raw DH/ECDH output is not directly suitable as an AES key.
Use a **Key Derivation Function (KDF)** to derive proper key material:

```java
// Simple approach: SHA-256 the shared secret
MessageDigest md = MessageDigest.getInstance("SHA-256");
byte[] aesKey = md.digest(sharedSecret);

// Better: HKDF (RFC 5869)
// HKDF-Extract → HKDF-Expand → key material
```

HKDF separates extraction (make a uniform random value) from expansion (derive multiple keys).

---

## Forward Secrecy — Ephemeral Key Exchange

**Static DH:** both parties have long-term key pairs. If private key leaks, all past sessions can be decrypted from captured traffic.

**Ephemeral ECDH (ECDHE):** fresh key pair for every session.

```
Session 1: Alice generates K1 (ephemeral) → computes shared secret → deletes K1
Session 2: Alice generates K2 (ephemeral) → computes shared secret → deletes K2
```

If the server's long-term private key is later compromised, past sessions are still protected — the ephemeral keys are gone.

**TLS 1.3 mandates ECDHE — forward secrecy is not optional.**

---

## Running the Examples

```bash
mvn exec:java -Dexec.mainClass="security.keyexchange.DiffieHellmanExample"
mvn exec:java -Dexec.mainClass="security.keyexchange.ECDHExample"
```

Observe:
- Both parties independently compute the same shared secret
- The secret is never transmitted
- ECDH keys are much smaller than classic DH keys
- Key derivation step: raw secret → AES key

---

<!-- _class: lead -->

## Next: Lecture 8
# Password Security
### Why plain hashing is catastrophically wrong
