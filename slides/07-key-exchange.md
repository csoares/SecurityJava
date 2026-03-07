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

# Lecture 7
## Key Exchange
### Agree on a secret in public — without revealing it

---

## The Problem

AES is secure — but both parties need the **same key**.

If Alice emails the key to Bob, Eve reads it.
If Alice posts the key, everyone knows it.
If they meet in person... that doesn't scale.

**For 2,000 years this was considered impossible.**

In 1976, Whitfield Diffie and Martin Hellman published a solution
that changed everything.

---

## The Paint Mixing Analogy

This is the most intuitive explanation of key exchange:

```
Both agree on a public colour:  YELLOW (everyone knows this)

Alice picks a secret:  RED        Bob picks a secret:  BLUE

Alice mixes: YELLOW + RED = ORANGE    Bob mixes: YELLOW + BLUE = TEAL
         (sends ORANGE publicly)              (sends TEAL publicly)

Alice: TEAL + RED = BROWN         Bob: ORANGE + BLUE = BROWN
```

Both arrive at **BROWN** — the shared secret.

Eve sees YELLOW, ORANGE, TEAL.
To get BROWN she'd need to "un-mix" paint — mathematically infeasible.

---

## The Mathematics — Discrete Logarithm

```
Public parameters: prime p, generator g  (everyone knows these)

Alice:                          Bob:
  picks secret: a                 picks secret: b
  computes: A = g^a mod p         computes: B = g^b mod p
  publishes A                     publishes B

Alice:                          Bob:
  shared = B^a mod p              shared = A^b mod p
         = g^(ba) mod p                  = g^(ab) mod p
         = g^(ab) mod p  ← SAME VALUE ✓

Eve sees: g, p, A = g^a mod p, B = g^b mod p
Eve needs: a  →  solve  g^a ≡ A  (mod p)
= Discrete Logarithm Problem — no efficient algorithm at 2048 bits
```

---

## Diffie-Hellman in Java

```java
// Step 1: Generate Alice's key pair
KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
kpg.initialize(2048); // 2048-bit prime
KeyPair alice = kpg.generateKeyPair();

// Step 2: Generate Bob's key pair (same parameters)
KeyPairGenerator kpg2 = KeyPairGenerator.getInstance("DH");
kpg2.initialize(((DHPublicKey) alice.getPublic()).getParams());
KeyPair bob = kpg2.generateKeyPair();

// Step 3: Both compute the shared secret independently
KeyAgreement ka = KeyAgreement.getInstance("DH");
ka.init(alice.getPrivate());
ka.doPhase(bob.getPublic(), true);
byte[] sharedSecret = ka.generateSecret(); // same on both sides ✓
```

---

## ⚠️ DH Has No Authentication

DH tells us two parties agreed on a secret.
It does **not** tell us WHO those parties are.

```
Alice ─── sends A ──► Mallory ─── sends M1 ──► Bob
Alice ◄── gets M2 ─── Mallory ◄── gets B ───── Bob

Mallory has:
  Secret with Alice: she thinks she's talking to Bob
  Secret with Bob:   he thinks he's talking to Alice

Mallory decrypts both sides, reads everything, re-encrypts.
Neither Alice nor Bob suspects anything.
```

This is a **Man-in-the-Middle attack**.

---

## Fix: Authenticate the Keys

TLS solves this by having the server **sign** its DH public key:

```
Bob sends:  B (DH public key)
            + Signature(B, bob_private_key)
            + bob_certificate (links public key to "Bob")

Alice verifies:
  1. Certificate is valid (signed by a CA she trusts → Lecture 9)
  2. Signature on B is valid (really Bob's key, not Mallory's)
```

DH + Authentication = **secure key exchange**.

---

## ECDH — Same Idea, Better Maths

Elliptic Curve Diffie-Hellman — uses curves instead of modular arithmetic:

```
Classic DH:   A = g^a mod p           (modular exponentiation)
ECDH:         A = a × G               (point multiplication on a curve)
```

**Why bother?**

| Security level | Classic DH key | ECDH key |
|---------------|----------------|----------|
| 112-bit | 2048 bits | 224 bits |
| 128-bit | 3072 bits | **256 bits** |
| 256-bit | 15360 bits | 521 bits |

**8× smaller keys, faster operations, same security.**
TLS 1.3 uses ECDH by default (curve P-256 or X25519).

---

## ECDH in Java

```java
// Use NIST P-256 curve (secp256r1) — 256-bit key, 128-bit security
KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
kpg.initialize(new ECGenParameterSpec("secp256r1"));

KeyPair alice = kpg.generateKeyPair();
KeyPair bob   = kpg.generateKeyPair();

// Compute shared secret
KeyAgreement ka = KeyAgreement.getInstance("ECDH");
ka.init(alice.getPrivate());
ka.doPhase(bob.getPublic(), true);
byte[] sharedSecret = ka.generateSecret();
// → derive AES-256 key from this shared secret using SHA-256
```

---

## Forward Secrecy — Why It Matters

**Without forward secrecy (static keys):**
```
Eve records all encrypted traffic for years.
In 2035, she hacks the server and gets the private key.
She decrypts all recorded traffic from 2025 onwards. 🔓
```

**With ephemeral ECDH (ECDHE):**
```
Each session: new ECDH key pair generated
              shared secret used for session
              key pair deleted after handshake

Eve records traffic.
In 2035, she gets the private key.
She cannot decrypt old sessions — the ephemeral keys are gone. ✓
```

**TLS 1.3 mandates ECDHE. Forward secrecy is not optional.**

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.keyexchange.DiffieHellmanExample"
mvn exec:java -Dexec.mainClass="security.keyexchange.ECDHExample"
```

**What to observe:**
- Alice and Bob independently compute **the same** shared secret
- The secret is never sent over the network
- ECDH keys are much smaller than classic DH keys
- Key derivation: raw shared secret → AES key via SHA-256

---

<!-- _class: lead -->

## Next: Lecture 8
# Password Security
### The #1 cause of data breaches
