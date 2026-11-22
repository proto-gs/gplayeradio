package sound.gplayeraudio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sound.gplayeraudio.data.fetchLocalAudioTracks
import sound.gplayeraudio.data.loadTracksFromPrefs
import sound.gplayeraudio.data.saveTracksToPrefs
import sound.gplayeraudio.model.AudioTrack

class AudioViewModel : ViewModel() {
    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks: StateFlow<List<AudioTrack>> = _tracks.asStateFlow()

    fun loadTracks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("player_settings", Context.MODE_PRIVATE)


            val savedTracks = loadTracksFromPrefs(prefs)
            if (savedTracks.isNotEmpty()) {
                _tracks.value = savedTracks
            }


            val scannedTracks = fetchLocalAudioTracks(context)
            if (scannedTracks != savedTracks) {
                saveTracksToPrefs(prefs, scannedTracks)
                _tracks.value = scannedTracks
            }
        }
    }
}




