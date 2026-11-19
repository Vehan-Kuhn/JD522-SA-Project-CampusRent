package za.ac.ctu.campusrent.util;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Creates and verifies password hashes.
 * Passwords are never stored as plain text.
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);

        byte[] hash = deriveKey(password.toCharArray(), salt, ITERATIONS);

        return "pbkdf2_sha256$" + ITERATIONS + "$"
                + Base64.getEncoder().withoutPadding().encodeToString(salt)
                + "$"
                + Base64.getEncoder().withoutPadding().encodeToString(hash);
    }

    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }

        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4 || !"pbkdf2_sha256".equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100_000 || iterations > 2_000_000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            if (salt.length < 16 || expectedHash.length == 0) {
                return false;
            }

            byte[] actualHash = deriveKey(
                    password.toCharArray(), salt, iterations
            );

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] deriveKey(
            char[] password, byte[] salt, int iterations) {

        PBEKeySpec spec = new PBEKeySpec(
                password, salt, iterations, KEY_LENGTH_BITS
        );

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "Password hashing is unavailable on this system.",
                    exception
            );

        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(password, '\0');
        }
    }
}
