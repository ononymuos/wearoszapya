package com.bastyoliva.wearoszapya

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bastyoliva.wearoszapya.data.ConnectionStatus
import com.bastyoliva.wearoszapya.data.TransferRepository
import com.bastyoliva.wearoszapya.data.WearNode
import com.bastyoliva.wearoszapya.presentation.AboutScreen
import com.bastyoliva.wearoszapya.presentation.DonationScreen
import com.bastyoliva.wearoszapya.presentation.MainViewModel
import com.bastyoliva.wearoszapya.presentation.NodeSelectionPager
import com.bastyoliva.wearoszapya.presentation.NotificationPermissionRequest
import com.bastyoliva.wearoszapya.presentation.Queue
import com.bastyoliva.wearoszapya.ui.theme.WearOsZapyaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            WearOsZapyaTheme {
                val navController = rememberNavController()
                NavHost(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    navController = navController,
                    startDestination = "main",
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
                ) {
                    composable("main") {
                        MainScreen(
                            availableNodes = TransferRepository.availableNodes,
                            selectedNodeId = TransferRepository.selectedNodeId,
                            sharedUris = viewModel.sharedUris,
                            onNodeSelected = { viewModel.onNodeSelected(it) },
                            onMenuClick = { navController.navigate("about") },
                            onBoostClick = { navController.navigate("donation") }
                        )
                    }
                    composable("about") {
                        AboutScreen(
                            onBackClick = { navController.popBackStack() },
                            onDonateClick = { navController.navigate("donation") }
                        )
                    }
                    composable("donation") {
                        DonationScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
                viewModel.updateSharedUris(listOfNotNull(uri))
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                }
                viewModel.updateSharedUris(uris ?: emptyList())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    availableNodes: List<WearNode>,
    selectedNodeId: String?,
    sharedUris: List<Uri>,
    onNodeSelected: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onBoostClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val fileSender = remember { WearableFileSender(context) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            fileSender.sendFileToWear(uri)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.mobile_app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        scrolledContainerColor = Color.Transparent,
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        // Boost Turbo Info Button
                        IconButton(onClick = onBoostClick) {
                            Icon(
                                painter = painterResource(R.drawable.ic_bolt),
                                contentDescription = "Turbo Boost",
                                tint = Color(0xFFFFB300)
                            )
                        }

                        IconButton(onClick = onMenuClick) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = stringResource(R.string.menu_content_description)
                            )
                        }
                    }
                )

                NodeSelectionPager(
                    modifier = Modifier.padding(bottom = 8.dp),
                    nodes = availableNodes,
                    selectedNodeId = selectedNodeId,
                    onNodeSelected = onNodeSelected
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            val selectedNode = availableNodes.find { it.id == selectedNodeId }
            val isFabVisible = selectedNode?.status == ConnectionStatus.READY

            AnimatedVisibility(
                isFabVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExtendedFloatingActionButton(
                        onClick = { launcher.launch("*/*") },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .navigationBarsPadding()
                            .padding(vertical = 12.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_upload),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.select_files))
                    }
                }
            }
        },
    ) { contentPadding ->
        NotificationPermissionRequest()
        Queue(
            contentPadding = contentPadding,
            initialUris = sharedUris
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    WearOsZapyaTheme {
        MainScreen(
            availableNodes = emptyList(),
            selectedNodeId = "1",
            sharedUris = emptyList(),
            onNodeSelected = {}
        )
    }
}
