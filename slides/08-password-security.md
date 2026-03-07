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

# Lecture 8
## Password Security
### From catastrophic to correct — step by step

---

## This Happens Every Year

```
2012: LinkedIn hacked
  Passwords stored as plain SHA-1 (no salt).
  117 million passwords cracked within days.
  Sold on dark web for $5.

2019: Facebook internal logs
  Hundreds of millions of passwords stored in plaintext.
  Accessible to ~20,000 employees.

2024: RockYou2024 collection released
  10 billion plaintext passwords compiled
  from decades of breaches.
```

> Every single one of these was preventable.

---

## The Golden Rule

> **Passwords must NEVER be stored as plaintext or as plain hashes.**

The goal: if your database is stolen, the attacker learns nothing useful.

We'll go through three approaches:
1. ❌ Plain SHA-256 — catastrophically wrong
2. ⚠️ SHA-256 + salt — better, still not enough
3. ✅ PBKDF2 / bcrypt / Argon2 — correct

---

## Approach 1: Plain SHA-256 ❌

```java
// WRONG — never do this
String stored = sha256(password);
```

**Problem 1: Rainbow Tables**

Attackers precompute SHA-256 for billions of common passwords:
```
sha256("password")  → 5e884898da28...   ← in the table
sha256("123456")    → 8d969eef6ecad...  ← in the table
sha256("letmein")   → 0d107d09f5bbe...  ← in the table
```

Get the database → look up each hash → instantly get passwords.

**Problem 2: Same password → same hash always**
If two users have the same password, the attacker knows immediately.

---

## How Fast Is SHA-256?

```
Modern GPU: ~10,000,000,000 SHA-256 per second
                              (10 billion per second)

8-character password (a-z, A-Z, 0-9):
  62^8 = 218,340,105,584,896 possibilities

Time to brute force: 218 trillion / 10 billion = ~6 hours
```

SHA-256 is **designed to be fast** — great for data integrity, terrible for passwords.

For passwords, we need the **opposite**: deliberately **slow** hashing.

---

## Approach 2: SHA-256 + Salt ⚠️

A **salt** is a random value added to each password before hashing:

```java
byte[] salt = new byte[16];
new SecureRandom().nextBytes(salt);  // unique per user
String stored = sha256(salt + password);
// store: (userId, salt, hash)
```

```
sha256("secret_salt_A" + "password") → a3f2...  ← not in any rainbow table
sha256("secret_salt_B" + "password") → 9c7b...  ← different! even same password
```

**Rainbow tables: defeated ✅** — the precomputed table doesn't include the salt.

**But GPUs still run at 10 billion SHA-256 per second ❌**
Salting alone doesn't slow the attacker down.

---

## Approach 3: PBKDF2 ✅

PBKDF2 is designed to be **intentionally slow**:

```
PBKDF2(password, salt, iterations=310000, keyLength=256)
= HMAC(HMAC(HMAC(... HMAC(password, salt) ...)))
       ← 310,000 times! →
```

Each password guess requires 310,000 HMAC operations:

```
GPU at 10 billion SHA-256/sec
→ GPU at ~10,000 PBKDF2/sec  (1 million× slower)

8-character brute force: 6 hours → 2,500 years
```

The legitimate user waits ~100ms to log in. The attacker waits centuries.

---

## PBKDF2 in Java

```java
// On registration:
byte[] salt = new byte[16];
new SecureRandom().nextBytes(salt);  // unique per user

PBEKeySpec spec = new PBEKeySpec(
    password.toCharArray(),
    salt,
    310_000,   // iterations (OWASP 2024 recommendation)
    256        // output bits
);
SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
byte[] hash = skf.generateSecret(spec).getEncoded();

// Store in database: (userId, salt, hash, iterations=310000)
// NEVER store the password itself
```

---

## Login Verification

```java
boolean verify(char[] attempt, byte[] storedSalt, byte[] storedHash) {
    PBEKeySpec spec = new PBEKeySpec(
        attempt,
        storedSalt,
        310_000,
        256
    );
    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] candidate = skf.generateSecret(spec).getEncoded();

    // IMPORTANT: constant-time comparison (prevent timing attacks)
    return MessageDigest.isEqual(candidate, storedHash);
}
```

If user types wrong password → hashes don't match → reject.
Password itself is **never** stored anywhere.

---

## Cracking Speed Comparison

```
Algorithm               GPU speed       8-char brute force
─────────────────────────────────────────────────────────
SHA-256 (plain)         10 B/sec        6 hours      ❌
SHA-256 + salt          10 B/sec        6 hours      ❌  (no rainbow, still fast)
PBKDF2 310k             10 K/sec        2,500 years  ✅
bcrypt cost=12           5 K/sec        5,000 years  ✅
Argon2id (64 MB)          500/sec       50,000 years ✅✅
```

**Argon2id** wins because it also requires large memory — GPU parallelism is defeated.
OWASP recommends Argon2id as first choice in 2024.

---

## Algorithm Recommendation

```
┌─────────────────────────────────────────────────────────┐
│  If you can add a dependency:                           │
│    USE Argon2id (first choice — memory-hard)            │
│    USE bcrypt (second choice — widely supported)        │
│                                                         │
│  If you need Java built-in only (e.g., FIPS required):  │
│    USE PBKDF2WithHmacSHA256 with ≥310,000 iterations   │
│                                                         │
│  NEVER use:                                             │
│    ✗ Plain SHA-256 / SHA-1 / MD5 for passwords          │
│    ✗ Symmetric encryption for passwords (need reversal?)│
│    ✗ Plaintext                                          │
└─────────────────────────────────────────────────────────┘
```

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.passwords.PasswordHashingExample"
```

**What to observe:**
- Stage 1: same password → same hash every time
- Stage 2: add a salt → different hash each run, rainbow tables defeated
- Stage 3: PBKDF2 → noticeable delay (100ms) — that's intentional!
- Login simulation: correct password accepted, wrong password rejected
- Try the timing: PBKDF2 takes ~100ms, plain SHA-256 takes microseconds

---

<!-- _class: lead -->

## Next: Lecture 9
# PKI & Certificates
### How does your browser know it's really talking to your bank?
