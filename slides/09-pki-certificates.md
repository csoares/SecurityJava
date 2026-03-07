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

# Lecture 9
## PKI & Certificates
### Digital passports — proving who you are online

---

## The Identity Gap

So far we can:
- Encrypt messages (AES)
- Sign messages (RSA signatures)
- Exchange keys securely (ECDH)

But when you visit `https://mybank.com`, you need to know:
- Is this really my bank's server?
- Or is Mallory pretending to be the bank?

A public key alone doesn't answer this.
**PKI (Public Key Infrastructure)** solves identity.

---

## The Passport Analogy

```
Physical world:           Digital world (PKI):
────────────────          ─────────────────────
Government issues         Certificate Authority (CA)
your passport     →       issues a certificate

Passport contains:        Certificate contains:
  Your photo                Server's public key
  Your name                 Server's domain name
  Issued by: Gov.           Issued by: DigiCert CA
  Signed by: Gov.           Signed by: DigiCert CA
  Expiry date               Expiry date

Verifier checks:          Browser checks:
  Is the gov. signature     Is the CA signature
  authentic?                authentic?
```

---

## What is an X.509 Certificate?

A certificate is a signed document that binds a **public key** to a **name**:

```
Subject:    CN=www.mybank.com, O=MyBank Ltd, C=GB
Public Key: [RSA 2048-bit public key — the bank's]
Valid From: 2025-01-01
Valid To:   2026-01-01
Issued by:  DigiCert Global CA
Signature:  [DigiCert's digital signature over all the above]
```

If you trust DigiCert, and DigiCert signed this, then:
- The public key belongs to MyBank
- MyBank owns the domain `www.mybank.com`

---

## The Chain of Trust

No single authority signs everything. It's a chain:

```
🏛️  ROOT CA (DigiCert Root G2)
    Self-signed. Baked into your OS/browser.
    Private key used VERY rarely. Stored offline in a vault.
         |
         | signs
         ▼
🏢  INTERMEDIATE CA (DigiCert TLS RSA SHA256 2020 CA1)
    Signs certificates daily.
    If compromised: Root CA revokes it.
         |
         | signs
         ▼
🌐  SERVER CERTIFICATE (www.mybank.com)
    The certificate your browser sees.
    Expires in 90 days to 1 year.
```

---

## Why Intermediate CAs?

If the Root CA signed everything directly:
- Root CA private key used constantly → higher chance of compromise
- If Root CA is compromised → **all trust on the internet is broken**

With Intermediate CAs:
```
Root CA key: locked in an air-gapped HSM, used maybe once a year
             if compromised → catastrophe, but very unlikely

Intermediate CA key: used daily
                     if compromised → Root CA revokes just this intermediate
                     → millions of certs revoked but Root is still safe
```

**Damage is contained.** This is defence in depth.

---

## TLS Handshake — What Happens When You Visit HTTPS

```
Browser                               Server (mybank.com)
   │                                        │
   │──── "Hello, I support TLS 1.3" ───────►│
   │◄─── "Here is my certificate"  ─────────│
   │     (contains server's public key)      │
   │◄─── "Here's my ECDH public key,         │
   │      signed with my certificate key"   │
   │                                        │
   │ Browser verifies:                      │
   │   ✓ Certificate chain up to trusted CA │
   │   ✓ Domain matches certificate         │
   │   ✓ Certificate not expired            │
   │   ✓ Signature on ECDH key is valid     │
   │                                        │
   │── ECDH key exchange → shared secret ───│
   │════════ Encrypted connection ══════════│
```

---

## What the Browser Checks

When you see the 🔒 padlock:

```
1. CHAIN VALID?
   Certificate signed by Intermediate CA ✓
   Intermediate CA signed by Root CA ✓
   Root CA is in my trust store ✓

2. DOMAIN MATCHES?
   Certificate says: CN=www.mybank.com
   Address bar says:    www.mybank.com  ✓

3. NOT EXPIRED?
   Valid From: 2025-01-01
   Valid To:   2026-01-01
   Today:      2025-06-15  ✓

4. NOT REVOKED?
   Check OCSP / CRL  ✓
```

All four checks pass → 🔒

---

## Certificate Revocation

What if the bank's private key is stolen before the certificate expires?

```
Option 1: CRL (Certificate Revocation List)
  CA publishes a list of revoked certificate serial numbers.
  Browser downloads periodically. Can be stale by hours.

Option 2: OCSP (Online Certificate Status Protocol)
  Browser asks CA in real time: "Is this certificate still valid?"
  Privacy issue: CA learns every site you visit.

Option 3: OCSP Stapling (best)
  Server fetches its own OCSP response and attaches it to the TLS handshake.
  Browser gets freshness proof without asking the CA.
  No privacy leak. No extra latency.
```

---

## Let's Encrypt Changed Everything

Before 2016: TLS certificates cost £100–£1000/year.

Let's Encrypt: **free, automated, 90-day certificates**.

```bash
# Get a free TLS certificate in 30 seconds:
certbot certonly --webroot -d mysite.com

# Auto-renewal (runs every 90 days):
certbot renew --quiet
```

Result:
- HTTPS was on ~30% of websites in 2015
- HTTPS is on ~95% of websites today

---

## Try It Yourself

```bash
mvn exec:java -Dexec.mainClass="security.pki.CertificateChainExample"
```

**What to observe:**
- Certificate generated with subject, issuer, validity period
- Chain: root → intermediate → end-entity
- Chain validation: all valid → accepted
- Tamper detection: modify the certificate → signature fails → rejected
- Expired certificate → rejected

---

<!-- _class: lead -->

## Next: Lecture 10
# Attacks & Defences
### Think like an attacker — defend like an expert
