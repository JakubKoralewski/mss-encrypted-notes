package com.example.composetest

import android.hardware.biometrics.BiometricManager.Authenticators.*
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

enum class LoginState {
    Authenticated,
    Unauthenticated
}

enum class PasswordState {
    NoPasswordSet,
    SavingNewPassword,
    PasswordSet
}

@Composable
fun Login(
    onLogin: (BiometricPrompt.CryptoObject?) -> Unit,
    useCipher: Boolean,
    ivIfDecrypting: ByteArray? = null
) {
    val context = LocalContext.current
    var show: (() -> Unit)? = null

    val executor = ContextCompat.getMainExecutor(context)
    val promptInfo = BiometricPrompt.Builder(context)
        .setTitle("Biometric login for my app")
        .setSubtitle("Log in using your biometric credential")
        // Can't call setNegativeButtonText() and
        // setAllowedAuthenticators(... or DEVICE_CREDENTIAL) at the same time.
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence
        ) {
            super.onAuthenticationError(errorCode, errString)
            Toast.makeText(
                context,
                "Authentication error: $errString", Toast.LENGTH_SHORT
            )
                .show()
            show!!.invoke()
        }

        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            super.onAuthenticationSucceeded(result)
            Toast.makeText(
                context,
                "Authentication succeeded!", Toast.LENGTH_SHORT
            ) .show()
            onLogin(result.cryptoObject)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Toast.makeText(
                context, "Authentication failed",
                Toast.LENGTH_SHORT
            )
                .show()
            show!!.invoke()
        }
    };

    val cancellation = CancellationSignal()
    show =
        if (useCipher) {
            {
                val cipher = getCipher()
                val secretKey = getSecretKey()
                if (ivIfDecrypting == null) {
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                } else {
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ivIfDecrypting))
                }
                promptInfo.authenticate(
                    BiometricPrompt.CryptoObject(cipher),
                    cancellation,
                    executor,
                    callback
                )
            }
        } else {
            {
                generateSecretKeyIfNotExists()
                promptInfo.authenticate(cancellation, executor, callback);
            }
        }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    show()
                }
                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> {
                    cancellation.cancel()
                }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}
