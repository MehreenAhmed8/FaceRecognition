package com.demo.faceRecognition.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.demo.faceRecognition.R
import com.demo.faceRecognition.data.model.AppState
import com.demo.faceRecognition.ui.screen.recogniseFace.RecogniseFaceScreen
import com.demo.faceRecognition.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp


object MAIN {
    @HiltAndroidApp
    class HiltApp : Application()

    @AndroidEntryPoint
    class AppActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setContent { AppContent(this) }  // Pass `Activity` context to `AppContent`
        }
    }

    @Composable
    fun AppContent(activity: Activity) = AppTheme(dynamicColors = true, statusBar = true) {
        val hostController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val appState = AppState(
            activity = activity,
            scope = coroutineScope,
            host = hostController,
            snackbar = snackbarHostState
        )

        var showWelcomeScreen by remember { mutableStateOf(true) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showWelcomeScreen) {
                WelcomeScreen(onNavigateToHome = { showWelcomeScreen = false })
            } else {
                //   AppHost(Routes.Home.path)
                RecogniseFaceScreen(appState = appState)
            }
        }
    }


    @Composable
    fun WelcomeScreen(onNavigateToHome: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.my_img)
                    .decoderFactory(ImageDecoderDecoder.Factory())
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = "Loading GIF",
                modifier = Modifier.size(500.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "Start Your Recognition Journey")
            }
        }
    }
}



