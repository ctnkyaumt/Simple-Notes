package com.simplemobiletools.notes.pro.helpers

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object NotebookPasswordHasher {
    private const val SALT_BYTES = 16

    fun createHash(password: String): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = sha256(salt, password)
        return "${Base64.encodeToString(salt, Base64.NO_WRAP)}:${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(':', limit = 2)
        if (parts.size != 2) return false

        val salt = runCatching { Base64.decode(parts[0], Base64.NO_WRAP) }.getOrNull() ?: return false
        val expected = runCatching { Base64.decode(parts[1], Base64.NO_WRAP) }.getOrNull() ?: return false
        val actual = sha256(salt, password)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun sha256(salt: ByteArray, password: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        digest.update(password.toByteArray(Charsets.UTF_8))
        return digest.digest()
    }
}
