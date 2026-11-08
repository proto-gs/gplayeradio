package sound.gplayeraudio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AudioViewModel : ViewModel() {
    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks: StateFlow<List<AudioTrack>> = _tracks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    fun loadTracks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val fetchedTracks = fetchLocalAudioTracks(context)
            _tracks.value = fetchedTracks
            _isLoading.value = false
        }
    }
}


