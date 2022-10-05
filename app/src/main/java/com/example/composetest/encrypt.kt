package com.example.composetest

import android.hardware.biometrics.BiometricPrompt
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.AlgorithmParameters
import java.security.KeyStore
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

//const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
//const val ALGORITHM = "PBKDF2WithHmacSHA1"
//const val KEY_SPEC = "AES"
//const val ITERATIONS = 10000
//const val KEY_SIZE = 256
//const val SALT_SIZE = KEY_SIZE / 8
const val KEY_NAME = "majki5"

class NoteEncoder {
    //    public val salt: ByteArray
//    public val iv: ByteArray
    public lateinit var iv: ByteArray

    constructor(password: BiometricPrompt.CryptoObject) {
//        generateSecretKey(KeyGenParameterSpec.Builder(
//            KEY_NAME,
//            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
//            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//            .setUserAuthenticationRequired(true)
//            // Invalidate the keys if the user has registered a new biometric
//            // credential, such as a new fingerprint. Can call this method only
//            // on Android 7.0 (API level 24) or higher. The variable
//            // "invalidatedByBiometricEnrollment" is true by default.
//            .setInvalidatedByBiometricEnrollment(true)
//            .build()
//        )

//        iv = cipher(password, encrypt = true).iv

//        salt = ByteArray(SALT_SIZE)
//        SecureRandom().nextBytes(salt)
////        key = calculateKey(password, salt)
//
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        iv = ByteArray(cipher.blockSize)
//        SecureRandom().nextBytes(iv)
    }

    private fun cipher(password: BiometricPrompt.CryptoObject, encrypt: Boolean): Cipher {
        val key = getSecretKey()
        val cipher = password.cipher!!
        iv = cipher.iv
        return cipher
    }

    fun toDb(): ByteArray {
        return iv
    }

    constructor(_iv: ByteArray) {
        iv = _iv
    }

    fun encode(string: String, password: BiometricPrompt.CryptoObject): ByteArray {
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        val ivParams = IvParameterSpec(iv)
//        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams)
        return cipher(password, encrypt = true).doFinal(string.toByteArray(Charsets.UTF_8))
    }

    fun decode(encodedString: ByteArray, password: BiometricPrompt.CryptoObject): String {
        return String(cipher(password, encrypt = false).doFinal(encodedString), Charsets.UTF_8)
    }

    companion object {
        fun fromDb(ba: ByteArray): NoteEncoder {
//            val keySizeBytes = KEY_SIZE/8;
//            val keyString = ba.copyOfRange(0, keySizeBytes) // 0..32
//            val key = SecretKeySpec(keyString, KEY_SPEC)
//            val salt = ba.copyOfRange(keySizeBytes, keySizeBytes + SALT_SIZE) // 32..64
//            val iv = ba.copyOfRange(keySizeBytes + SALT_SIZE, keySizeBytes + SALT_SIZE + Cipher.getInstance(TRANSFORMATION).blockSize) // 64..80

            return NoteEncoder(ba)
        }
    }
}

class EncodedString {
    val encodedString: ByteArray

    constructor(string: String, ne: NoteEncoder, password: BiometricPrompt.CryptoObject) {
        encodedString = ne.encode(string, password)
    }

    constructor(es: ByteArray) {
        encodedString = es
    }

    override fun toString(): String = encodedString.toString(Charsets.UTF_8)

    fun decode(password: BiometricPrompt.CryptoObject, noteEncoder: NoteEncoder): String {
        return noteEncoder.decode(encodedString, password)
    }
}

private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
    )
    keyGenerator.init(keyGenParameterSpec)
    keyGenerator.generateKey()
}

fun generateSecretKeyIfNotExists() {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")

    // Before the keystore can be accessed, it must be loaded.
    keyStore.load(null)
    if (!keyStore.containsAlias(KEY_NAME)) {
        generateSecretKey(
            KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .setInvalidatedByBiometricEnrollment(true)
                .setUserAuthenticationParameters(1, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
                .build()
        )
    }
}

fun getSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")

    // Before the keystore can be accessed, it must be loaded.
    keyStore.load(null)
    return keyStore.getKey(KEY_NAME, null) as SecretKey
}

fun getCipher(): Cipher {
    return Cipher.getInstance(
        KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7
    )
}


//private fun calculateKey(
//    password: String,
//    salt: ByteArray
//): SecretKeySpec =
//    SecretKeySpec(
//        SecretKeyFactory.getInstance(ALGORITHM)
//            .generateSecret(
//                PBEKeySpec(
//                    password.toCharArray(),
//                    salt,
//                    ITERATIONS,
//                    KEY_SIZE
//                )
//            ).encoded,
//        KEY_SPEC
//    )

