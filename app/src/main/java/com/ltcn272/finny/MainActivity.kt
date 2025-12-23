package com.ltcn272.finny

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.facebook.CallbackManager
import com.ltcn272.finny.core.Gate
import com.ltcn272.finny.core.OnboardingManager
import com.ltcn272.finny.core.TokenManager
import com.ltcn272.finny.core.navigation.AppNav
import com.ltcn272.finny.core.navigation.Graph
import com.ltcn272.finny.presentation.theme.FinnyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var onboardingManager: OnboardingManager

    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        callbackManager = CallbackManager.Factory.create()

        val gate = Gate(tokenManager, onboardingManager)

        val startRoute = when {
            gate.isFirstLaunch() -> Graph.ONBOARD
            !gate.isLoggedIn() -> Graph.AUTH
            else -> Graph.MAIN
        }

        setContent {
            FinnyTheme {
                AppNav(
                    startRoute = startRoute,
                    callbackManager = callbackManager,
                    onboardingManager
                )
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
