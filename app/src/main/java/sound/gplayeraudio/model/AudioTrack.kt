package sound.gplayeraudio.model


import android.net.Uri

data class AudioTrack(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumId: Long
)
