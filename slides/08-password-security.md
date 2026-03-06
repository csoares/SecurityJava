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

# Lecture 8
## Password Security

Plain Hashing · Salting · PBKDF2 · bcrypt · Argon2

---

## The Fundamental Rule

> **Passwords must never be stored as plaintext or as plain hashes.**

Every year, millions of user passwords are stolen from breached databases.
If stored correctly, a breach leaks nothing usable.
If stored incorrectly, attackers recover every password within hours.

This lecture shows exactly why, step by step.

---

## Stage 1 — Plain SHA-256 (NEVER DO THIS)

```java
// BAD
String stored = sha256(password);
```

Problem: the same password always produces the same hash.

```
sha256("password") = 5e884898da28047151d0e56f8dc62927...
sha256("password") = 5e884898da28047151d0e56f8dc62927...  ← identical!
```

**Rainbow tables:** precomputed tables of billions of passwords and their SHA-256 hashes. An attacker looks up the hash and immediately gets the password.

**GPU speed:** a modern GPU computes ~10 billion SHA-256 hashes/second.

---

## Rainbow Tables — Precomputed Attack

```
Table entry: "password"  →  5e884898da28...
Table entry: "123456"    →  8d969eef6ecad3...
Table entry: "abc123"    →  6367c48dd193d5...
...billions more...
```

When the attacker gets your database:
1. Take each stored hash
2. Look it up in the table → instant password recovery

**O(1) lookup — no computation needed.**

Free rainbow tables cover all passwords up to 8-10 characters.

---

## Stage 2 — SHA-256 + Salt (Better, Not Enough)

```java
// Better but still weak
byte[] salt = new byte[16];
new SecureRandom().nextBytes(salt);
String stored = sha256(salt + password);
// Store: (salt, hash) per user
```

**Salt defeats rainbow tables:** the precomputed table is useless because it does not include the salt in the input. Attackers must now brute-force each user individually.

**But the attacker still has a GPU:** 10 billion SHA-256 attempts/second.
An 8-character password takes seconds to crack by brute force.

---

## GPU Cracking Speed

```
Password space for 8 chars (a-z, A-Z, 0-9): 62^8 ≈ 218 trillion
SHA-256 speed:  10,000,000,000 /sec
Time to crack:  218,000,000,000,000 / 10,000,000,000 ≈ 6 hours
```

SHA-256 is **designed to be fast** for data integrity.
For passwords, we need the opposite: deliberately **slow** hashing.

---

## Stage 3 — PBKDF2 (Recommended)

```java
// GOOD
PBEKeySpec spec = new PBEKeySpec(
    password.toCharArray(),
    salt,
    310_000,          // iterations (OWASP 2024 recommendation)
    256               // output bits
);
SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
byte[] hash = skf.generateSecret(spec).getEncoded();
```

310,000 iterations = 310,000 HMAC operations per guess.
**GPU speed drops from 10 billion/sec to ~10,000/sec.**
The same 6-hour crack now takes **centuries**.

---

## What to Store

```java
// Store all of these per user (none of this is secret except the hash)
record StoredCredential(
    String userId,
    byte[] salt,       // random 16 bytes, unique per user
    int    iterations, // 310_000
    byte[] hash        // PBKDF2 output
) {}
```

**Never store the password itself.**
**Never store the password in logs or error messages.**

When the user logs in: recompute PBKDF2 with the stored salt and compare.

---

## Login Verification

```java
boolean verify(String attemptedPassword, StoredCredential stored) {
    PBEKeySpec spec = new PBEKeySpec(
        attemptedPassword.toCharArray(),
        stored.salt(),
        stored.iterations(),
        256
    );
    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] candidate = skf.generateSecret(spec).getEncoded();

    // Constant-time comparison — prevent timing attacks
    return MessageDigest.isEqual(candidate, stored.hash());
}
```

---

## Algorithm Comparison

| Algorithm | GPU speed | Memory | Notes |
|-----------|-----------|--------|-------|
| SHA-256 (plain) | ~10 B/sec | Tiny | **Never use for passwords** |
| PBKDF2 310k | ~10 K/sec | Tiny | Good — Java built-in, FIPS |
| bcrypt cost=12 | ~5 K/sec | Tiny | Widely adopted, max 72 bytes |
| **Argon2id** | ~500/sec | 64 MB | **Best — OWASP 2024 first choice** |

Argon2id requires large memory per guess, making GPU/ASIC attacks impractical.
Use it if you can add a library dependency.

---

## Upgrading Hash Algorithms

When you switch from PBKDF2 to Argon2:

```
DO NOT invalidate existing hashes — users would lose access.

Instead:
1. Add a "hash_version" column to the database
2. On successful login (you have the plaintext password):
   - Re-hash with Argon2
   - Update the database entry
3. Over time, all active users are migrated
4. Accounts that never log in retain the old hash
```

This is the industry-standard approach for live migrations.

---

## Running the Example

```bash
mvn exec:java -Dexec.mainClass="security.passwords.PasswordHashingExample"
```

Observe:
- Stage 1: same password → same hash every time
- Stage 2: salt added → unique hash per user, rainbow tables defeated
- Stage 3: PBKDF2 → iteration count makes each guess slow
- Login simulation: correct password verified, wrong password rejected

---

<!-- _class: lead -->

## Next: Lecture 9
# PKI & Certificates
### How trust is established on the internet
