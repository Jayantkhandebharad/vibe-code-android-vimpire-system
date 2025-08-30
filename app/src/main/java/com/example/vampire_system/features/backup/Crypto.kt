package com.example.vampire_system.features.backup

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object Crypto {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS_DEVICE_WRAP = "bk_device_wrap"
    private val rng = SecureRandom()

    fun deriveKey(passphrase: CharArray, salt: ByteArray, iterations: Int = 100_000, bits: Int = 256): SecretKey {
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, iterations, bits)
        val bytes = skf.generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }

    data class Box(val iv: ByteArray, val ciphertext: ByteArray)
    fun aesEncrypt(key: SecretKey, plaintext: ByteArray): Box {
        val iv = ByteArray(12).also { rng.nextBytes(it) }
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        return Box(iv, c.doFinal(plaintext))
    }
    fun aesDecrypt(key: SecretKey, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return c.doFinal(ciphertext)
    }

    private fun deviceWrapKey(context: Context): SecretKey {
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            ALIAS_DEVICE_WRAP,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        try {
            val ks = java.security.KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            (ks.getEntry(ALIAS_DEVICE_WRAP, null) as? java.security.KeyStore.SecretKeyEntry)?.secretKey
                ?: run { kg.init(spec); kg.generateKey() }
        } catch (_: Exception) {
            kg.init(spec); kg.generateKey()
        }
        val ks = java.security.KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return (ks.getEntry(ALIAS_DEVICE_WRAP, null) as java.security.KeyStore.SecretKeyEntry).secretKey
    }

    fun getOrCreateSoftKey(context: Context): ByteArray {
        val f = File(context.filesDir, "backup_softkey.enc")
        val devKey = deviceWrapKey(context)
        if (f.exists()) {
            val bytes = f.readBytes()
            val iv = bytes.copyOfRange(0, 12)
            val ct = bytes.copyOfRange(12, bytes.size)
            return aesDecrypt(devKey, iv, ct)
        }
        val soft = ByteArray(32).also { rng.nextBytes(it) }
        val box = aesEncrypt(devKey, soft)
        f.writeBytes(box.iv + box.ciphertext)
        return soft
    }

    fun wrapSoftKeyWithPassphrase(softKey: ByteArray, passphrase: CharArray): ByteArray {
        val salt = ByteArray(16).also { rng.nextBytes(it) }
        val key = deriveKey(passphrase, salt)
        val box = aesEncrypt(key, softKey)
        return "BKE1".toByteArray(Charsets.US_ASCII) + salt + box.iv + box.ciphertext
    }

    fun unwrapSoftKeyWithPassphrase(enc: ByteArray, passphrase: CharArray): ByteArray {
        require(enc.size > 4 + 16 + 12) { "key.enc too short" }
        val magic = enc.copyOfRange(0,4).toString(Charsets.US_ASCII)
        require(magic == "BKE1") { "bad key.enc" }
        val salt = enc.copyOfRange(4, 20)
        val iv = enc.copyOfRange(20, 32)
        val ct = enc.copyOfRange(32, enc.size)
        val key = deriveKey(passphrase, salt)
        return aesDecrypt(key, iv, ct)
    }

    fun softKeyToSecretKey(soft: ByteArray): SecretKey = SecretKeySpec(soft, "AES")
}


