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

# Lecture 6
## MAC / HMAC

Message Authentication Codes · Timing Attacks · When to Use What

---

## Message Authentication Code (MAC)

A MAC is a short tag computed from a message and a **shared secret key**.

```
tag = MAC(key, message)
```

- Integrity: any modification to the message changes the tag
- Authentication: only someone with the key can produce a valid tag
- Forgery: an attacker without the key cannot produce a valid tag

Unlike a hash, a MAC requires the key to verify — you cannot forge without it.

---

## HMAC — Hash-based MAC

HMAC wraps a hash function (e.g. SHA-256) with a two-layer construction:

```
HMAC(key, message) =
    SHA-256(
        (key XOR opad) ||
        SHA-256( (key XOR ipad) || message )
    )
```

Where `ipad = 0x36...` and `opad = 0x5C...` (fixed constants).

**Why two layers?** Prevents length-extension attacks that break `SHA-256(key || message)`.

---

## HMAC in Java

```java
Mac mac = Mac.getInstance("HmacSHA256");

// Both parties share this key
SecretKeySpec keySpec = new SecretKeySpec(sharedKeyBytes, "HmacSHA256");
mac.init(keySpec);

// Alice computes tag
byte[] tag = mac.doFinal("Transfer $1000 to account 12345".getBytes());

// Bob verifies — recompute and compare
mac.init(keySpec);
byte[] expected = mac.doFinal(receivedMessage.getBytes());
boolean valid = MessageDigest.isEqual(expected, receivedTag);
```

---

## Attacker Cannot Forge Without the Key

```
Alice sends: message = "Transfer $1000 to account 12345"
             tag     = HMAC(sharedKey, message)

Attacker intercepts and modifies:
             message = "Transfer $9999 to account 99999"

Attacker needs to produce a valid tag for the new message.
Without the shared key: impossible.

Bob computes: expected = HMAC(sharedKey, "Transfer $9999...")
              expected ≠ original_tag  → REJECT
```

---

## Timing Attack on MAC Verification

**Vulnerable code:**

```java
// BAD: exits early on first mismatch
if (computedTag.equals(receivedTag)) { ... }
```

An attacker can measure how long verification takes:
- 0.1 ms → first byte matched
- 0.2 ms → first two bytes matched
- ...

By iterating over all 256 byte values, the attacker recovers the expected tag **byte by byte** without the key.

---

## Constant-Time Comparison

**Safe code:**

```java
// GOOD: always checks all bytes regardless of mismatch position
boolean valid = MessageDigest.isEqual(computedTag, receivedTag);
```

`MessageDigest.isEqual` is implemented to always take the same time:

```java
// Conceptually:
int diff = 0;
for (int i = 0; i < a.length; i++) {
    diff |= a[i] ^ b[i]; // accumulate differences without short-circuiting
}
return diff == 0;
```

**Always use `MessageDigest.isEqual` for security-sensitive comparisons.**

---

## Authentication Spectrum

| Method | Key | Guarantees |
|--------|-----|-----------|
| **Hash (SHA-256)** | None | Integrity only — anyone can forge |
| **HMAC** | Shared symmetric key | Integrity + Authentication (shared identity) |
| **Digital Signature** | Asymmetric key pair | Integrity + Authentication + Non-repudiation |

Use HMAC when both parties share a secret (same system, microservices, APIs).
Use signatures when parties do not share a secret (public software distribution, TLS).

---

## HMAC Applications

| Application | How HMAC Is Used |
|-------------|-----------------|
| **API authentication** | HMAC-SHA256 of request body — AWS Signature V4 |
| **JWT (HS256)** | HMAC signs the token claims |
| **TLS** | HMAC inside MAC-then-Encrypt (pre-TLS 1.3) |
| **Cookie integrity** | HMAC prevents users from forging session cookies |
| **PBKDF2** | Built on HMAC — see Lecture 8 |

---

## HMAC vs Encrypted Data

Common confusion: HMAC is NOT encryption.

| | HMAC | Encryption |
|--|------|-----------|
| Purpose | Authenticity | Confidentiality |
| Key | Shared symmetric | Symmetric or Asymmetric |
| Reversible? | No | Yes |
| Hides content? | No | Yes |

For both confidentiality and integrity: **Encrypt-then-MAC** or use **AEAD (GCM)**.

---

## Running the Example

```bash
mvn exec:java -Dexec.mainClass="security.mac.HMACExample"
```

Observe:
- Shared key generated
- Alice computes HMAC tag for a message
- Attacker modifies the message
- Bob recomputes the tag → mismatch detected
- Demonstration of timing-safe vs unsafe comparison

---

<!-- _class: lead -->

## Next: Lecture 7
# Key Exchange
### Agreeing on a secret over an insecure channel
