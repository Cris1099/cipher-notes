package dev.cipher.notes.crypto

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encryption manager using AES-256-GCM with PBKDF2.
 * 
 * References:
 * - Google Tink AEAD (https://developers.google.com/tink/manage-keys)
 * - OWASP Password Storage (https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
 * 
 * Design:
 * - PBKDF2 with 200k iterations + SHA-256 for key derivation
 * - AES-256-GCM for authenticated encryption
 * - Random salt (16 bytes) + random IV (12 bytes) per encryption
 * - No passwords stored ever
 * - All crypto on-device, zero network
 */
@Singleton
class CryptoManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val SALT_LENGTH = 16
        private const val IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128  // 128 bits
        private const val PBKDF2_ITERATIONS = 200_000
        private const val PBKDF2_KEY_LENGTH = 256  // bits
    }

    /**
     * Encrypts plaintext with password using AES-256-GCM + PBKDF2.
     * 
     * Output format (Base64): [16-byte salt][12-byte IV][ciphertext with auth tag]
     * 
     * @param plaintext The unencrypted content
     * @param password User's password (never stored)
     * @return Base64-encoded encrypted blob
     */
    fun encrypt(plaintext: String, password: String): String {
        // Generate random salt and IV
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }

        // Derive encryption key from password
        val key = deriveKey(password, salt)

        // Encrypt using AES-256-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            key,
            GCMParameterSpec(GCM_TAG_LENGTH, iv)
        )
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Pack salt + IV + ciphertext
        val combined = salt + iv + ciphertext
        return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
    }

    /**
     * Decrypts a Base64 blob produced by [encrypt].
     * 
     * @param cipherB64 Base64-encoded [salt:IV:ciphertext]
     * @param password User's password attempt
     * @return Decrypted plaintext
     * @throws javax.crypto.AEADBadTagException if password is wrong or data corrupted
     */
    fun decrypt(cipherB64: String, password: String): String {
        val combined = android.util.Base64.decode(cipherB64, android.util.Base64.NO_WRAP)

        // Unpack salt, IV, ciphertext
        val salt = combined.sliceArray(0 until SALT_LENGTH)
        val iv = combined.sliceArray(SALT_LENGTH until SALT_LENGTH + IV_LENGTH)
        val ciphertext = combined.sliceArray(SALT_LENGTH + IV_LENGTH until combined.size)

        // Re-derive key using stored salt
        val key = deriveKey(password, salt)

        // Decrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            GCMParameterSpec(GCM_TAG_LENGTH, iv)
        )
        val plainBytes = cipher.doFinal(ciphertext)
        return String(plainBytes, Charsets.UTF_8)
    }

    /**
     * PBKDF2 key derivation with SHA-256.
     * 200,000 iterations (as per OWASP recommendations 2024).
     * 
     * @param password User's password
     * @param salt Random salt bytes
     * @return 256-bit key suitable for AES-256
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            PBKDF2_KEY_LENGTH
        )
        val secretKey = factory.generateSecret(spec)
        spec.clearPassword()  // Clear password from memory
        return SecretKeySpec(secretKey.encoded, 0, secretKey.encoded.size, "AES")
    }

    /**
     * Estimate password strength for UI feedback.
     * Not a security measure — just UX guidance.
     */
    fun passwordStrength(password: String): PasswordStrength {
        var score = 0
        if (password.length >= 8)   score += 25
        if (password.length >= 14)  score += 20
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() })
            score += 20
        if (password.any { it.isDigit() })
            score += 15
        if (password.any { !it.isLetterOrDigit() })
            score += 20

        return when {
            score < 30  -> PasswordStrength.WEAK
            score < 55  -> PasswordStrength.MODERATE
            score < 80  -> PasswordStrength.STRONG
            else        -> PasswordStrength.VERY_STRONG
        }
    }
}

enum class PasswordStrength(val label: String, val fraction: Float) {
    WEAK("Weak", 0.25f),
    MODERATE("Moderate", 0.5f),
    STRONG("Strong", 0.75f),
    VERY_STRONG("Very strong", 1f)
}
