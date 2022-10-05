package com.example.composetest

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricPrompt
import android.provider.Settings
import androidx.core.content.edit
import java.security.MessageDigest
import java.util.*

//@SuppressLint("HardwareIds")
//fun hashPassword(password: String, hashingMethod: String, context: Context): String {
//    var saltedPassword = password
//    saltedPassword += "jcubed1234"
//    saltedPassword += Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//    val md = MessageDigest.getInstance(hashingMethod)
//    return md.digest(password.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8)
//}

//data class Password(val hashedPassword: ByteArray?, val hashingMethod: String)

//const val PASSWORD_KEY = "password"
//const val HASHING_METHOD_KEY = "password_hashing_method"
//const val DEFAULT_HASHING_ALGO = "SHA-512"

//fun loadPassword(sp: SharedPreferences): Password {
//    val existingPassword = sp.getString(PASSWORD_KEY, null)!!.toByteArray(Charsets.UTF_8)
//    val hashingMethod = sp.getString(HASHING_METHOD_KEY, DEFAULT_HASHING_ALGO)!!
//    return Password(existingPassword, hashingMethod)
//}
//
//fun savePassword(sp: SharedPreferences, newPassword: BiometricPrompt.CryptoObject, context: Context) {
//    sp.edit {
//        this.putString(PASSWORD_KEY, newPassword.signature.parameters.encoded.toString(Charsets.UTF_8))
//        this.putString(HASHING_METHOD_KEY, newPassword.cipher.algorithm)
//    }
//}
//const val TIME_AFTER_WHICH_DELAY_PASSED_KEY = "delay_time"
//
//fun saveDelay(sp: SharedPreferences, delaySeconds: Long) {
//    sp.edit {
//        val cal = Calendar.getInstance()
//        cal.add(Calendar.SECOND, delaySeconds.toInt())
//        val delayPassedAfter = cal.timeInMillis
//        this.putLong(TIME_AFTER_WHICH_DELAY_PASSED_KEY, delayPassedAfter)
//    }
//}
//
//fun timeSinceAfterDelayPassed(sp: SharedPreferences): Long {
//    val millisOfTimeToBeAfter = sp.getLong(TIME_AFTER_WHICH_DELAY_PASSED_KEY, 0)
//
//    val now = Calendar.getInstance()
//    val nowInMillis = now.timeInMillis
//    return nowInMillis - millisOfTimeToBeAfter
//}
