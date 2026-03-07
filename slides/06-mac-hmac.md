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

# Lecture 6
## MAC / HMAC
### Secret handshakes — prove you know the secret

---

## The Problem with Plain Hashes

Hash(message) proves the message is intact. But:

```
Mallory intercepts message + hash

Mallory modifies: message → message2
Mallory computes: hash2 = SHA256(message2)

Mallory sends: message2 + hash2

Bob checks: SHA256(message2) == hash2 ✓  ← FOOLED!
```

**Anyone can recompute a hash.** A hash proves nothing about WHO sent the message.

We need a hash that requires a **secret key** to produce.

---

## The Secret Handshake Analogy

Imagine two spies who agreed on a secret phrase before the mission:

```
Alice                              Bob
  knows: "swordfish"               knows: "swordfish"

  greet = mix(message, "swordfish")
  send: message + greet

                          verify = mix(message, "swordfish")
                          greet == verify? → authentic ✓
```

An outsider without "swordfish" **cannot** produce a valid greet.
This is exactly what HMAC does — with a cryptographic key instead of a phrase.

---

## HMAC — Hash-based Message Authentication Code

HMAC uses a shared key to produce a tag:

```
tag = HMAC(key, message)
```

```
SEND:
  Alice computes: tag = HMAC-SHA256(sharedKey, message)
  Alice sends: message + tag

VERIFY:
  Bob computes: expected = HMAC-SHA256(sharedKey, message)
  Bob checks: expected == received_tag?
    YES → ✅ message is authentic and unmodified
    NO  → ❌ reject — tampered or wrong sender
```

Without the `sharedKey`, Mallory cannot produce a valid tag. End of story.

---

## HMAC Construction — Why Not Just Hash(key + message)?

Why not just do `SHA256(key + message)`?

```
Vulnerable to length-extension attack:
  attacker knows: SHA256(key + message)
  attacker can compute: SHA256(key + message + extra)
  without knowing the key!
```

HMAC uses a two-layer construction that defeats this:

```
HMAC(key, message) =
    SHA256( (key XOR opad) ||
            SHA256( (key XOR ipad) || message ) )
```

Two nested hashes prevent the length-extension attack.

---

## HMAC in Java

```java
// Both Alice and Bob share this key (agreed in advance)
byte[] sharedKeyBytes = new byte[32];
new SecureRandom().nextBytes(sharedKeyBytes);
SecretKeySpec keySpec = new SecretKeySpec(sharedKeyBytes, "HmacSHA256");

Mac mac = Mac.getInstance("HmacSHA256");
mac.init(keySpec);

// Alice computes the tag
byte[] tag = mac.doFinal("Transfer £1000 to account 12345".getBytes());

// Bob verifies by recomputing
mac.init(keySpec);
byte[] expected = mac.doFinal(receivedMessage.getBytes());
boolean valid = MessageDigest.isEqual(expected, receivedTag);
```

---

## ⚠️ The Timing Attack

Using `.equals()` for comparison is **dangerous**:

```java
// DANGEROUS:
if (Arrays.equals(computed, received)) { ... }
// or
if (computed.equals(received)) { ... }
```

Why? These return `false` as soon as they find the first mismatch.

```
tag = [0x3F, 0xA1, 0x9B, ...]
Try:  [0x00, ...]  → fails after 1 byte check (very fast)
Try:  [0x3F, 0x00, ...]  → fails after 2 byte checks (slightly slower)
Try:  [0x3F, 0xA1, 0x00, ...]  → fails after 3 byte checks (even slower)
```

An attacker measures response time to recover the tag byte by byte.

---

## Constant-Time Comparison

```java
// SAFE — always checks ALL bytes, no early exit:
boolean valid = MessageDigest.isEqual(computed, received);
```

Internally:
```
diff = 0
for each byte i:
    diff = diff OR (computed[i] XOR received[i])
    ← accumulate differences without stopping early
return diff == 0
```

Time is **constant** regardless of where the mismatch is.
The attacker's timing measurement reveals nothing.

> **Rule:** always use `MessageDigest.isEqual()` for any security comparison.

---

## HMAC vs Digital Signatures — When to Use Which

```
HMAC                          Digital Signature
──────────────────────────    ─────────────────────────
✓ Both parties share a key    ✓ No shared key needed
✓ Very fast                   ✓ Public verification
✓ Simple                      ✓ Non-repudiation
✗ Requires key exchange       ✗ Slower
✗ No non-repudiation          ✗ More complex

Use HMAC for:                 Use signatures for:
• API authentication          • Software distribution
• Session cookies             • TLS certificates
• Microservice calls          • Legal documents
• Webhook verification        • Public key systems
```

---

## Real-World HMAC Examples

**AWS API Authentication (Signature V4):**
```
signature = HMAC-SHA256(
    HMAC-SHA256(
        HMAC-SHA256(
            HMAC-SHA256("AWS4" + secret, date),
            region),
        service),
    "aws4_request")
```

**Cookie integrity:**
```
cookie = username + ":" + HMAC(secret, username + expires)
server: recompute HMAC → check if cookie was tampered with
```

**Webhook verification (GitHub, Stripe, etc.):**
```
X-Hub-Signature-256: sha256=HMAC(secret, request_body)
```

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.mac.HMACExample"
```

**What to observe:**
- Shared key generated
- HMAC tag computed for original message
- Mallory modifies message → tag verification fails
- Timing comparison: measure `.equals()` vs `MessageDigest.isEqual()`

---

<!-- _class: lead -->

## Next: Lecture 7
# Key Exchange
### How to agree on a secret when everyone is watching
