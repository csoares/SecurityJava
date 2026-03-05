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
        direction LR
        PL["Plaintext\n'H E L L O'"]
        OP["+3 to each\nletter's position"]
        CT["Ciphertext\n'K H O O R'"]
        PL --> OP --> CT
    end

    subgraph Decrypt ["🔓 Decryption  (same key, reversed)"]
        direction LR
        CT2["Ciphertext\n'K H O O R'"]
        OP2["-3 from each\nletter's position"]
        PL2["Plaintext\n'H E L L O'"]
        CT2 --> OP2 --> PL2
    end
```

### Alphabet Shift

```mermaid
flowchart TD
    subgraph Alphabet ["Alphabet shift  (shift = 3, wraps around Z→A)"]
        ABC["Plain:   A B C D E F G H I J K L M N O P Q R S T U V W X Y Z"]
        DEF["Cipher:  D E F G H I J K L M N O P Q R S T U V W X Y Z A B C"]
        ABC -->|"shift +3"| DEF
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
        BF1 --> BF2 --> BF3
    end

    subgraph FreqAnalysis ["📊 Frequency Analysis (no key needed)"]
        FA1["English:    E ≈ 13%,  T ≈ 9%,  A ≈ 8%"]
        FA2["Ciphertext: H ≈ 13%,  W ≈ 9%,  D ≈ 8%"]
        FA3["H − E = 3  →  shift is probably 3!"]
        FA1 --> FA2 --> FA3
    end
```

---

## VigenereCipher.java

Uses a repeating keyword instead of one fixed shift — applying a different Caesar shift per letter. Considered unbreakable for 300 years, until Babbage cracked it in 1854.

### How the Keyword Works

```mermaid
flowchart TD
    subgraph KeyRepeat ["🔑 Keyword repeats to match plaintext length"]
        KR1["Plaintext : H  E  L  L  O     W  O  R  L  D"]
        KR2["Keyword   : K  E  Y  K  E     Y  K  E  Y  K   ← 'KEY' repeated"]
        KR3["Shifts    :10  4 24 10  4    24 10  4 24 10"]
        KR1 --- KR2 --- KR3
    end

    subgraph PerLetter ["🔤 Each letter uses a different Caesar shift"]
        L1["H + shift 10 → R"]
        L2["E + shift  4 → I"]
        L3["L + shift 24 → J"]
        L4["L + shift 10 → V"]
        L5["O + shift  4 → S"]
        L1 --- L2 --- L3 --- L4 --- L5
    end
```

### Caesar vs Vigenère

```mermaid
flowchart LR
    CC["Caesar\nONE shift\nBrute force: 25 tries\n→ trivially broken"]
    VC["Vigenère\nN different shifts\nBrute force: 26^N tries\n→ huge key space"]
    CC -->|"much stronger"| VC
```

### How It Is Broken — Kasiski Examination

```mermaid
flowchart TD
    subgraph Kasiski ["⚠️ Kasiski Examination"]
        K1["Find repeated trigrams in ciphertext\ne.g. 'XYZ' appears at positions 5 and 20"]
        K2["Distance = 20 − 5 = 15"]
        K3["Key length divides 15: factors are 1, 3, 5, 15"]
        K4["Try key length = 3 → split ciphertext into 3 Caesar streams\n→ frequency-analyse each one separately"]
        K1 --> K2 --> K3 --> K4
    end

    subgraph IC ["📊 Index of Coincidence"]
        IC1["Random text   IC ≈ 0.038"]
        IC2["English text  IC ≈ 0.067"]
        IC3["Vigenère      IC between 0.038 – 0.067\n(closer to 0.067 = shorter key)"]
        IC1 --- IC2 --- IC3
    end
```
