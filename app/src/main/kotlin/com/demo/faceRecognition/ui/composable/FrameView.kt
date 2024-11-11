package com.demo.faceRecognition.ui.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.demo.faceRecognition.R

@Composable
fun FrameView(
    frame: Bitmap,
    modifier: Modifier = Modifier,
    onFlipCamera: (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.BottomEnd, // Aligns children at the bottom end
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
                .fillMaxSize()// Ensures Box fills the available space
    ) {
        // Image will take up all the space
        Image(
            bitmap = frame.asImageBitmap(),
            contentScale = ContentScale.FillBounds,
            contentDescription = "Frame Bitmap",
            modifier = Modifier.fillMaxSize() // Image will cover the whole screen
        )

        // IconButton is placed in the bottom-right corner
        if (onFlipCamera != null) {
            IconButton(onClick = onFlipCamera) {
                Icon(
                    painter = painterResource(R.drawable.ic_flip_camera),
                    contentDescription = "Flip Camera Icon",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}