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

# Lecture 1
## Classical Ciphers
### Caesar · Vigenère · Why they failed

---

## The Story

**50 BCE.** Julius Caesar needs to send military orders across the Roman Empire.
Messengers can be captured. Letters can be intercepted.

His solution: shift every letter in the message by 3 positions.

```
A → D     B → E     C → F   ...   Z → C
```

Only his generals knew the shift. Everyone else saw gibberish.

> This is the world's most famous encryption system — and it is trivially broken.

---

## Caesar Cipher — How It Works

Shift each letter by a fixed amount (key = 3):

```
Plaintext:    H  E  L  L  O     W  O  R  L  D
Shift +3:     +3 +3 +3 +3 +3    +3 +3 +3 +3 +3
Ciphertext:   K  H  O  O  R     Z  R  U  O  G
```

To decrypt, shift back by 3:

```
Ciphertext:   K  H  O  O  R
Shift -3:     -3 -3 -3 -3 -3
Plaintext:    H  E  L  L  O  ✓
```

**The key is just a single number (1–25). That's the entire secret.**

---

## Caesar in Pseudocode

```
function encrypt(letter, shift):
    position = letter's position in alphabet  (A=0, B=1, ..., Z=25)
    new_position = (position + shift) mod 26
    return letter at new_position

function decrypt(letter, shift):
    same, but subtract shift instead of add
```

Example: `encrypt('H', 3)`
- H is position 7
- (7 + 3) mod 26 = 10
- Position 10 = 'K' ✓

---

## Breaking Caesar — Brute Force

There are only **25 possible keys**. Just try them all:

```
Try shift 1:  Jgnnq Yqtnf   ← doesn't look like English
Try shift 2:  Ifmmp Xpsme   ← doesn't look like English
Try shift 3:  Hello World   ← found it! ✓
Try shift 4:  Gdkkn Vnqkc   ← doesn't look like English
...
```

A computer can try all 25 shifts in **microseconds**.

> **Key space too small = cipher broken**
> This is the first lesson of cryptography.

---

## Breaking Caesar — Frequency Analysis

Even smarter: you don't even need to try all shifts.

English letters appear with predictable frequency:

```
Most common in English:  E (12.7%)  T (9.1%)  A (8.2%)
```

If you see the ciphertext letter `H` appears 12.7% of the time:

```
H is probably E.
H − E = 7 − 4 = 3
Shift is probably 3.
```

**No trial and error needed. Just statistics.**

---

## Vigenère Cipher — A Better Idea

Instead of one fixed shift, use a **keyword** that repeats:

```
Plaintext:   H  E  L  L  O  W  O  R  L  D
Keyword:     K  E  Y  K  E  Y  K  E  Y  K   ← "KEY" repeating
Shifts:      10  4 24 10  4 24 10  4 24 10
Ciphertext:  R  I  J  V  S  U  Y  V  J  N
```

Now each letter uses a **different shift**:
- Frequency analysis no longer works directly
- Two identical letters can encrypt to different ciphertext letters

Considered unbreakable for **300 years**.

---

## The Key Insight

Why does frequency analysis fail on Vigenère?

```
Plaintext:   E  E  E  E  E  E   (same letter, 6 times)
Keyword:     K  E  Y  K  E  Y
Shifts:      10  4 24 10  4 24
Ciphertext:  O  I  C  O  I  C   (only 2 distinct, not all same)
```

Each 'E' encrypts differently depending on its **position** relative to the keyword.

Pattern is hidden — but not perfectly. The pattern repeats every keyword-length characters.

---

## Breaking Vigenère — Kasiski Test (1854)

If the same word appears at two positions whose distance is a multiple of the keyword length, they encrypt identically.

```
Ciphertext: ...THE...fourteen letters...THE...
                ^                       ^
           position 5              position 20
           distance = 15
           factors of 15: 1, 3, 5, 15
           → keyword length is probably 3 or 5
```

Once you know the keyword length, split into streams — each stream is just a Caesar cipher. Frequency-analyse each one.

---

## Classical Ciphers — Lessons Learned

```
┌─────────────────────────────────────────────────────┐
│  LESSON 1: Key space must be astronomically large   │
│            25 keys → broken instantly               │
│            26^N keys → needs long random keys       │
├─────────────────────────────────────────────────────┤
│  LESSON 2: Patterns in output = weakness            │
│            Same input → same output → frequency     │
│            analysis works                           │
├─────────────────────────────────────────────────────┤
│  LESSON 3: Security must not rely on               │
│            keeping the ALGORITHM secret             │
│            Only the KEY should be secret            │
└─────────────────────────────────────────────────────┘
```

Lesson 3 is **Kerckhoffs's principle** — still the foundation of modern crypto.

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.encryption.classic.CaesarCipher"
mvn exec:java -Dexec.mainClass="security.encryption.classic.VigenereCipher"
```

**Experiment:**
- Encrypt "HELLO" with shift 3 → should get "KHOOR"
- Encrypt "HELLO" twice with same Vigenère key → same ciphertext
- Encrypt "HELLOHELLO" — see the repeating pattern

---

<!-- _class: lead -->

## Next: Lecture 2
# Symmetric Encryption
### AES — used in every HTTPS connection on Earth
