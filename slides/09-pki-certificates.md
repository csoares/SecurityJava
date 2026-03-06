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

# Lecture 9
## PKI & Certificates

Certificate Chains · TLS Handshake · Certificate Authorities · Trust

---

## The Identity Problem

Digital signatures prove the data was signed by the holder of a private key.
But who does that private key belong to?

```
You receive a signed message from "public_key_XYZ".
Is XYZ really Alice? Or is it Eve pretending to be Alice?
```

We need a way to bind public keys to real-world identities.
This is what **Public Key Infrastructure (PKI)** solves.

---

## X.509 Certificates

A certificate is a signed document containing:

```
Subject:    CN=www.example.com, O=Example Corp
Issuer:     CN=DigiCert TLS RSA SHA256 CA
Public Key: [RSA 2048-bit public key]
Valid From: 2024-01-01
Valid To:   2025-01-01
Signature:  [DigiCert's signature over all the above]
```

The **Issuer** has signed this certificate with their private key.
If you trust the Issuer, you can trust that the public key belongs to `www.example.com`.

---

## Certificate Chain of Trust

```
Root CA (self-signed, in your browser/OS trust store)
    └── Intermediate CA (signed by Root CA)
            └── Server Certificate (signed by Intermediate CA)
```

When you connect to a server:
1. Server sends its certificate + intermediate certificate
2. Your browser verifies: Intermediate CA signed the server cert
3. Your browser verifies: Root CA signed the intermediate cert
4. Root CA is in your trusted store (installed with your OS)

**Trust flows from the Root down the chain.**

---

## Why Intermediate CAs?

If Root CAs signed everything directly:
- A compromised Root = everything is broken
- Root CA private keys would be used frequently = higher exposure

**Solution:** Root CAs sign Intermediate CAs, then go offline.
The intermediate CA does the day-to-day signing.

```
Root CA private key:  stored in an air-gapped HSM, used rarely
Intermediate CA key:  online, used daily — if compromised, root revokes it
```

---

## Certificate Verification in Java

```java
// Load the certificate
CertificateFactory cf = CertificateFactory.getInstance("X.509");
X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);

// Build a PKIX trust chain validator
PKIXParameters params = new PKIXParameters(trustStore);
params.setRevocationEnabled(false);

CertPathValidator validator = CertPathValidator.getInstance("PKIX");
CertPath path = cf.generateCertPath(certList);
validator.validate(path, params); // throws if invalid
```

---

## TLS Handshake (TLS 1.3)

```
Client                              Server
  | ─── ClientHello ──────────────>  |  (supported ciphers, random nonce)
  | <── ServerHello ─────────────── |  (chosen cipher, random nonce)
  | <── Certificate ─────────────── |  (server's X.509 certificate)
  | <── CertificateVerify ────────── |  (signature proving private key ownership)
  | <── Finished ─────────────────── |
  | ─── Finished ──────────────────> |
  |                                   |
  |  ←── Encrypted application data ──→  |
```

No RSA key exchange in TLS 1.3. ECDHE only → **Forward Secrecy mandatory**.

---

## Certificate Revocation

What if a private key is compromised before the certificate expires?

**CRL (Certificate Revocation List):** a signed list of revoked serial numbers.
- Browsers download periodically — can be stale.

**OCSP (Online Certificate Status Protocol):** real-time query to CA.
- Privacy concern: CA learns which sites you visit.

**OCSP Stapling:** server periodically fetches its own OCSP response and staples it to the TLS handshake — no privacy leak, no extra latency.

---

## Certificate Transparency (CT)

Since 2018, all publicly trusted certificates must be logged in a public CT log.

```
CA issues cert → submits to CT logs → gets Signed Certificate Timestamp (SCT)
SCT is embedded in the certificate
Browser checks SCT on connection
```

**Why:** historically, CAs issued fraudulent certificates (DigiNotar 2011, Symantec 2017).
CT logs make it impossible to issue a certificate without it being publicly visible.

Anyone can monitor CT logs for certificates issued for their domain.

---

## Let's Encrypt — Free Certificates

Before 2016, TLS certificates cost $100–$1000/year.

Let's Encrypt issues free, automated certificates using the **ACME protocol**:

```bash
certbot certonly --webroot -d example.com
# Proves domain ownership, issues 90-day cert, auto-renews
```

Result: HTTPS adoption went from ~30% in 2015 to ~95% today.

---

## Running the Example

```bash
mvn exec:java -Dexec.mainClass="security.pki.CertificateChainExample"
```

Observe:
- Certificate generated with subject, issuer, validity period
- Chain validation: root → intermediate → end-entity
- Tamper detection: modifying the certificate invalidates the signature
- Trust store verification

---

<!-- _class: lead -->

## Next: Lecture 10
# Attacks & Defences
### Timing attacks · Weak randomness · Rainbow tables
