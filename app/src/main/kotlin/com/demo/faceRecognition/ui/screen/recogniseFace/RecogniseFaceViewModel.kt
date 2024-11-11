package com.demo.faceRecognition.ui.screen.recogniseFace

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
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
    val showSaveDialog: MutableState<Boolean> = mutableStateOf(false)
    var images: List<ProcessedImage> = listOf()
    val image: MutableState<ProcessedImage> = mutableStateOf(ProcessedImage())
    val showDialog: MutableState<Boolean> = mutableStateOf(false)
    var recognizedFace: MutableState<ProcessedImage?> = mutableStateOf(null)
    val lensFacing: MutableState<Int> = mutableStateOf(CameraSelector.LENS_FACING_FRONT)
    val cameraSelector get(): CameraSelector = repo.cameraSelector(lensFacing.value)
    val paint = Paint().apply {
        strokeWidth = 3f
        color = Color.BLUE
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
            lifecycleOwner = owner
            imageAnalysis = context.getImageAnalysis
            images = withContext(Dispatchers.IO) { repo.faceList().map { it.processedImage(context) } }
            bindCamera()
           // delay(1000)
            bindCamera()
            LOG.d("Recognise Face Screen Composed")
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

    fun showDialog() = runCatching {
        showDialog.value = true
        cameraProvider.unbindAll()
    }.onFailure { LOG.e(it, it.message) }

    fun hideDialog() = runCatching {
        showDialog.value = false
        bindCamera()
    }.onFailure { LOG.e(it, it.message) }

    fun showSaveDialog() = runCatching {
        showSaveDialog.value = true
        cameraProvider.unbindAll()
    }.onFailure { LOG.e(it, it.message) }

    fun hideSaveDialog() = runCatching { showSaveDialog.value = false }.onFailure { LOG.e(it, it.message) }

    fun onNameChange(value: String) = runCatching { image.value = image.value.copy(name = value) }.onFailure { LOG.e(it, it.message) }

    fun saveFace() = viewModelScope.launch {
        runCatching {
            hideSaveDialog()
            val error = withContext(Dispatchers.IO) { repo.saveFace(image.value).exceptionOrNull() }
            snackbarHost.showSnackbar(error?.message ?: "Face Saved Successfully")
        }.onFailure { LOG.e(it, it.message) }
    }

}
