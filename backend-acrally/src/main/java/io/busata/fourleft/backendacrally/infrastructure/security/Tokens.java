package io.busata.fourleft.backendacrally.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Random-token helpers for agent pairing/keys. High-entropy tokens are hashed with SHA-256
 * for storage/lookup (no bcrypt needed — bcrypt is for low-entropy passwords).
 */
public final class Tokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();
    // No 0/O/1/I/L — unambiguous when read aloud or typed.
    private static final char[] USER_CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789".toCharArray();

    private Tokens() {
    }

    /** Opaque agent API key, e.g. {@code acr_<43 base64url chars>}. */
    public static String apiKey() {
        return "acr_" + randomBase64Url(32);
    }

    /** Agent-held device secret exchanged for a key. */
    public static String deviceCode() {
        return randomBase64Url(32);
    }

    /** Short human-entered code, formatted {@code XXXX-XXXX}. */
    public static String userCode() {
        StringBuilder sb = new StringBuilder(9);
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                sb.append('-');
            }
            sb.append(USER_CODE_ALPHABET[RANDOM.nextInt(USER_CODE_ALPHABET.length)]);
        }
        return sb.toString();
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String randomBase64Url(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return BASE64URL.encodeToString(buffer);
    }
}
