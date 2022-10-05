package com.example.composetest

import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Visibility
import androidx.compose.material.icons.twotone.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.composetest.ui.theme.ComposetestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {
    var delaySeconds = -1L
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val context = LocalContext.current

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var loginState by remember {
        mutableStateOf(LoginState.Unauthenticated)
    }

    val onPasswordInputDone = { _: BiometricPrompt.CryptoObject? ->
        loginState = LoginState.Authenticated
    }

    val widthPx = with(density) { config.screenWidthDp.dp.roundToPx() }

    ComposetestTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
            AnimatedVisibility(
                visible = loginState == LoginState.Unauthenticated,
                enter = slideInHorizontally(spring()) { w -> -widthPx / 2 - w },
                exit = slideOutHorizontally(spring()) { w -> +widthPx / 2 + w },
            ) {
                Login(onLogin = onPasswordInputDone, useCipher = false, ivIfDecrypting = null)
            }

            AnimatedVisibility(
                visible = loginState == LoginState.Authenticated,
                enter = slideInHorizontally(spring()) { w -> -widthPx / 2 - w },
                exit = slideOutHorizontally(spring()) { w -> +widthPx / 2 + w },
            ) {
                NotesContainer()
            }
        }
    }
}
