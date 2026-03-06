# PKI — Public Key Infrastructure

Public keys solve the encryption problem but create a new one: how do you know a public key actually belongs to `www.example.com` and not to an attacker? **Certificate Authorities** answer this by digitally signing certificates that bind a public key to an identity.

Run with:
```bash
mvn exec:java -Dexec.mainClass="security.pki.CertificateChainExample"
```

---

## CertificateChainExample.java

### The Certificate Chain of Trust

```mermaid
flowchart TD
    ROOT["🏛️ Root CA<br/>Self-signed · Stored in OS/browser trust store<br/>Private key kept OFFLINE in a vault"]
    INT["🏢 Intermediate CA<br/>Signed by Root CA<br/>Kept ONLINE to sign site certificates"]
    SERVER["🌐 Server Certificate<br/>CN=www.example.com<br/>Contains server's public key<br/>Presented during TLS handshake"]

    ROOT -->|"signs"| INT
    INT  -->|"signs"| SERVER
    SERVER --> NOTE["Trust flows DOWN: Root → Intermediate → Server<br/>Verification goes UP: each signature checked back to Root CA"]
```

### Chain Validation — What the Browser Does

```mermaid
flowchart TD
    V1["1. Server sends its certificate + intermediate certificate(s)"]
    V2["2. Browser verifies server cert signature using intermediate CA public key ✅"]
    V3["3. Browser verifies intermediate cert signature using root CA public key ✅"]
    V4["4. Browser checks: is this root CA in my trust store? ✅"]
    V5["5. Browser checks: cert not expired, hostname matches CN/SAN, not revoked ✅"]
    V6["🔒 Trust established → proceed with ECDHE key exchange → AES-GCM session"]
    V1 --> V2 --> V3 --> V4 --> V5 --> V6
```

### Why Intermediate CAs Exist

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph Why
        T_Why["❓ Why Not Root CA → Server Directly?"]
        T_Why ~~~ W1
        W1["Root CA private key kept OFFLINE — extremely high security"]
        W2["Intermediate CA kept ONLINE to issue certificates daily"]
        W3["If Intermediate CA is compromised:<br/>Revoke that intermediate only<br/>Root CA and other intermediates unaffected"]
        W4["If Root CA were compromised directly:<br/>Entire PKI collapses — all certificates untrusted"]
        W1 --> W2 --> W3 --> W4
    end
```

### Tamper Detection — Demonstrated in Code

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph TamperDemo
        T_TamperDemo["🚨 Tamper Detection"]
        T_TamperDemo ~~~ TC1
        TC1["serverCert.verify(rootCert.getPublicKey())"]
        TC2["❌ SignatureException — server cert was NOT signed by Root CA"]
        TC3["It was signed by Intermediate CA's private key"]
        TC4["Attacker cannot forge a certificate without the CA's private key"]
        TC1 --> TC2 --> TC3 --> TC4
    end
```

### TLS Handshake — Full Picture

```mermaid
sequenceDiagram
    participant Browser
    participant Server
    participant TrustStore as 🔒 Browser Trust Store

    Browser->>Server: ClientHello (supported cipher suites, TLS version)
    Server->>Browser: ServerHello + server cert + intermediate cert(s)
    Browser->>TrustStore: Validate certificate chain against trusted Root CAs
    TrustStore->>Browser: ✅ Chain trusted

    Note over Browser: Extract server's public key from validated certificate
    Browser->>Server: ECDHE key exchange (ephemeral public key)
    Server->>Browser: ECDHE key exchange (ephemeral public key)

    Note over Browser,Server: Both derive same shared secret via ECDH
    Note over Browser,Server: Derive AES-GCM session keys from shared secret (via HKDF)

    Browser->>Server: 🔒 Encrypted application data (AES-GCM)
    Server->>Browser: 🔒 Encrypted application data (AES-GCM)
```

### What an X.509 Certificate Contains

```mermaid
%%{init: {'flowchart': {'subGraphTitleMargin': {'top': 50, 'bottom': 10}}}}%%
flowchart TD
    subgraph CertFields
        T_CertFields["📄 X.509 Certificate Fields"]
        T_CertFields ~~~ F1
        F1["Subject DN: who this certificate identifies<br/>e.g. CN=www.example.com, O=Example Corp, C=US"]
        F2["Issuer DN: who signed this certificate<br/>e.g. CN=Example Intermediate CA"]
        F3["Public Key: the subject's RSA or EC public key"]
        F4["Validity: notBefore and notAfter dates"]
        F5["Serial Number: unique per issuing CA"]
        F6["Signature: issuer's digital signature over all the above fields"]
        F1 --- F2 --- F3 --- F4 --- F5 --- F6
    end
```
