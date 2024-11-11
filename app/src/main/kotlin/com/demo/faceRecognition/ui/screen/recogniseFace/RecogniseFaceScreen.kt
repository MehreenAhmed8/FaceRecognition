package com.demo.faceRecognition.ui.screen.recogniseFace

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import com.demo.faceRecognition.data.model.AppState
import com.demo.faceRecognition.data.model.ProcessedImage
import com.demo.faceRecognition.ui.composable.FrameView
import com.demo.faceRecognition.ui.theme.spacing
import kotlinx.coroutines.delay


@Composable
fun RecogniseFaceScreen(appState: AppState, vm: RecogniseFaceViewModel = hiltViewModel()) {
    val currentFace: ProcessedImage by vm.image
    val recognizedFace: ProcessedImage? by vm.recognizedFace
    val showSaveDialog by vm.showSaveDialog

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        showText = true
    }
    LaunchedEffect(showSaveDialog) {
        if (!showSaveDialog) {
            vm.bindCamera()
        }
    }

    DisposableEffect(vm.lensFacing, showSaveDialog) {
        vm.onCompose(appState.activity, lifecycleOwner, appState.snackbar)
        onDispose {
            vm.onDispose()
            Log.d("RecogniseFaceScreen", "Camera resources disposed")
        }
    }

    val content: @Composable (PaddingValues) -> Unit = { padding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            currentFace.frame?.let {
                FrameView(
                    frame = it,
                    onFlipCamera = vm::onFlipCamera,
                    modifier = Modifier.weight(0.5f)
                )
            }
            if (showText) {
                Text(
                    text = if (recognizedFace?.matchesCriteria == true) {
                        "Hello ${recognizedFace?.name}"
                    } else if (currentFace.face == null) {
                        "No Face Detected"
                    } else {
                        "Tap to save your face"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp)
                        .clickable {
                            if (recognizedFace?.matchesCriteria != true && currentFace.face != null) {
                                vm.showSaveDialogOnce()
                            }
                        }
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = contentColorFor(MaterialTheme.colorScheme.background),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = content
    )

    if (showSaveDialog) {
        val context = LocalContext.current
        SaveDialog(
            currentFace,
            onValueChange = vm::onNameChange,
            onCancel = vm::hideSaveDialog,
            onSave = { vm.saveFace(context) }
        )
    }
}

@Composable
private fun SaveDialog(
    value: ProcessedImage,
    modifier: Modifier = Modifier,
    title: String = "Save Your Face",
    placeholder: String = "Face Name",
    positiveBtnText: String = "Save",
    negativeBtnText: String = "Cancel",
    properties: DialogProperties = DialogProperties(),
    content: (@Composable () -> Unit)? = null,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    val newContent = content ?: {
        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(MaterialTheme.spacing.Small)
        ) {
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.Normal)
            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.Small),
                )
                value.faceBitmap?.asImageBitmap()?.let {
                    Image(
                        it,
                        contentDescription = null,
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(250.dp)
                    )
                }
                OutlinedTextField(
                    value = value.name,
                    label = { Text(text = placeholder) },
                    placeholder = { Text(text = placeholder) },
                    onValueChange = onValueChange, modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.Small)
                )
            }
            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                Text(
                    text = negativeBtnText.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(vertical = MaterialTheme.spacing.Small)
                        .clickable(onClick = onCancel)
                        .weight(1f)
                )
                val isValid = value.name.length > 3
                val saveColor =
                    if (isValid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                Text(
                    text = positiveBtnText.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(saveColor)
                        .padding(vertical = MaterialTheme.spacing.Small)
                        .clickable(enabled = isValid, onClick = onSave)
                        .weight(1f)
                )
            }
        }
    }
    Dialog(onCancel, properties, newContent)
}
