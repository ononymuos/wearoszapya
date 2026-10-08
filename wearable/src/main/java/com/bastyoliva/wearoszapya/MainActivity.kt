package com.bastyoliva.wearoszapya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.bastyoliva.wearable.design.theme.WearOsZapyaTheme
import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearoszapya.navigation.NavigationGraph
import com.bastyoliva.wearoszapya.navigation.NavigationHandler
import com.bastyoliva.wearoszapya.presentation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.viewState.value.isLoading
        }

        setContent {
            val state = viewModel.viewState.collectAsState().value

            WearOsZapyaTheme(seedColor = state.themeColor) {

                AppScaffold {
                    val navController = rememberSwipeDismissableNavController()

                    NavigationHandler(
                        navigator = navigator,
                        navController = navController
                    )

                    NavigationGraph(
                        navController = navController
                    )
                }

            }
        }
    }

}
