package it.polito.mad.cookbookcommunity.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import it.polito.mad.cookbookcommunity.model.profile.ProfileImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

private const val PROFILE_IMAGE_DATA_URI_PREFIX = "data:image/jpeg;base64,"
private const val PROFILE_IMAGE_MAX_SIZE = 256
private const val PROFILE_IMAGE_JPEG_QUALITY = 72

fun String.isSharedProfileImageDataUri(): Boolean {
    return startsWith(PROFILE_IMAGE_DATA_URI_PREFIX)
}

fun ProfileImage.needsSharedProfileImageConversion(): Boolean {
    return isLocalUri &&
            value.startsWith("content://") &&
            !value.isSharedProfileImageDataUri()
}

suspend fun Context.toSharedProfileImageDataUri(uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        val sourceBitmap = contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return@withContext null

        val resizedBitmap = sourceBitmap.resizeKeepingAspectRatio(PROFILE_IMAGE_MAX_SIZE)
        val output = ByteArrayOutputStream()

        resizedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            PROFILE_IMAGE_JPEG_QUALITY,
            output
        )

        PROFILE_IMAGE_DATA_URI_PREFIX +
                Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }
}

private fun Bitmap.resizeKeepingAspectRatio(maxSize: Int): Bitmap {
    val maxDimension = maxOf(width, height)

    if (maxDimension <= maxSize) {
        return this
    }

    val scale = maxSize.toFloat() / maxDimension.toFloat()
    val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
    val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)

    return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
}

@Composable
fun SharedProfileImage(
    value: String,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit
) {
    if (value.isSharedProfileImageDataUri()) {
        val imageBitmap = remember(value) {
            runCatching {
                val encodedImage = value.substringAfter("base64,")
                val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        }

        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        } else {
            fallback()
        }
    } else {
        var loadFailed by remember(value) {
            mutableStateOf(false)
        }

        if (loadFailed || value.isBlank()) {
            fallback()
        } else {
            AsyncImage(
                model = value,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier,
                onError = {
                    loadFailed = true
                }
            )
        }
    }
}