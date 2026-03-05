# Password Security

Passwords must never be stored as plain text or plain hashes. This package shows the evolution from completely insecure to production-ready password storage, and demonstrates why each step is necessary.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.passwords.PasswordHashingExample"
```

---

## PasswordHashingExample.java

### Stage Progression — Insecure to Secure

```mermaid
flowchart TD
    subgraph Stage1 ["❌ Stage 1: Plain SHA-256  (NEVER do this)"]
        P1["'password'"] -->|"SHA-256"| H1["5e884898da28..."]
        H1 --> RT[("Rainbow Table<br/>billions of precomputed entries")]
        RT -->|"O(1) instant lookup"| CRACK1["💥 CRACKED"]
        NOTE1["Same password → same hash always<br/>Two users with 'password' exposed at once<br/>GPU: billions of SHA-256/sec"]
    end

    subgraph Stage2 ["⚠️ Stage 2: SHA-256 + Salt  (better, still fast)"]
        SALT2["Random Salt<br/>(16 bytes, unique per user)"] --> MIX["SHA-256(salt ‖ password)"]
        P2["'password'"] --> MIX --> H2["unique hash per user"]
        NOTE2["Rainbow tables: defeated ✅<br/>GPU brute force: still billions/sec ❌<br/>SHA-256 is designed to be fast — bad for passwords"]
    end

    subgraph Stage3 ["✅ Stage 3: PBKDF2  (recommended)"]
        SALT3["Unique Salt"] --> SLOW["310,000 rounds<br/>of HMAC-SHA256"]
        P3["'password'"] --> SLOW --> H3["derived key<br/>(256 bits)"]
        NOTE3["Each guess costs 310,000 HMAC operations<br/>GPU drops from billions/sec to thousands/sec<br/>Factor of ~10^6 harder to crack"]
    end

    Stage1 --> Stage2 --> Stage3
```

### Why SHA-256 Is Wrong for Passwords

```mermaid
flowchart TD
    subgraph Rates ["⚡ Cracking Speed on 1 GPU"]
        R1["SHA-256: ~10 billion/sec ❌"]
        R2["PBKDF2 310k: ~10,000/sec ⚠️"]
        R3["bcrypt cost=12: ~5,000/sec ✅"]
        R4["Argon2id: ~500/sec ✅✅ (memory-hard)"]
        R1 --- R2 --- R3 --- R4
    end
```

### Login Verification Flow

```mermaid
sequenceDiagram
    participant User
    participant Server

    Note over Server: Database stores: { userId, salt, iterations, hash }<br/>Password is NEVER stored

    User->>Server: REGISTER with password "MySecret123"
    Server->>Server: salt = SecureRandom (16 bytes)
    Server->>Server: hash = PBKDF2(password, salt, 310000)
    Server->>Server: Store (salt, 310000, hash) — discard password

    User->>Server: LOGIN attempt "MySecret123"
    Server->>Server: Load stored (salt, iterations, hash) for this user
    Server->>Server: candidate = PBKDF2(attempt, salt, iterations)
    Server->>Server: MessageDigest.isEqual(candidate, storedHash)

    alt Hashes match
        Server->>User: ✅ Login successful
    else No match
        Server->>User: ❌ Login failed
    end
```

### Algorithm Comparison

```mermaid
flowchart TD
    subgraph Algorithms ["Algorithm Comparison"]
        A1["PBKDF2WithHmacSHA256<br/>✅ Java built-in (no extra library)<br/>✅ FIPS-compliant<br/>⚠️ Not memory-hard — GPU-friendly"]
        A2["bcrypt<br/>✅ Widely adopted, battle-tested<br/>✅ Automatic work factor<br/>⚠️ Limited to 72-byte passwords"]
        A3["Argon2id<br/>✅ Memory-hard — GPU/ASIC resistant<br/>✅ 2024 OWASP first choice<br/>✅ Configurable memory + time cost"]
        A1 --- A2 --- A3
    end
```
