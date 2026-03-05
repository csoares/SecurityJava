package security.attacks;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/*
 * DIAGRAM: see README.md in this package for visual diagrams of this class.
 *
 * Rainbow Table Attack Demonstration
 *
 * A rainbow table is a precomputed lookup table: hash → password.
 * If a database stores passwords as plain hashes (MD5, SHA-1, SHA-256),
 * an attacker with the database can instantly reverse any hash
 * that corresponds to a password in the table.
 *
 * Defense: salt — a unique random value prepended to each password before hashing.
 * Salt makes every hash unique, even for identical passwords.
 * It does NOT make hashing "stronger" against brute force, but it destroys
 * the effectiveness of precomputed tables by making them per-user.
 *
 * Combined defense: salt + slow hash function (PBKDF2, bcrypt, Argon2).
 */
public class RainbowTableExample {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Rainbow Table Attack Demonstration ===");
        System.out.println();
        System.out.println("A rainbow table precomputes hash→password pairs.");
        System.out.println("Given a stolen password database, cracking is O(1) — instant lookup.");
        System.out.println("Salt defeats precomputation by making each hash unique per user.");
        System.out.println();

        // ─── Build mini rainbow table ─────────────────────────────────────────────
        System.out.println("--- Building a Mini Rainbow Table (10 common passwords) ---");
        String[] commonPasswords = {
            "password", "123456", "qwerty", "letmein", "admin",
            "welcome", "monkey", "dragon", "master", "sunshine"
        };

        Map<String, String> rainbowTable = new HashMap<>();
        for (String pw : commonPasswords) {
            String hash = sha256Hex(pw);
            rainbowTable.put(hash, pw);
            System.out.println(hash.substring(0, 20) + "...  →  \"" + pw + "\"");
        }
        System.out.println();
        System.out.println("Real rainbow tables contain billions of entries and cover most");
        System.out.println("passwords up to 8-10 characters, including l33t substitutions.");
        System.out.println();

        // ─── Attack scenario 1: unsalted database ────────────────────────────────
        System.out.println("--- Attack Scenario: Stolen Database with Unsalted SHA-256 Hashes ---");
        System.out.println("Three users all use common passwords:");
        System.out.println();

        String[] victims = {"password", "123456", "admin"};
        for (String victimPw : victims) {
            String storedHash = sha256Hex(victimPw);
            System.out.println("User stored hash: " + storedHash.substring(0, 24) + "...");
            String cracked = rainbowTable.get(storedHash);
            System.out.println("Rainbow lookup:   " + (cracked != null ? "CRACKED → \"" + cracked + "\"" : "not found"));
            System.out.println();
        }
        System.out.println("All three cracked in O(1) time — no brute force needed.");
        System.out.println("Notice: two users with the same password have the SAME hash → obvious.");
        System.out.println();

        // ─── Defense: salted hash ─────────────────────────────────────────────────
        System.out.println("--- Defense: Unique Salt per User ---");
        System.out.println("Each user gets a different random salt stored alongside their hash.");
        System.out.println();

        String targetPassword = "password"; // same common password
        for (int user = 1; user <= 3; user++) {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            String saltHex = bytesToHex(salt);

            // Hash = SHA-256(salt || password)
            String saltedHash = sha256Hex(saltHex + targetPassword);

            System.out.println("User " + user + ":");
            System.out.println("  Salt:        " + saltHex);
            System.out.println("  Stored hash: " + saltedHash.substring(0, 24) + "...");

            String crackedSalted = rainbowTable.get(saltedHash);
            System.out.println("  Rainbow lookup: " + (crackedSalted != null
                    ? "CRACKED: " + crackedSalted : "NOT FOUND — attack defeated!"));
            System.out.println();
        }

        System.out.println("All three users have the same password \"password\",");
        System.out.println("but three completely different salted hashes — impossible to batch-crack.");
        System.out.println("The precomputed rainbow table is useless because the salt was never");
        System.out.println("included when the table was built.");
        System.out.println();

        // ─── What attackers must do instead ──────────────────────────────────────
        System.out.println("--- What the Attacker Must Do Now ---");
        System.out.println("For each user: compute SHA-256(userSalt + guess) for every guess.");
        System.out.println("No shared work between users — must crack each account independently.");
        System.out.println("With SHA-256: GPUs can still try ~10 billion guesses/second per user.");
        System.out.println("→ Use PBKDF2, bcrypt, or Argon2 to make each guess take ~100ms.");
        System.out.println("  10 billion/sec drops to ~10 guesses/sec — effectively infeasible.");
        System.out.println();

        System.out.println("--- Key Takeaways ---");
        System.out.println("1. Plain hash (MD5/SHA-256) → instantly cracked by rainbow tables");
        System.out.println("2. Salt → defeats precomputed tables; forces per-user brute force");
        System.out.println("3. Salt + slow hash (PBKDF2/bcrypt/Argon2) → brute force is impractical");
        System.out.println("4. Never invent your own password storage scheme — use a vetted library");
    }

    public static String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return bytesToHex(md.digest(input.getBytes()));
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
