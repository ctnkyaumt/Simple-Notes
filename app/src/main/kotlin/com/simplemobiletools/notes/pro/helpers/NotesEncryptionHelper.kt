package com.simplemobiletools.notes.pro.helpers

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object NotesEncryptionHelper {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_STRING = "SimpleNotesKey12"
    private const val ENCRYPTED_PREFIX = "ENC:"

    private val secretKey = SecretKeySpec(KEY_STRING.toByteArray(Charsets.UTF_8), ALGORITHM)

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return ENCRYPTED_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encryptedText: String): String? {
        if (!encryptedText.startsWith(ENCRYPTED_PREFIX)) {
            return null
        }
        return try {
            val base64Data = encryptedText.removePrefix(ENCRYPTED_PREFIX)
            val combined = Base64.decode(base64Data, Base64.NO_WRAP)
            val iv = combined.copyOfRange(0, 16)
            val encrypted = combined.copyOfRange(16, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
