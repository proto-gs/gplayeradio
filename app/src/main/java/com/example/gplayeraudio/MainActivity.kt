package com.example.gplayeraudio

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import android.content.ComponentName
import com.google.common.util.concurrent.MoreExecutors

val PureBlackScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF242424),
    onPrimaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFB0B0B0)
)

@Composable
fun MonochromePlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PureBlackScheme,
        content = content
    )
}


data class AudioTrack(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val duration: Long,
    val albumId: Long
)

class MainActivity : ComponentActivity() {
    @kotlin.OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MonochromePlayerTheme {
                val context = LocalContext.current
                val appContext = context.applicationContext
                val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<AudioViewModel>()
                val tracksList by viewModel.tracks.collectAsStateWithLifecycle()

                val sharedPrefs = remember(appContext) {
                    appContext.getSharedPreferences("player_settings", Context.MODE_PRIVATE)
                }

                var currentTrack by remember { mutableStateOf<AudioTrack?>(null) }
                var hasPermission by remember { mutableStateOf(false) }


                var isGridView by remember {
                    mutableStateOf(sharedPrefs.getBoolean("is_grid_view", false))
                }

                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()


                val requiredPermissions = remember {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_AUDIO,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                    } else {
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }


                val checkCurrentPermission = remember(appContext, requiredPermissions) {
                    {
                        requiredPermissions.all { perm ->
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                appContext, perm
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        }
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissionsMap ->
                    hasPermission = permissionsMap.values.all { it }
                }

                LaunchedEffect(hasPermission) {
                    if (checkCurrentPermission()) {
                        hasPermission = true
                        viewModel.loadTracks(appContext)
                    } else {
                        permissionLauncher.launch(requiredPermissions)
                    }
                }

                if (!hasPermission) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Приложению нужен доступ к аудиофайлам", color = Color.White, modifier = Modifier.padding(16.dp))
                            Button(onClick = { permissionLauncher.launch(requiredPermissions) }) {
                                Text("Предоставить разрешение")
                            }
                        }
                    }
                } else if (currentTrack == null) {
                    MediaLibraryScreen(
                        hasPermission = hasPermission,
                        listState = listState,
                        gridState = gridState,
                        isGridView = isGridView,
                        onViewTypeChanged = { newValue ->
                            isGridView = newValue
                            sharedPrefs.edit().putBoolean("is_grid_view", newValue).apply()
                        },
                        onTrackSelected = { track -> currentTrack = track },
                        onRequestPermissionAgain = {
                            permissionLauncher.launch(requiredPermissions)
                        }
                    )
                } else {
                    val currentIndex = tracksList.indexOf(currentTrack)

                    AudioPlayerScreen(
                        tracks = tracksList,
                        initialIndex = if (currentIndex != -1) currentIndex else 0,
                        onBackToLibrary = { currentTrack = null }
                    )
                }

            }
        }
    }
}

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
    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

    context.contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
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

@ExperimentalMaterial3Api
@Composable
fun MediaLibraryScreen(
    hasPermission: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    isGridView: Boolean,
    onViewTypeChanged: (Boolean) -> Unit,
    onTrackSelected: (AudioTrack) -> Unit,
    onRequestPermissionAgain: () -> Unit
) {
    val context = LocalContext.current
    var tracks by remember { mutableStateOf(listOf<AudioTrack>()) }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            tracks = fetchLocalAudioTracks(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Медиатека", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = { onViewTypeChanged(!isGridView) }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Сменить вид",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Музыка на устройстве не найдена", color = Color.Gray)
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tracks) { track ->
                        TrackGridItem(track = track, onClick = { onTrackSelected(track) })
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tracks) { track ->
                        TrackListItem(track = track, onClick = { onTrackSelected(track) })
                    }
                }
            }
        }
    }
}

@Composable
fun TrackListItem(track: AudioTrack, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121212))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF242424)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, color = Color.Gray, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun TrackGridItem(track: AudioTrack, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF121212))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.aspectRatio(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF242424)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(track.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayerScreen(
    tracks: List<AudioTrack>,
    initialIndex: Int,
    onBackToLibrary: () -> Unit
) {
    val context = LocalContext.current

    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    val currentTrack = tracks.getOrNull(currentIndex)


    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    androidx.activity.compose.BackHandler(enabled = true) {
        onBackToLibrary()
    }


    DisposableEffect(Unit) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                mediaController = controller


                if (currentTrack != null) {
                    val mediaItems = tracks.map { MediaItem.fromUri(it.uri) }
                    controller.setMediaItems(mediaItems)
                    controller.seekTo(currentIndex, 0L)
                    controller.prepare()
                    controller.play()
                }


                controller.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                        isPlaying = isPlayingChanged
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            duration = controller.duration.coerceAtLeast(0L)
                        }
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {

                        currentIndex = controller.currentMediaItemIndex
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())

        onDispose {
            mediaController?.release()
        }
    }


    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaController?.let {
                playbackPosition = it.currentPosition
            }
            delay(500)
        }
    }

    val coverScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.85f,
        animationSpec = tween(durationMillis = 400),
        label = "CoverScaleAnimation"
    )

    val buttonCornerPercent by animateDpAsState(
        targetValue = if (isPlaying) 24.dp else 50.dp,
        animationSpec = tween(durationMillis = 300),
        label = "ButtonShapeAnimation"
    )

    var totalDragX by remember { mutableStateOf(0f) }

    if (currentTrack == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(all = 24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDragEnd = {
                        if (totalDragX > 80f) {
                            onBackToLibrary()
                        }
                    },
                    onDragCancel = { totalDragX = 0f },
                    onDrag = { change, dragAmount ->
                        totalDragX += dragAmount.x
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopStart) {
            IconButton(onClick = onBackToLibrary) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(coverScale)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize().padding(64.dp))
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(currentTrack.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Text(currentTrack.artist, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Spacer(modifier = Modifier.height(50.dp))


        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = if (duration > 0) playbackPosition.toFloat() / duration else 0f,
                onValueChange = { percent ->
                    val targetPosition = (percent * duration).toLong()
                    mediaController?.seekTo(targetPosition)
                    playbackPosition = targetPosition
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(playbackPosition), color = Color.Gray, fontSize = 12.sp)
                Text(text = formatTime(duration), color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { mediaController?.seekToPreviousMediaItem() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.FastRewind, contentDescription = "Назад", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.width(32.dp))

            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(buttonCornerPercent))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        mediaController?.let {
                            if (it.isPlaying) it.pause() else it.play()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Воспроизведение",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            IconButton(
                onClick = { mediaController?.seekToNextMediaItem() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.FastForward, contentDescription = "Вперед", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}




