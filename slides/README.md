# Security Lectures — Slide Decks

10 lectures in [Marp](https://marp.app/) markdown format.

## Lectures

| File | Topic |
|------|-------|
| `00-overview.md` | Course Overview & CIA Triad |
| `01-classical-ciphers.md` | Caesar, Vigenère, Frequency Analysis |
| `02-symmetric-encryption.md` | AES, Keys, IVs, Base64 |
| `03-block-cipher-modes.md` | ECB, CBC, GCM |
| `04-hashing-integrity.md` | SHA-256, Avalanche Effect, Merkle Trees |
| `05-digital-signatures.md` | RSA, SHA256withRSA, File Signing |
| `06-mac-hmac.md` | HMAC, Timing Attacks, Constant-time Compare |
| `07-key-exchange.md` | Diffie-Hellman, ECDH, Forward Secrecy |
| `08-password-security.md` | PBKDF2, bcrypt, Argon2, Salting |
| `09-pki-certificates.md` | Certificate Chains, TLS Handshake, CT Logs |
| `10-attacks-defences.md` | Timing Attacks, Weak RNG, Rainbow Tables |

## Rendering Slides

### Option 1 — VS Code (recommended)

Install the [Marp for VS Code](https://marketplace.visualstudio.com/items?itemName=marp-team.marp-vscode) extension, then open any `.md` file and click the preview button.

### Option 2 — Marp CLI (export to PDF or HTML)

```bash
npm install -g @marp-team/marp-cli

# Export one lecture to PDF
marp 01-classical-ciphers.md --pdf

# Export all to HTML
marp --html *.md

# Start a live preview server
marp --server .
```

### Option 3 — GitHub

GitHub renders Marp frontmatter as plain markdown. The slides are readable directly on GitHub as structured documents.
