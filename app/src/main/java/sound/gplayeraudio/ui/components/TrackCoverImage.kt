package sound.gplayeraudio.ui.components


import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TrackCoverImage(
    albumId: Long,
    showCover: Boolean,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp
) {
    val context = LocalContext.current
    var bitmap by remember(albumId, showCover) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(albumId, showCover) {
        if (!showCover || albumId <= 0L) {
            bitmap = null
            return@LaunchedEffect
        }
        bitmap = withContext(Dispatchers.IO) {
            try {
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
                context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (showCover && bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = "Обложка трека",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
