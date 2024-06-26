package com.bstudio.composestarted

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bstudio.composestarted.navigation.BottomNavItem
import com.bstudio.composestarted.navigation.Destinations
import com.bstudio.composestarted.navigation.MyBottomNavigation
import com.bstudio.composestarted.navigation.NavigationGraph
import com.bstudio.composestarted.ui.theme.ComposeStartedTheme
import com.bstudio.composestarted.ui.theme.StartedTheme
import com.bstudio.composestarted.util.SharePrefManager
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject


class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(2)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        val scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                {
                    if (result.resultCode == RESULT_OK) {
                        val result =
                            GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                        result?.getPages()?.let { pages ->
                            for (page in pages) {
                                val imageUri = pages.get(0).getImageUri()
                            }
                        }
                        result?.getPdf()?.let { pdf ->
                            val pdfUri = pdf.getUri()
                            val pageCount = pdf.getPageCount()
                        }
                    }
                }
            }

        scanner.getStartScanIntent(this)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener {
                Log.e("binh", it.toString())
            }

        setContent {
            val viewModel: MainActivityViewModel = koinViewModel()
            ComposeStartedTheme(startedTheme = viewModel.themeState) {
                val navController = rememberNavController()
                Scaffold(bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    if (navBackStackEntry?.destination?.route in arrayListOf(
                            Destinations.HomeScreen.route,
                            BottomNavItem.Home.screen_route,
                            BottomNavItem.AddPost.screen_route,
                            BottomNavItem.Settings.screen_route
                        )
                    ) {
                        MyBottomNavigation(navController)
                    }
                }) {
                    NavigationGraph(navHostController = navController)
                }

            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeStartedTheme(startedTheme = StartedTheme.DARK) {
        Greeting("Android")
    }
}


@Composable
fun SecondScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Second Screen",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
    }
}

@Composable
fun AddPostScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Add Post Screen",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
    }
}


@Composable
fun NotificationScreen() {
    val sharePrefManager: SharePrefManager = koinInject()
    val currentThem = StartedTheme.valueOf(
        sharePrefManager.getString(
            SharePrefManager.THEME_KEY,
            StartedTheme.LIGHT.name
        )
    )
    var enableDarkMode by remember { mutableStateOf(currentThem == StartedTheme.DARK) }
    LaunchedEffect(enableDarkMode) {
        sharePrefManager.putString(
            SharePrefManager.THEME_KEY,
            if (enableDarkMode) StartedTheme.DARK.name else StartedTheme.LIGHT.name
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = "Enable dark mode: ")
            Switch(checked = enableDarkMode, onCheckedChange = {
                enableDarkMode = it
            })
        }
    }
}

