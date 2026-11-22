package sound.gplayeraudio.data


import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import org.json.JSONArray
import org.json.JSONObject
import sound.gplayeraudio.model.AudioTrack

fun fetchLocalAudioTracks(context: Context): List<AudioTrack> {
    val tracks = mutableListOf<AudioTrack>()
    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ID
    )

    val selection = "${MediaStore.Audio.Media.DURATION} >= 10000"
    val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"


    context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val title = cursor.getString(titleColumn) ?: "Неизвестный трек"
            val artist = cursor.getString(artistColumn) ?: "Неизвестный исполнитель"
            val duration = cursor.getLong(durationColumn)
            val albumId = cursor.getLong(albumIdColumn)
            val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

            tracks.add(AudioTrack(id, contentUri, title, artist, duration, albumId))
        }
    }
    return tracks
}

fun saveTracksToPrefs(prefs: SharedPreferences, tracks: List<AudioTrack>) {
    val jsonArray = JSONArray()
    tracks.forEach { track ->
        val obj = JSONObject().apply {
            put("id", track.id)
            put("uri", track.uri.toString())
            put("title", track.title)
            put("artist", track.artist)
            put("duration", track.duration)
            put("albumId", track.albumId)
        }
        jsonArray.put(obj)
    }
    prefs.edit().putString("saved_tracks_list", jsonArray.toString()).apply()
}

fun loadTracksFromPrefs(prefs: SharedPreferences): List<AudioTrack> {
    val jsonString = prefs.getString("saved_tracks_list", null) ?: return emptyList()
    val result = mutableListOf<AudioTrack>()
    try {
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            result.add(
                AudioTrack(
                    id = obj.getLong("id"),
                    uri = Uri.parse(obj.getString("uri")),
                    title = obj.getString("title"),
                    artist = obj.getString("artist"),
                    duration = obj.getLong("duration"),
                    albumId = obj.getLong("albumId")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}
