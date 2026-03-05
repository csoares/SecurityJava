# Attack Demonstrations

Understanding attacks makes you a better defender. These classes show three common vulnerabilities so you can recognise and avoid them in your own code.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.attacks.TimingAttackExample"
mvn exec:java -Dexec.mainClass="security.attacks.WeakRandomnessExample"
mvn exec:java -Dexec.mainClass="security.attacks.RainbowTableExample"
```

---

## TimingAttackExample.java

A **timing attack** extracts secrets by measuring how long an operation takes. `String.equals()` returns false the moment it finds a mismatch — earlier mismatch = faster return = information leak.

### How the Attack Works

```mermaid
sequenceDiagram
    participant A as Attacker
    participant S as Server  (uses String.equals ❌)

    A->>S: token = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"  (wrong at pos 0)
    S->>A: INVALID  (fast — exits at character 0)

    A->>S: token = "a3XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"  (2 chars right)
    S->>A: INVALID  (slightly slower — exits at character 2)

    A->>S: token = "a3f8c1d2e4b5067891abcdef0123456X"  (31 chars right!)
    S->>A: INVALID  (slowest — compares all 31 before failing)

    Note over A: Longer response = more chars matched!<br/>Binary-search character by character<br/>Recover a 32-char token in ~32 × 256 = 8192 requests
```

### Vulnerable vs Secure Comparison

```mermaid
flowchart LR
    subgraph Vulnerable ["❌ String.equals() — Early Exit"]
        V1["Compare char 0: 'a' == 'a' ✅"]
        V2["Compare char 1: '3' == '3' ✅"]
        V3["Compare char 2: 'f' == 'X' ❌  return false immediately"]
        V4["Time ∝ position of first mismatch → leak!"]
        V1 --> V2 --> V3 --> V4
    end

    subgraph Secure ["✅ MessageDigest.isEqual() — Constant Time"]
        S1["Compare ALL bytes with bitwise OR"]
        S2["Never exits early — always full length"]
        S3["Time is identical regardless of mismatch position"]
        S4["No exploitable timing signal"]
        S1 --> S2 --> S3 --> S4
    end

    Vulnerable -.->|"replace with"| Secure
```

### The Rule

```mermaid
flowchart TD
    RULE["Always use MessageDigest.isEqual() for comparing:"]
    R1["Password hashes"]
    R2["HMAC tags"]
    R3["Session tokens, API keys, CSRF tokens"]
    R4["Any secret byte sequence"]
    RULE --> R1 & R2 & R3 & R4
    NEVER["Never use  ==  .equals()  Arrays.equals()  for secret comparison\nEven a remote HTTP response-time difference is measurable"]
```

---

## WeakRandomnessExample.java

Cryptography depends on **unpredictability**. `java.util.Random` is seeded from the clock — if an attacker knows roughly when a token was generated, they can reproduce every value it produced.

### Weak PRNG vs CSPRNG

```mermaid
flowchart TD
    subgraph Weak ["❌ java.util.Random — Predictable"]
        SEED["Seed = System.currentTimeMillis()\n(attacker can guess: ±5 sec = ~10,000 tries)"]
        LCG["Linear Congruential Generator\nnext = (a × current + c) mod m\nFully deterministic formula"]
        OUT["Output: 2423, 9550, 6394, ..."]
        ATKSEED["Attacker tries all seeds in the window"] --> ATKOUT["Reproduces exact sequence 💥"]
        SEED --> LCG --> OUT
    end

    subgraph Strong ["✅ java.security.SecureRandom — Unpredictable"]
        ENTROPY["OS Entropy Pool\n• CPU timing jitter\n• Hardware RNG\n• Network packet timings\n• Disk I/O timing"]
        DRBG["CSPRNG (DRBG)\nCryptographically Secure\nPseudo-Random Number Generator"]
        SOUT["Output: computationally\nindistinguishable from true random"]
        ENTROPY --> DRBG --> SOUT
    end
```

### What Breaks with Weak Randomness

```mermaid
flowchart TD
    subgraph Impact ["💥 Consequences of Using java.util.Random for Security"]
        I1["AES key → attacker recovers key → decrypts all past + future traffic"]
        I2["Session token → attacker forges sessions → account takeover"]
        I3["IV reuse in AES-GCM → nonce reuse attack → key recovered from two messages"]
        I4["RSA key generation → weak primes → key factored in seconds"]
    end
```

### When to Use Which

```mermaid
flowchart LR
    SEC["java.security.SecureRandom\n→ ALL crypto:\nkeys, IVs, salts, nonces\ntokens, CSRF tokens, OTPs"]
    WEAK["java.util.Random\n→ Non-security:\nsimulations, games\nshuffling playlists"]
    MATH["Math.random()\n→ NEVER for security\n(same as Random, worse API)"]
```

---

## RainbowTableExample.java

A **rainbow table** is a precomputed lookup: `hash → password`. An attacker who steals a database of unsalted hashes can crack every recognisable password in seconds. A unique random salt per user destroys this attack.

### Building and Using a Rainbow Table

```mermaid
flowchart TD
    subgraph Build ["🏗️ Attacker builds Rainbow Table  (once, offline)"]
        PW1["'password'"] -->|"SHA-256"| H1["5e884898..."]
        PW2["'123456'"]   -->|"SHA-256"| H2["8d969eef..."]
        PW3["'qwerty'"]   -->|"SHA-256"| H3["65e84be3..."]
        PW4["...billions of entries..."] --> H4["..."]
    end

    subgraph Attack ["⚡ Attack — Unsalted Database"]
        STOLEN["Alice's stored hash: 5e884898da28..."]
        LOOKUP[("Rainbow Table\nlookup")]
        STOLEN --> LOOKUP --> FOUND["💥 'password' — instant!"]
    end
```

### Salt Defeats the Attack

```mermaid
flowchart TD
    subgraph Defense ["🛡️ Salted Hashes — All Three Users Have 'password'"]
        SAME["Three users, same password: 'password'"]
        U1["User 1: salt=5b5ea0...  →  hash=cc5ac0b7..."]
        U2["User 2: salt=a0b2ea...  →  hash=f42c694b..."]
        U3["User 3: salt=437f0e...  →  hash=f58264a7..."]
        SAME --> U1 & U2 & U3
        LOOKUP2[("Rainbow Table\nlookup")]
        U1 & U2 & U3 --> LOOKUP2 --> NOTFOUND["✅ NOT FOUND\nAll three hashes are unique\nPrecomputed table is useless"]
    end
```

### Defence in Depth

```mermaid
flowchart LR
    L1["Plain hash\n❌ Rainbow tables\ncrack instantly"]
    L2["Salt + hash\n⚠️ No rainbow tables\nGPU brute force still fast\n(billions/sec SHA-256)"]
    L3["Salt + PBKDF2 / bcrypt / Argon2\n✅ No rainbow tables\nBrute force impractical\n(thousands/sec at most)"]
    L1 -->|"add salt"| L2 -->|"add slow hash"| L3
```
