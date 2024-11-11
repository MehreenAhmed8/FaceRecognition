package com.demo.faceRecognition.ui.screen.recogniseFace

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.faceRecognition.data.Repository
import com.demo.faceRecognition.data.model.ProcessedImage
import com.demo.faceRecognition.lib.AiModel.mobileNet
import com.demo.faceRecognition.lib.AiModel.recognizeFace
import com.demo.faceRecognition.lib.LOG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecogniseFaceViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    lateinit var imageAnalysis: ImageAnalysis
    lateinit var lifecycleOwner: LifecycleOwner
    lateinit var snackbarHost: SnackbarHostState
    val cameraProvider: ProcessCameraProvider by lazy { repo.cameraProviderFuture.get() }

    var images: List<ProcessedImage> = listOf()
    val image: MutableState<ProcessedImage> = mutableStateOf(ProcessedImage())
    var recognizedFace: MutableState<ProcessedImage?> = mutableStateOf(null)
    val lensFacing: MutableState<Int> = mutableStateOf(CameraSelector.LENS_FACING_FRONT)
    val cameraSelector get(): CameraSelector = repo.cameraSelector(lensFacing.value)
    val paint = Paint().apply {
        strokeWidth = 3f
        color = Color.BLUE
    }

    var showSaveDialog = mutableStateOf(false)

    fun showSaveDialogOnce() {
        if (!showSaveDialog.value) {
            showSaveDialog.value=true
        }
    }
    fun hideSaveDialog() {
        showSaveDialog.value = false
    }
    val Context.getImageAnalysis
        get() = repo.imageAnalysis(lensFacing.value, paint) { result ->
            runCatching {
                val data = result.getOrNull() ?: return@runCatching
                data.landmarks = data.face?.allLandmarks ?: listOf()
                image.value = data
                recognizedFace.value = recognizeFace(data, images)
                recognizedFace.value = recognizedFace.value?.copy(spoof = mobileNet(data).getOrNull())

            }.onFailure { LOG.e(it, it.message) }
        }

    fun onCompose(context: Context, owner: LifecycleOwner, snackbar: SnackbarHostState) = viewModelScope.launch {
        runCatching {
            snackbarHost = snackbar
            if (showSaveDialog.value) return@runCatching
            lifecycleOwner = owner
            imageAnalysis = context.getImageAnalysis
            images = withContext(Dispatchers.IO) { repo.faceList().map { it.processedImage(context) } }
            bindCamera()
        }.onFailure { LOG.e(it, it.message) }
    }

    fun onDispose() = runCatching {
        cameraProvider.unbindAll()
        LOG.d("Recognise Face Screen Disposed")
    }.onFailure { LOG.e(it, it.message) }

    fun onFlipCamera() = runCatching {
        lensFacing.value = if (lensFacing.value == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        bindCamera()  // Rebind the camera with the updated lensFacing
        LOG.d("Camera Flipped lensFacing\t:\t${lensFacing.value}")
    }.onFailure { LOG.e(it, it.message) }

    fun bindCamera() = runCatching {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
        LOG.d("Camera is bound to lifecycle.")
    }.onFailure { LOG.e(it, it.message) }

    fun onNameChange(value: String) = runCatching { image.value = image.value.copy(name = value) }.onFailure { LOG.e(it, it.message) }

    fun saveFace(context: Context) = viewModelScope.launch {
        runCatching {
            hideSaveDialog()
            val error = withContext(Dispatchers.IO) { repo.saveFace(image.value).exceptionOrNull() }
            images = withContext(Dispatchers.IO) { repo.faceList().map { it.processedImage(context) } }
            recognizeFace(image.value, images)
        }.onFailure { LOG.e(it, it.message) }
    }
    private fun recognizeFace(currentFace: ProcessedImage?, savedFaces: List<ProcessedImage>) {
        currentFace?.let {
            Log.d("llmdd","saved faces: $it")

            savedFaces.forEach { savedFace ->
                Log.d("llmdd","saved faces: $savedFace")
                if (savedFace.matchesCriteria && savedFace.faceSignature == currentFace.faceSignature) {
                    recognizedFace.value = savedFace // Set recognized face if it matches
                    return
                }
            }
        }
    }
}
