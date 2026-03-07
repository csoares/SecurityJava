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

# Lecture 4
## Hashing & Integrity
### Digital fingerprints — one way only

---

## The Fingerprint Analogy

A hash function is like taking a fingerprint:

```
Person ──fingerprint──► 🖊️ unique pattern
```

- You can go from person → fingerprint (easy)
- You **cannot** go from fingerprint → reconstruct the person (impossible)
- Two different people have different fingerprints (almost certainly)
- The fingerprint is always the same size regardless of the person

A hash function works the same way — but for data.

---

## What a Hash Does

Takes **any** input, produces a **fixed-size** output (the "digest"):

```
SHA-256("Hello, World!") → dffd6021bb2bd5b0af676290809ec3a5  (32 bytes)
SHA-256("Hello, World?") → a0dc65ffca799873f86f8d5e7a26a34f  (32 bytes, totally different!)
SHA-256(a 1 GB movie)   → some 32-byte value                  (still 32 bytes!)
SHA-256("")              → e3b0c44298fc1c149afbf4c8996fb924  (32 bytes)
```

Properties:
- **Fast** to compute
- **Fixed** output size (SHA-256 always = 32 bytes)
- **One-way** — cannot reverse it
- **Deterministic** — same input always gives same output

---

## The Avalanche Effect

Change one character — the entire hash changes:

```
SHA-256("Hello, this is a sample text!")
→  b94d27b9934d3e08a52e52d7da7dabfac484efe0

SHA-256("Hello, this is a sample TEXT!")
→  9f86d081884c7d659a2feaa0c55ad015a3bf4f1b
   ↑ completely different!

SHA-256("Hello, this is a sample text?")
→  6367c48dd193d56ea7b0baad25b19455e529f5e
   ↑ completely different!
```

There is **no relationship** between small input changes and output changes.
This prevents attackers from guessing what the original input was.

---

## Using Hashes for Integrity

```
Alice sends a large file to Bob:

ALICE:
  file = "contract.pdf"
  hash = SHA256(file) = "b94d27b9..."
  sends: file + hash

MALLORY intercepts, changes "£1000" to "£9000" in the file

BOB receives:
  hash2 = SHA256(received_file) = "7f3a91c..." ← different!
  hash2 ≠ hash → 🚨 FILE WAS TAMPERED! REJECT.
```

Used everywhere: software downloads, Git commits, TLS records.

---

## Hashing in Java

```java
// Compute SHA-256 hash
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hash = digest.digest("Hello, World!".getBytes(StandardCharsets.UTF_8));

// Convert to hex string for display
StringBuilder hex = new StringBuilder();
for (byte b : hash) {
    hex.append(String.format("%02x", b));
}
System.out.println(hex.toString());
// → "dffd6021bb2bd5b0af676290809ec3a5..."
```

Alternatively in many frameworks:
```java
String hash = DigestUtils.sha256Hex(data); // Apache Commons Codec
```

---

## What Hashing Does NOT Solve

A plain hash does **not** prove identity:

```
Mallory intercepts Alice's file + hash.

Mallory modifies the file:
  new_file = modified version
  new_hash = SHA256(new_file)  ← easy to recompute!

Mallory sends: new_file + new_hash

Bob checks: SHA256(new_file) == new_hash ✓  ← passes! Bob is fooled.
```

**The hash must be delivered separately and securely.**
Otherwise use HMAC (Lecture 6) or Digital Signatures (Lecture 5).

---

## Hash Algorithms — Which to Use

```
MD5         ❌ Broken — collisions found in 1996
SHA-1       ❌ Broken — Google collision in 2017 (SHAttered attack)
SHA-256     ✅ Safe — use this for general purpose
SHA-3-256   ✅ Safe — different mathematical construction
BLAKE2      ✅ Safe — faster than SHA-256, used in many tools
```

> Real example: The SHAttered attack created two different PDF files with the **exact same SHA-1 hash**. Cost: about $100,000 in cloud computing.

SHA-256 has no known collision after 25+ years.

---

## Fun Fact: Git Uses Hashes

Every Git commit is identified by a SHA-1 hash:

```bash
git log --oneline
a3f2bc1 Add encryption example     ← "a3f2bc1" is SHA-1 of commit content
7d4e8f2 Fix bug in cipher mode
```

If anyone modifies a file in history → all subsequent commit hashes change.
The history is tamper-evident — you'd immediately notice.

(GitHub is migrating to SHA-256 due to SHA-1 vulnerabilities)

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.encryption.integrity.IntegrityCheckHash"
```

**What to observe:**
- Hash of a string — 64 hex chars always
- Change one letter → completely different hash (avalanche effect)
- Integrity check passes with original data
- Integrity check fails after any modification to the data

---

<!-- _class: lead -->

## Next: Lecture 5
# Digital Signatures
### Wax seals for the digital age
