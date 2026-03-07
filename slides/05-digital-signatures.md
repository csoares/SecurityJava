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

# Lecture 5
## Digital Signatures
### Wax seals — anyone can verify, only you can create

---

## The Wax Seal Analogy

In medieval times, important letters were sealed with a wax seal stamped with the sender's ring:

```
┌────────────────────────────────────┐
│   Letter from the King             │
│                                    │
│   "Send 100 soldiers to..."        │
│                                    │
│             [🔴 Royal Seal]        │
└────────────────────────────────────┘
```

- Anyone can **verify** the seal is real (they know what the king's seal looks like)
- Only the king can **create** the seal (he has the ring)
- If the letter is tampered with, the seal breaks

Digital signatures work exactly the same way.

---

## Public Key Pair — Two Keys, One Purpose

Digital signatures use **asymmetric cryptography** — two mathematically linked keys:

```
┌──────────────────────┐    ┌──────────────────────┐
│   PRIVATE KEY        │    │   PUBLIC KEY          │
│   (keep secret!)     │    │   (share with world)  │
│                      │    │                       │
│   Used to SIGN       │    │   Used to VERIFY      │
│   Only Alice has it  │    │   Anyone can have it  │
└──────────────────────┘    └──────────────────────┘
         ↑ mathematically linked — you cannot derive
           the private key from the public key
```

Alice publishes her public key. She keeps her private key secret.

---

## Signing a Message — Step by Step

```
Step 1: Hash the message
  message = "Transfer £1000 to account 12345"
  hash = SHA256(message) = "b94d27..."   (32 bytes)

Step 2: Encrypt the hash with private key
  signature = RSA_encrypt(hash, alice_private_key)
              = "3F9A..." (256 bytes for 2048-bit RSA)

Step 3: Send message + signature together
  Alice sends: message + signature
```

The signature is unique to:
- This message (different message → different hash → different signature)
- Alice's private key (only she could have produced it)

---

## Verifying a Signature — Step by Step

```
Bob receives: message + signature

Step 1: Decrypt signature with Alice's PUBLIC key
  extracted_hash = RSA_decrypt(signature, alice_public_key)
  = "b94d27..."

Step 2: Hash the received message independently
  recomputed_hash = SHA256(message)
  = "b94d27..."

Step 3: Compare
  extracted_hash == recomputed_hash?
    YES → ✅ signature is valid (Alice signed it, message unmodified)
    NO  → ❌ invalid (message tampered OR wrong signer)
```

---

## Digital Signatures in Java

```java
// Generate key pair (Alice does this once)
KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
kpg.initialize(2048);
KeyPair pair = kpg.generateKeyPair();

// SIGN (Alice)
Signature sig = Signature.getInstance("SHA256withRSA");
sig.initSign(pair.getPrivate());
sig.update("Transfer £1000 to account 12345".getBytes());
byte[] signature = sig.sign();

// VERIFY (Bob — only needs Alice's public key)
sig.initVerify(pair.getPublic());
sig.update(message.getBytes());
boolean valid = sig.verify(signature); // true = authentic ✓
```

---

## What a Valid Signature Proves

```
✅ INTEGRITY     The message has not been modified since signing.
                 Even one changed character → signature fails.

✅ AUTHENTICITY  The message was signed by the holder of the private key.
                 Nobody else could have produced this signature.

✅ NON-REPUDIATION  Alice cannot later deny having signed it.
                    The signature is mathematical proof she did.

❌ DOES NOT PROVE Alice's real-world identity.
                  Who is "Alice"? We need PKI for that (Lecture 9).
```

---

## Tamper Detection Demo

```java
// What happens if Mallory changes one byte?
byte[] message = "Transfer £1000 to account 12345".getBytes();
message[10] = 'X';  // tamper!

sig.initVerify(pair.getPublic());
sig.update(message);
boolean valid = sig.verify(signature);
// → false ✅  Tampering detected!
```

Even changing a **single bit** of the message produces a completely different hash, which doesn't match the hash embedded in the signature.

---

## Signing Files

Software companies sign their downloads so users can verify authenticity:

```
Publisher:
  hash = SHA256(installer.exe)          // hash the file
  signature = RSA_sign(hash, privKey)   // sign the hash
  publish: installer.exe + installer.sig

User:
  hash_of_download = SHA256(installer.exe)
  claimed_hash = RSA_decrypt(installer.sig, pubKey)
  hash_of_download == claimed_hash?
    YES → ✅ genuine software from the publisher
    NO  → ❌ file corrupted or tampered — do not install!
```

---

## Real-World Uses

| Where | What is signed |
|-------|---------------|
| **App stores** | Every app is signed by the developer |
| **Software updates** | OS verifies signature before installing |
| **TLS certificates** | CA signs the server's public key |
| **Git commits** | Developers sign commits with GPG |
| **JWT tokens** | Server signs the claims, clients verify |
| **PDF documents** | Legal documents carry embedded signatures |
| **Emails (S/MIME)** | Signed emails prove sender identity |

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.encryption.integrity.IntegrityCheckSignature"
mvn exec:java -Dexec.mainClass="security.encryption.integrity.FileDigitalSignature"
```

**What to observe:**
- Key pair generation (private + public)
- Signing produces a byte array (the signature)
- Verification: passes on original message
- Verification: fails after modifying even one character
- File signing: a `.sig` file created on disk

---

<!-- _class: lead -->

## Next: Lecture 6
# MAC / HMAC
### Secret handshakes for shared secrets
