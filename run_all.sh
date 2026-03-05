#!/usr/bin/env bash
# run_all.sh — Build the project and run every main class, reporting pass/fail.
# Usage: ./run_all.sh [--skip-slow]
#   --skip-slow  Skip classes that take >10 seconds (DiffieHellman, PasswordHashing)

set -euo pipefail

SKIP_SLOW=false
for arg in "$@"; do
    [[ "$arg" == "--skip-slow" ]] && SKIP_SLOW=true
done

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# ── Colours ──────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

pass() { echo -e "  ${GREEN}PASS${NC}  $1"; }
fail() { echo -e "  ${RED}FAIL${NC}  $1"; }
skip() { echo -e "  ${YELLOW}SKIP${NC}  $1 (use without --skip-slow to run)"; }

# ── Step 1: Compile ───────────────────────────────────────────────────────────
echo "========================================"
echo " Building SecurityJava"
echo "========================================"
if mvn compile -q 2>&1; then
    echo -e "${GREEN}Build successful.${NC}"
else
    echo -e "${RED}Build FAILED. Fix compilation errors before running.${NC}"
    exit 1
fi
echo ""

# Build classpath for running main classes
CP=$(mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/cp.txt 2>/dev/null && cat /tmp/cp.txt)
CP="target/classes:$CP"

# ── Step 2: Run each main class ───────────────────────────────────────────────
echo "========================================"
echo " Running all example classes"
echo "========================================"
echo ""

PASSED=0
FAILED=0
SKIPPED=0

# Safe increment that works with set -e (((n++)) exits 1 when n was 0)
inc() { eval "$1=$(( ${!1} + 1 ))"; }

run_class() {
    local class="$1"
    local label="$2"
    local slow="${3:-false}"

    if [[ "$slow" == "true" && "$SKIP_SLOW" == "true" ]]; then
        skip "$label"
        inc SKIPPED
        return
    fi

    echo "--- $label ---"
    if java -cp "$CP" "$class" > /tmp/run_output.txt 2>&1; then
        pass "$label"
    else
        fail "$label"
        echo "    Output:"
        sed 's/^/      /' /tmp/run_output.txt
        inc FAILED
        return
    fi
    inc PASSED
}

run_class "security.encryption.classic.CaesarCipher" \
          "CaesarCipher"

run_class "security.encryption.classic.VigenereCipher" \
          "VigenereCipher"

run_class "security.encryption.symmetric.SymmetricEncryptionExample" \
          "SymmetricEncryptionExample (AES-CBC)"

run_class "security.encryption.asymmetric.AsymmetricEncryptionExample" \
          "AsymmetricEncryptionExample (RSA)"

run_class "security.encryption.integrity.IntegrityCheckHash" \
          "IntegrityCheckHash (SHA-256)"

run_class "security.encryption.integrity.IntegrityCheckSignature" \
          "IntegrityCheckSignature (RSA signatures)"

run_class "security.encryption.integrity.FileDigitalSignature" \
          "FileDigitalSignature"

run_class "security.encryption.modes.CipherModesComparison" \
          "CipherModesComparison (ECB/CBC/GCM)"

run_class "security.mac.HMACExample" \
          "HMACExample"

run_class "security.keyexchange.ECDHExample" \
          "ECDHExample (ECDH key exchange)"

run_class "security.ecc.ECCSignatureExample" \
          "ECCSignatureExample (ECDSA)"

run_class "security.attacks.TimingAttackExample" \
          "TimingAttackExample"

run_class "security.attacks.WeakRandomnessExample" \
          "WeakRandomnessExample"

run_class "security.attacks.RainbowTableExample" \
          "RainbowTableExample"

run_class "security.pki.CertificateChainExample" \
          "CertificateChainExample"

# Slow classes (DH param generation ~5-10s, PBKDF2 ~3-5s)
run_class "security.keyexchange.DiffieHellmanExample" \
          "DiffieHellmanExample (slow: 2048-bit DH param gen)" "true"

run_class "security.passwords.PasswordHashingExample" \
          "PasswordHashingExample (slow: PBKDF2 310k iterations)" "true"

# ── Step 3: Run unit tests ────────────────────────────────────────────────────
echo ""
echo "========================================"
echo " Running JUnit tests"
echo "========================================"
if mvn test -q 2>&1; then
    echo -e "${GREEN}All tests passed.${NC}"
else
    echo -e "${RED}Some tests FAILED. See output above.${NC}"
    inc FAILED
fi

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
echo "========================================"
echo " Summary"
echo "========================================"
echo -e "  ${GREEN}Passed:${NC}  $PASSED"
echo -e "  ${RED}Failed:${NC}  $FAILED"
[[ $SKIPPED -gt 0 ]] && echo -e "  ${YELLOW}Skipped:${NC} $SKIPPED (run without --skip-slow to include)"
echo ""

if [[ $FAILED -eq 0 ]]; then
    echo -e "${GREEN}All checks passed!${NC}"
    exit 0
else
    echo -e "${RED}$FAILED check(s) failed.${NC}"
    exit 1
fi
