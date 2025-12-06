package com.ltcn272.finny

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.facebook.CallbackManager
import com.ltcn272.finny.core.Gate
import com.ltcn272.finny.core.navigation.AppNav
import com.ltcn272.finny.core.navigation.Graph
import com.ltcn272.finny.presentation.theme.FinnyTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.ltcn272.finny.data.LocaleDataStore
import com.ltcn272.finny.util.LocaleManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        callbackManager = CallbackManager.Factory.create()
        val startRoute = when {
            Gate.isFirstLaunch(this) -> Graph.ONBOARD
            !Gate.isLoggedIn(this) -> Graph.AUTH
            else -> Graph.MAIN
        }

        setContent {
            // collect saved locale from DataStore and apply
            val localeTagState by LocaleDataStore.localeFlow(this@MainActivity).collectAsState(initial = null)
            // apply locale when it changes
            LaunchedEffect(localeTagState) {
                val prev = LocaleManager.applyLocale(this@MainActivity, localeTagState)
                // If using fallback context wrapper, Activity recreation may be needed to fully
                // refresh resources. We'll request recreate to ensure UI updates once.
                // Avoid forcing recreate on first composition unless tag changed.
                // Simple approach: always recreate once when user-specified locale is present.
                // To avoid infinite loop, only recreate when localeTagState is not null.
                if (localeTagState != null) {
                    recreate()
                }
            }

            FinnyTheme {
                AppNav(startRoute = startRoute, callbackManager = callbackManager)
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

}
