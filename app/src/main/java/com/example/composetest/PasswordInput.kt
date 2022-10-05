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

//const val minPasswordLength = 7
//
//enum class InvalidPasswordReason {
//    Empty,
//    TooShort,
//    NoDigit,
//    NoLetter,
//    ContainsWhitespace,
//    NoUppercase,
//    NoLowercase
//}

//fun validatePassword(password: String): InvalidPasswordReason? {
//    if (password.isEmpty()) return InvalidPasswordReason.Empty
//    if (password.length < minPasswordLength) return InvalidPasswordReason.TooShort
//    var numLetters = 0
//    var numUppercaseLetters = 0
//    var numDigits = 0
//    for (char in password) {
//        if (char.isLetter()) numLetters += 1
//        if (char.isUpperCase()) numUppercaseLetters += 1
//        if (char.isDigit()) numDigits += 1
//        if (char.isWhitespace()) return InvalidPasswordReason.ContainsWhitespace
//    }
//    return when {
//        numLetters == 0 -> InvalidPasswordReason.NoLetter
//        numDigits == 0 -> InvalidPasswordReason.NoDigit
//        numUppercaseLetters == 0 -> InvalidPasswordReason.NoUppercase
//        numUppercaseLetters == numLetters -> InvalidPasswordReason.NoLowercase
//        else -> null
//    }
//}

enum class LoginState {
    Authenticated,
    Unauthenticated
}


enum class PasswordState {
    NoPasswordSet,
    SavingNewPassword,
    PasswordSet
}

//fun whyPasswordInvalid(pir: InvalidPasswordReason?): String {
//    return when (pir) {
//        InvalidPasswordReason.Empty -> "Empty"
//        InvalidPasswordReason.TooShort -> "Too short"
//        InvalidPasswordReason.NoDigit -> "No digit"
//        InvalidPasswordReason.NoLetter -> "No letter"
//        InvalidPasswordReason.ContainsWhitespace -> "Contains whitespace"
//        InvalidPasswordReason.NoUppercase -> "No uppercase letter"
//        InvalidPasswordReason.NoLowercase -> "No lowercase letter"
//        else -> "Good password"
//    }
//}

//fun giveHint(pir: InvalidPasswordReason?, state: PasswordState, matches: Boolean?): String =
//    when (state) {
//        PasswordState.NoPasswordSet -> when (pir) {
//            InvalidPasswordReason.Empty -> "Create your new password"
//            null -> "Set this as your new password. Will you remember it?"
//            else -> whyPasswordInvalid(pir)
//        }
//        PasswordState.PasswordSet -> when (pir) {
//            InvalidPasswordReason.Empty -> "Insert the password you created"
//            else -> if (matches == false || matches == null)
//                whyPasswordInvalid(
//                    pir
//                )
//            else "Password accepted"
//        }
//        else -> "Unknown"
//    }

//fun checkPassword(possiblePassword: String, context: Context, password: Password? = null): Boolean {
//    val password = password ?: loadPassword(PreferenceManager.getDefaultSharedPreferences(context))
//    val hashedPossiblePassword =
//        hashPassword(possiblePassword, DEFAULT_HASHING_ALGO, context)
//    return hashedPossiblePassword == password.hashedPassword
//}
//fun checkPassword(possiblePassword: BiometricPrompt.CryptoObject, context: Context, password: Password? = null): Boolean {
//    val password = password ?: loadPassword(PreferenceManager.getDefaultSharedPreferences(context))
//    return possiblePassword.signature.parameters.encoded == password.hashedPassword
//}

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
//        .setNegativeButton(
//            "Only strong biometric allowed",
//            executor
//        ) { _di, _x -> show?.let { it() } }
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()
//    var success by remember {mutableStateOf(false)}

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

//        DisposableEffect(Unit) {
//        onDispose {
//            cancellation.cancel()
//        }
//    }



}

//
//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//fun Login(
//    onLogin: (String) -> Unit,
//    passwordState: PasswordState,
//    requestFocus: Boolean = false,
//    giveHintFunc: (pir: InvalidPasswordReason?, state: PasswordState, matches: Boolean?) -> String=::giveHint
//) {
//    var password by remember { mutableStateOf(TextFieldValue()) }
//    var showPassword by remember { mutableStateOf(false) }
//    val passwordInvalidityReason by remember { derivedStateOf { validatePassword(password.text) } }
//    val context = LocalContext.current
//
//    val promptInfo = BiometricPrompt.Builder(context)
//        .setTitle("Biometric login for my app")
//        .setSubtitle("Log in using your biometric credential")
//        // Can't call setNegativeButtonText() and
//        // setAllowedAuthenticators(... or DEVICE_CREDENTIAL) at the same time.
//        // .setNegativeButtonText("Use account password")
//        .setAllowedAuthenticators(BIOMETRIC_STRONG)
//        .build()
//    promptInfo.
//
//
//    Column(
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Row(Modifier.padding(horizontal = 30.dp), verticalAlignment = Alignment.CenterVertically) {
//            val focusRequester = remember { if (requestFocus) FocusRequester() else null }
//            if (requestFocus) {
//                LaunchedEffect(Unit) {
//                    focusRequester!!.requestFocus()
//                }
//            }
//
//            val baseModifier = Modifier.weight(1f)
//            val keyboardController = LocalSoftwareKeyboardController.current
//
//            TextField(
//                // Use BasicTextField to fix uneven padding
//                value = password,
//                modifier = if (!requestFocus) baseModifier else baseModifier
//                    .focusRequester(
//                        focusRequester!!
//                    )
//                    .onFocusChanged {
//                        if (it.isFocused) {
//                            keyboardController?.show()
//                        }
//                    },
//                onValueChange = {
//                    password = it
//                },
//                isError = passwordInvalidityReason != null && passwordInvalidityReason != InvalidPasswordReason.Empty,
//                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
//                singleLine = true,
//                enabled = true,
//                label = {
//                    Text(
//                        giveHintFunc(passwordInvalidityReason, passwordState, null),
//                        style = if (passwordInvalidityReason == null) LocalTextStyle.current.copy(
//                            color = MaterialTheme.colors.secondary
//                        ) else LocalTextStyle.current
//                    )
//                },
//                textStyle = TextStyle(fontSize = 10.em),
//                keyboardOptions = KeyboardOptions(
//                    autoCorrect = false,
//                    imeAction = ImeAction.Done,
//                    capitalization = KeyboardCapitalization.None,
//                    keyboardType = KeyboardType.Password
//                ),
//                keyboardActions = KeyboardActions(
//                    onDone = { if (passwordInvalidityReason == null) onLogin(password.text) }
//                ),
//                colors = TextFieldDefaults.textFieldColors(
//                    cursorColor = MaterialTheme.colors.secondary,
//                    textColor = MaterialTheme.colors.primary,
//                    backgroundColor = MaterialTheme.colors.background,
//                    errorCursorColor = Color.Red,
//                    errorLabelColor = Color.Red,
//
//                    errorIndicatorColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent,
//                    disabledIndicatorColor = Color.Transparent
//                ),
//            )
//
//            Button(
//                onClick = { showPassword = !showPassword },
//                elevation = null,
//                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
//            ) {
//                Icon(
//                    if (showPassword) Icons.TwoTone.VisibilityOff else Icons.TwoTone.Visibility,
//                    "",
//                    tint = MaterialTheme.colors.primary,
//                    modifier = Modifier.padding(10.dp)
//                )
//            }
//        }
//    }
//}
