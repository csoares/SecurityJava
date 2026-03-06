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

# Lecture 10
## Attacks & Defences

Timing Attacks · Weak Randomness · Rainbow Tables · Secure Coding Rules

---

## Why Learn About Attacks?

> "The best defence is understanding how attacks work."

A developer who does not understand attacks will:
- Use `String.equals()` for MAC comparison (timing attack)
- Use `new Random()` for key generation (predictable keys)
- Use MD5 for password hashing (rainbow tables)

These are not theoretical — they cause real breaches every year.

---

## Attack 1 — Timing Attack

**Premise:** the time taken by code reveals secret information.

```java
// VULNERABLE — exits as soon as a byte mismatches
boolean verify(String submitted, String expected) {
    return submitted.equals(expected);
}
```

If the first byte mismatches: returns in ~1 ns
If first 10 bytes match: returns in ~10 ns

An attacker sends millions of guesses and measures response time.
They can recover the correct value byte-by-byte.

---

## Timing Attack — Byte by Byte

```
Try: AAAAAAAAAAAAAAAA  → returns in 1.0 ns (first byte wrong)
Try: XAAAAAAAAAAAAAAA  → returns in 1.0 ns (first byte wrong)
Try: 5AAAAAAAAAAAAAAA  → returns in 2.1 ns (first byte matched! extra work done)

Now first byte is known = 0x35 ('5')
Repeat for second byte, third byte...
```

With enough samples to average out noise, the full secret is recovered without knowing the algorithm.

**Real-world impact:** ISAKMP (VPN), OAuth tokens, session cookies.

---

## Defence — Constant-Time Comparison

```java
// SAFE — always processes all bytes
boolean verify(byte[] submitted, byte[] expected) {
    return MessageDigest.isEqual(submitted, expected);
}
```

Internally:
```java
int diff = 0;
for (int i = 0; i < a.length; i++) {
    diff |= (a[i] ^ b[i]); // XOR — 0 if equal, non-zero if different
}
return diff == 0; // only true if ALL bytes matched
```

Time is constant regardless of where the mismatch occurs.

---

## Attack 2 — Weak Randomness

**Premise:** predictable "random" values break key generation, IV generation, token generation.

```java
// VULNERABLE — java.util.Random is a Linear Congruential Generator
Random rng = new Random();
byte[] key = new byte[32];
rng.nextBytes(key); // predictable from seed!
```

`java.util.Random` uses a 48-bit seed. If an attacker observes one output value, they can predict all past and future values.

**The entire key is compromised before it is even used.**

---

## Linear Congruential Generator (LCG)

`java.util.Random` implements:

```
state(n+1) = (state(n) × 25214903917 + 11) mod 2^48
```

Given any output, you can:
1. Recover the 48-bit internal state (by brute force: only 2^48 possibilities)
2. Predict all future outputs
3. Recover the AES key, IV, session token, or CSRF token

**2^48 = 281 trillion** — sounds large, but a GPU cracks it in seconds.

---

## Defence — Cryptographically Secure RNG

```java
// SAFE — uses OS entropy (/dev/urandom on Linux, CryptGenRandom on Windows)
SecureRandom rng = new SecureRandom();
byte[] key = new byte[32];
rng.nextBytes(key);
```

`SecureRandom` is:
- Seeded from system entropy (hardware noise, interrupt timing, etc.)
- Unpredictable: knowing past outputs gives no information about future outputs
- Compliant with FIPS 140-2

**Rule:** always use `SecureRandom` for any security-sensitive random values.

---

## When to Use SecureRandom

| Use case | Use |
|----------|-----|
| AES key generation | `SecureRandom` |
| IV / nonce generation | `SecureRandom` |
| CSRF token | `SecureRandom` |
| Password reset token | `SecureRandom` |
| Session ID | `SecureRandom` |
| Game dice roll | `Random` (fine) |
| Shuffle a playlist | `Random` (fine) |
| Statistical simulation | `Random` (fine) |

---

## Attack 3 — Rainbow Tables

**Premise:** precomputed tables map hash values back to passwords.

```
Attacker precomputes offline:
  sha256("password")   → 5e884898...
  sha256("123456")     → 8d969eef...
  sha256("letmein")    → 0d107d09...
  ... (billions of entries, terabytes of data, computed once)

Attacker gets your database (plain SHA-256 passwords):
  Looks up each hash → instant password recovery
```

Free rainbow tables are available online for all common passwords.

---

## Defence Against Rainbow Tables — Salting

```java
// Salt: a random unique value per user
byte[] salt = new byte[16];
new SecureRandom().nextBytes(salt);

// Hash includes the salt
byte[] hash = sha256(concat(salt, password));

// Stored: (userId, salt, hash)
```

The attacker's precomputed table contains `sha256("password")` but not `sha256(salt + "password")`.
They must brute-force each user individually — no table reuse.

**But salting alone is not enough — use PBKDF2/bcrypt/Argon2 too (Lecture 8).**

---

## Summary — Secure Coding Rules

| Rule | Why |
|------|-----|
| Use `SecureRandom` for all keys, IVs, tokens | `Random` is predictable |
| Use `MessageDigest.isEqual` for MAC/hash comparison | Timing attacks |
| Never use MD5 or SHA-1 for security | Collisions found |
| Never use plain SHA-256 for passwords | Rainbow tables, fast GPUs |
| Always use PBKDF2 / bcrypt / Argon2 for passwords | Deliberate slowness |
| Never use ECB mode | Patterns survive |
| Always use AES-GCM | Confidentiality + Integrity |
| Validate ALL inputs | Injection attacks |

---

## Running the Examples

```bash
mvn exec:java -Dexec.mainClass="security.attacks.TimingAttackExample"
mvn exec:java -Dexec.mainClass="security.attacks.WeakRandomnessExample"
mvn exec:java -Dexec.mainClass="security.attacks.RainbowTableExample"
```

Observe:
- **Timing:** measurable time difference between early-exit and constant-time comparison
- **Weak RNG:** `Random` seeds recovered, future values predicted
- **Rainbow:** plain hash looked up instantly; salted hash requires full brute-force

---

## Course Summary

| Topic | Key Takeaway |
|-------|-------------|
| Classical Ciphers | Small key space + patterns = broken |
| Symmetric Encryption | AES-256-GCM is the standard |
| Block Cipher Modes | ECB: never. GCM: always. |
| Hashing | SHA-256 for integrity; never for passwords alone |
| Digital Signatures | RSA/ECDSA — integrity + identity + non-repudiation |
| HMAC | Shared-key authentication; constant-time compare |
| Key Exchange | ECDHE for forward secrecy |
| Passwords | PBKDF2 / bcrypt / Argon2 with unique salts |
| PKI | Certificate chains establish public key trust |
| Attacks | Know them to defend against them |

---

<!-- _class: lead -->

# Thank You

All examples are runnable at:
**github.com/csoares/SecurityJava**

```bash
git clone https://github.com/csoares/SecurityJava
mvn exec:java -Dexec.mainClass="security.attacks.TimingAttackExample"
```
