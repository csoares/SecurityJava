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
  table { font-size: 1.2rem; }
---

<!-- _class: lead -->

# Security for Dummies
## Applied Cryptography — a hands-on course

---

## Why Does This Matter?

Real breaches. Real consequences.

| Year | Company | What happened | Users affected |
|------|---------|--------------|----------------|
| 2012 | LinkedIn | Passwords stored as plain SHA-1 | 117 million |
| 2013 | Adobe | Passwords encrypted (not hashed!) | 153 million |
| 2016 | Yahoo | MD5 passwords, no salt | 500 million |
| 2019 | Facebook | Passwords stored in plain text | 600 million |

> Every single one of these was avoidable with basic cryptography knowledge.

---

## The Big Question

**Alice wants to send a secret message to Bob.**
Eve is watching everything.

```
Alice ──────── internet ──────── Bob
                  |
                 Eve
              (watching)
```

How can Alice and Bob communicate privately?
How does Bob know the message really came from Alice?
How does Bob know nobody changed the message?

**This course answers all three questions.**

---

## Three Things We Need to Protect

Think of it like a bank vault:

```
┌─────────────────────────────────────────────┐
│  CONFIDENTIALITY  — only Alice & Bob can    │
│                     read the message        │
├─────────────────────────────────────────────┤
│  INTEGRITY        — nobody changed the      │
│                     message in transit      │
├─────────────────────────────────────────────┤
│  AUTHENTICITY     — Bob knows for sure      │
│                     Alice sent it           │
└─────────────────────────────────────────────┘
```

These are the three pillars of security. Every tool we learn serves one (or more) of these.

---

## The Main Characters

We'll use these names throughout the course:

| Character | Role |
|-----------|------|
| **Alice** | Sender — wants to send a secure message |
| **Bob** | Receiver — wants to verify and read it |
| **Eve** | Eavesdropper — watches all traffic passively |
| **Mallory** | Active attacker — intercepts and modifies messages |
| **Trent** | Trusted third party — a certificate authority |

These names are used in every cryptography textbook. Get used to them!

---

## The Vocabulary You Need

Plain English first:

| Fancy word | What it means |
|-----------|--------------|
| **Plaintext** | The original message: "Meet at 3pm" |
| **Ciphertext** | The scrambled version: "Xhzq bq 7ux" |
| **Key** | The secret that controls the scrambling |
| **Encrypt** | Scramble: plaintext + key → ciphertext |
| **Decrypt** | Unscramble: ciphertext + key → plaintext |
| **Algorithm** | The recipe used to scramble (e.g. AES) |

---

## Two Families of Cryptography

**Symmetric** — one shared key, like a house key:

```
Alice                    Bob
  🔑 key               🔑 same key
  encrypt(msg, key) ──► decrypt(cipher, key)
```

**Asymmetric** — a padlock and key pair:

```
Alice                    Bob
  🔓 Bob's padlock      🔑 Bob's private key
  lock(msg, padlock) ──► unlock(cipher, private_key)
```

Bob publishes his padlock (public key). Anyone can lock a box.
Only Bob's private key can open it.

---

## The Course Map

```
┌──────────────────────────────────────────────────────┐
│  Lecture 1  Classical Ciphers   (where it all began) │
│  Lecture 2  Symmetric AES       (the workhorse)      │
│  Lecture 3  Block Cipher Modes  (how to use AES)     │
│  Lecture 4  Hashing             (fingerprints)       │
│  Lecture 5  Digital Signatures  (wax seals)          │
│  Lecture 6  HMAC                (secret handshakes)  │
│  Lecture 7  Key Exchange        (paint mixing)       │
│  Lecture 8  Passwords           (the #1 failure)     │
│  Lecture 9  PKI & Certificates  (digital passports)  │
│  Lecture 10 Attacks             (think like a hacker)│
└──────────────────────────────────────────────────────┘
```

---

## Running the Examples

Every concept has working Java code you can run:

```bash
git clone https://github.com/csoares/SecurityJava
cd SecurityJava

# Example:
mvn exec:java -Dexec.mainClass="security.attacks.TimingAttackExample"
```

No theory without practice. Every slide has a matching Java file.

---

<!-- _class: lead -->

## Let's start!
# Lecture 1
## Classical Ciphers
### How Julius Caesar sent secret messages
