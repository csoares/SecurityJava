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

# Lecture 10
## Attacks & Defences
### Think like an attacker — defend like an expert

---

## Why Learn Attacks?

> "To defeat your enemy, you must understand how they think."

The three mistakes we'll cover today have caused:
- Hundreds of millions of stolen passwords
- Billions of dollars in fraud
- Real-world consequences for real people

And all three are **trivially easy to avoid** once you know about them.

---

## Attack 1 — Timing Attack

**Core idea:** the time your code takes reveals secret information.

Real scenario:
```
User submits a password reset token.
Server checks: if (submittedToken.equals(storedToken))

The .equals() method stops as soon as it finds a mismatch.
```

An attacker sends millions of token guesses and measures response time:

```
Try "AAAA..." → 1.00 ms (first char wrong — exits immediately)
Try "XAAA..." → 1.00 ms (first char wrong)
Try "5AAA..." → 1.05 ms (first char matched! more work done)
               → first character is "5"
Repeat for each position...
```

---

## Timing Attack — Visualised

```
Correct token:  5 F 3 A 9 B ...

Try: AAAAAAA  → fail at position 1 (fast)
Try: 5AAAAAA  → fail at position 2 (a bit slower)
Try: 5FAAAAA  → fail at position 3 (a bit more slower)
Try: 5F3AAAA  → fail at position 4 (even slower)
...

With enough measurements to average out network noise,
the attacker recovers the full token character by character.

32-char token: instead of 62^32 guesses,
               only 62 × 32 = 1,984 guesses!
```

A 32-character token reduced to under 2000 guesses.

---

## Timing Attack — The Fix

```java
// VULNERABLE:
if (userToken.equals(storedToken))      // stops early!
if (Arrays.equals(a, b))                // stops early!

// SAFE:
if (MessageDigest.isEqual(a, b))        // always checks ALL bytes
```

**How it works:**
```java
// Conceptual constant-time comparison:
int diff = 0;
for (int i = 0; i < expected.length; i++) {
    diff |= submitted[i] ^ expected[i];   // accumulate all differences
}
return diff == 0;  // only true if ALL bytes matched
```

Time is always the same. Timing measurement reveals nothing.

---

## Attack 2 — Weak Randomness

**Core idea:** predictable "random" numbers break everything that depends on them.

```java
// DANGEROUS:
Random rng = new Random();  // java.util.Random
byte[] sessionToken = new byte[32];
rng.nextBytes(sessionToken);  // predictable!
```

`java.util.Random` uses a 48-bit internal state.

```
state(n+1) = (state(n) × 25214903917 + 11) mod 2^48

If attacker observes even ONE output:
  They can recover the 48-bit state by brute force (2^48 = fast)
  They can predict ALL future tokens
  Every session token, CSRF token, API key is compromised
```

---

## Weak Randomness — Real Attack

```
Step 1: Attacker creates an account on the site.
Step 2: They receive their session token.
Step 3: From the token, they recover the RNG seed
        (brute force: try all 2^48 ≈ 281 trillion seeds,
         fast on a GPU in seconds).
Step 4: They predict the next session token generated.
Step 5: That token belongs to the next user who logged in.
Step 6: Attacker uses that token to log in as that user.
```

This is **not theoretical**. It has been found in production web apps.

---

## Weak Randomness — The Fix

```java
// WRONG:
Random rng = new Random();

// RIGHT:
SecureRandom rng = new SecureRandom();
```

`SecureRandom` uses the operating system's entropy pool:
- Hardware noise (CPU timing jitter, disk access timing)
- Mouse movement, keyboard timing
- Network packet arrival times

```
Entropy gathered: truly unpredictable physical events
Internal state:   256+ bits (not 48!)
Prediction:       computationally infeasible
```

**Always use `SecureRandom` for keys, tokens, IVs, salts.**

---

## When to Use Which Random

```
┌──────────────────────────────────────────────────────┐
│  Security-sensitive → SecureRandom                   │
│    ✓ AES key                                         │
│    ✓ Session token / CSRF token                      │
│    ✓ Password reset link                             │
│    ✓ IV / salt                                       │
│    ✓ API key generation                              │
├──────────────────────────────────────────────────────┤
│  Not security-sensitive → Random (fine)              │
│    ✓ Game dice roll                                  │
│    ✓ Shuffle a playlist                              │
│    ✓ Pick a random test item                         │
│    ✓ Statistical simulation                          │
└──────────────────────────────────────────────────────┘
```

---

## Attack 3 — Rainbow Tables

**Core idea:** precompute hash → password mappings and store them.

```
Attacker spends time (or buys a table):
  sha256("password") → 5e884898...
  sha256("123456")   → 8d969eef...
  sha256("letmein")  → 0d107d09...
  ... (100 billion entries, 5 TB of storage)

Then gets your password database (plain SHA-256 hashes):
  User: alice  hash: 5e884898...  → lookup → "password" ✓
  User: bob    hash: 8d969eef...  → lookup → "123456"   ✓
  User: carol  hash: 0d107d09...  → lookup → "letmein"  ✓

All three cracked in milliseconds.
```

---

## Rainbow Table Defence — Salting

```
Without salt:
  sha256("password") = 5e884898...  ← in every rainbow table

With unique salt per user:
  salt_alice = "7f3a91b2..."  (random 16 bytes)
  sha256("7f3a91b2..." + "password") = a3c7e9f1...  ← NOT in any table

  salt_bob   = "2c8d45e1..."  (different random bytes)
  sha256("2c8d45e1..." + "password") = 8f2b4c7d...  ← also NOT in table
```

The precomputed table is now **useless**.

Attacker must brute-force each user individually — and with PBKDF2, that takes centuries.

---

## Summary — The Secure Coding Checklist

```
□ Use SecureRandom for ALL security-sensitive values
□ Use MessageDigest.isEqual() for ALL security comparisons
□ Use AES-256-GCM (not ECB, not CBC without MAC)
□ Use SHA-256 for general hashing (not MD5, not SHA-1)
□ Use PBKDF2 / bcrypt / Argon2 for passwords (not plain hash)
□ Use unique random salt per user for passwords
□ Use ECDHE for key exchange (forward secrecy)
□ Use HMAC to authenticate messages (not plain hash)
□ Never hardcode keys in source code
□ Never log passwords or sensitive data
```

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.attacks.TimingAttackExample"
mvn exec:java -Dexec.mainClass="security.attacks.WeakRandomnessExample"
mvn exec:java -Dexec.mainClass="security.attacks.RainbowTableExample"
```

**Timing:** observe measurable time difference between safe and unsafe comparison.
**Weak RNG:** watch the seed being recovered and future tokens predicted.
**Rainbow:** plain SHA-256 cracked instantly; salted + PBKDF2 is safe.

---

<!-- _class: lead -->

# Course Complete!

**You now know:**
Ciphers · AES · Modes · Hashing · Signatures · HMAC · Key Exchange · Passwords · PKI · Attacks

**All examples:**
```bash
git clone https://github.com/csoares/SecurityJava
```

> Security is not about making systems unbreakable.
> It's about making them too expensive to break.
