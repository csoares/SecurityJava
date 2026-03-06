# Classical Ciphers

Pre-computer encryption based on letter substitution. Broken by modern standards, but essential for understanding the vocabulary of cryptography: plaintext, ciphertext, key, and attacks.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.encryption.classic.CaesarCipher"
mvn exec:java -Dexec.mainClass="security.encryption.classic.VigenereCipher"
```

---

## CaesarCipher.java

Each letter is shifted by a fixed number of positions in the alphabet. Named after Julius Caesar (~50 BCE). Only 25 possible keys — trivially broken.

### Encrypt / Decrypt

```mermaid
flowchart LR
    subgraph Encrypt ["🔒 Encryption  (key = shift 3)"]
        PL["Plaintext: 'H E L L O'"]
        OP["+3 to each letter's position"]
        CT["Ciphertext: 'K H O O R'"]
        PL --> OP --> CT
    end

    subgraph Decrypt ["🔓 Decryption  (same key, reversed)"]
        CT2["Ciphertext: 'K H O O R'"]
        OP2["-3 from each letter's position"]
        PL2["Plaintext: 'H E L L O'"]
        CT2 --> OP2 --> PL2
    end

    CT -.->|"decrypt"| CT2
```

### Alphabet Shift

```mermaid
flowchart LR
    subgraph Alphabet ["Alphabet shift — shift = 3, wraps around at Z"]
        A1["A→D"] --- B1["B→E"] --- C1["C→F"] --- D1["..."] --- W1["W→Z"] --- X1["X→A"] --- Y1["Y→B"] --- Z1["Z→C"]
    end
```

### Why It Fails — Attacks

```mermaid
flowchart TD
    subgraph BruteForce ["⚠️ Brute Force Attack — only 25 keys to try"]
        BF1["Shift  1: Jgnnq Yqtnf ...  ❌"]
        BF2["Shift  2: Ifmmp Xpsme ...  ❌"]
        BF3["Shift  3: Hello World ...  ✅  Found it!"]
        BF4["Shift  4: Gdkkn Vnqkc ...  ❌"]
        BF5["...24 more tries at most"]
        BF1 --> BF2 --> BF3 --> BF4 --> BF5
    end

    subgraph FreqAnalysis ["📊 Frequency Analysis (no key needed)"]
        FA1["English:    E ≈ 13%,  T ≈ 9%,  A ≈ 8%"]
        FA2["Ciphertext: H ≈ 13%,  W ≈ 9%,  D ≈ 8%"]
        FA3["H − E = 3  →  shift is probably 3!"]
        FA1 --> FA2 --> FA3
    end

    BF5 -.->|"or use"| FA1
```

---

## VigenereCipher.java

Uses a repeating keyword instead of one fixed shift — applying a different Caesar shift per letter. Considered unbreakable for 300 years, until Babbage cracked it in 1854.

### How the Keyword Works

```mermaid
flowchart TD
    subgraph KeyRepeat ["🔑 Keyword repeats to match plaintext length"]
        KR1["Plaintext: H E L L O W O R L D"]
        KR2["Keyword:   K E Y K E Y K E Y K<br/>(KEY repeating)"]
        KR3["Shifts:    10 4 24 10 4 24 10 4 24 10"]
        KR1 --- KR2 --- KR3
    end

    subgraph PerLetter ["🔤 Each letter uses a different Caesar shift"]
        L1["H + shift 10 = R"]
        L2["E + shift  4 = I"]
        L3["L + shift 24 = J"]
        L4["L + shift 10 = V"]
        L5["O + shift  4 = S"]
        L1 --- L2 --- L3 --- L4 --- L5
    end

    KR3 --> L1
```

### Caesar vs Vigenère

```mermaid
flowchart LR
    CC["Caesar<br/>ONE shift<br/>Brute force: 25 tries<br/>→ trivially broken"]
    VC["Vigenère<br/>N different shifts<br/>Brute force: 26^N tries<br/>→ huge key space"]
    CC -->|"much stronger"| VC
```

### How It Is Broken — Kasiski Examination

```mermaid
flowchart TD
    subgraph Kasiski ["⚠️ Kasiski Examination"]
        K1["Find repeated trigrams in ciphertext<br/>e.g. 'XYZ' appears at positions 5 and 20"]
        K2["Distance = 20 − 5 = 15"]
        K3["Key length divides 15: factors are 1, 3, 5, 15"]
        K4["Try key length = 3<br/>Split ciphertext into 3 Caesar streams<br/>Frequency-analyse each stream separately"]
        K1 --> K2 --> K3 --> K4
    end

    subgraph IC ["📊 Index of Coincidence"]
        IC1["Random text   IC ≈ 0.038"]
        IC2["English text  IC ≈ 0.067"]
        IC3["Vigenère      IC between 0.038 – 0.067<br/>(closer to 0.067 = shorter key)"]
        IC1 --- IC2 --- IC3
    end

    K4 --> IC1
```
