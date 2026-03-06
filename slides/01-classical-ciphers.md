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

# Lecture 1
## Classical Ciphers

Caesar · Vigenère · Frequency Analysis

---

## Why Study Broken Ciphers?

- Introduce vocabulary: key, shift, substitution
- Show **why** security fails — small key space, patterns in output
- Attacks on classical ciphers are the same attacks used today — just faster

> "Those who cannot remember the past are condemned to repeat it."

---

## Caesar Cipher

Each letter is shifted by a fixed amount. Used by Julius Caesar ~50 BCE.

```
Plaintext:   H  E  L  L  O
Shifts:      +3 +3 +3 +3 +3
Ciphertext:  K  H  O  O  R
```

```java
char encrypt(char c, int shift) {
    return (char) ('A' + (c - 'A' + shift) % 26);
}
```

**Key space:** only 25 possible keys — try them all in milliseconds.

---

## Caesar — Brute Force Attack

Because there are only 25 possible keys, an attacker tries all of them:

```
Shift  1: Jgnnq Yqtnf   ← clearly wrong
Shift  2: Ifmmp Xpsme   ← clearly wrong
Shift  3: Hello World   ← found it!
Shift  4: Gdkkn Vnqkc   ← clearly wrong
...
```

**No key is safe** when the key space is this small.
A computer breaks any Caesar cipher in microseconds.

---

## Frequency Analysis

Even without the key, English letter frequencies expose the shift:

| English | Frequency |
|---------|-----------|
| E | 12.7% |
| T | 9.1% |
| A | 8.2% |

If the most common ciphertext letter is `H`, then `H - E = 3` → shift is 3.

**No computation needed** — just statistics.

---

## Vigenère Cipher

Uses a repeating keyword. Different Caesar shift per position.

```
Plaintext:   H  E  L  L  O  W  O  R  L  D
Keyword:     K  E  Y  K  E  Y  K  E  Y  K
Shifts:      10  4 24 10  4 24 10  4 24 10
Ciphertext:  R  I  J  V  S  U  Y  V  J  N
```

**Key space:** 26^N for keyword of length N — astronomically larger.
Considered unbreakable for ~300 years.

---

## Kasiski Examination — Breaking Vigenère

Babbage cracked it in 1854.

**Key insight:** if the same plaintext fragment aligns with the same keyword fragment, it produces the same ciphertext.

```
Ciphertext: ...XYZ....XYZ...
Positions:     5    20
Distance:  20 - 5 = 15
Factors of 15: 1, 3, 5, 15  → keyword length is probably 3 or 5
```

Split ciphertext into N Caesar streams → frequency-analyse each one independently.

---

## Index of Coincidence

Measures how "English-like" a text is:

| Text type | IC value |
|-----------|----------|
| Random | ~0.038 |
| English plaintext | ~0.067 |
| Vigenère ciphertext | 0.038 – 0.067 |

IC closer to 0.067 → shorter key length.
Iterate key lengths until IC of each stream reaches 0.067.

---

## Lessons from Classical Ciphers

| Lesson | Modern consequence |
|--------|-------------------|
| Small key space → brute force | AES uses 128+ bit keys (2^128 possibilities) |
| Patterns preserved → frequency analysis | Modern ciphers appear random (pseudorandom permutation) |
| Substitution alone is not enough | Need confusion **and** diffusion (Shannon 1949) |

**Shannon's two principles still guide every modern cipher.**

---

## Shannon's Principles (1949)

**Confusion** — each ciphertext bit depends on many key bits in a complex way.
→ Prevents recovering the key from ciphertext.

**Diffusion** — changing one plaintext bit changes ~half the ciphertext bits.
→ Prevents statistical analysis (avalanche effect).

Caesar has neither. AES has both.

---

## Running the Examples

```bash
mvn exec:java -Dexec.mainClass="security.encryption.classic.CaesarCipher"
mvn exec:java -Dexec.mainClass="security.encryption.classic.VigenereCipher"
```

Observe:
- Caesar: try all 25 shifts, see the plaintext appear
- Vigenère: keyword repeats, same plaintext + same key position → same output

---

<!-- _class: lead -->

## Next: Lecture 2
# Symmetric Encryption
### AES — the global standard
