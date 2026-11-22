package sound.gplayeraudio

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import sound.gplayeraudio.model.*
import sound.gplayeraudio.ui.MainContainerScreen
import sound.gplayeraudio.ui.screens.player.AudioPlayerScreen
import sound.gplayeraudio.ui.theme.LocalAppResources
import sound.gplayeraudio.ui.theme.MonochromePlayerTheme
import sound.gplayeraudio.ui.theme.appString
import java.util.Locale

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val appContext = context.applicationContext
            val sharedPrefs = remember(appContext) {
                appContext.getSharedPreferences("player_settings", Context.MODE_PRIVATE)
            }

            var themeMode by remember {
                val savedName = sharedPrefs.getString("theme_mode", AppThemeMode.DARK.name)
                val initialMode = try {
                    AppThemeMode.valueOf(savedName ?: AppThemeMode.DARK.name)
                } catch (e: Exception) {
                    AppThemeMode.DARK
                }
                mutableStateOf(initialMode)
            }
            var playerSliderStyle by remember {
                val savedSlider =
                    sharedPrefs.getString("player_slider_style", PlayerSliderStyle.WAVY.name)
                val initialSlider = try {
                    PlayerSliderStyle.valueOf(savedSlider ?: PlayerSliderStyle.WAVY.name)
                } catch (e: Exception) {
                    PlayerSliderStyle.WAVY
                }
                mutableStateOf(initialSlider)
            }
            var coverShape by remember {
                val savedShape = sharedPrefs.getString("cover_shape", CoverShape.ROUNDED.name)
                val initialShape = try {
                    CoverShape.valueOf(savedShape ?: CoverShape.ROUNDED.name)
                } catch (e: Exception) {
                    CoverShape.ROUNDED
                }
                mutableStateOf(initialShape)
            }
            var rotatePlayerCover by remember {
                mutableStateOf(sharedPrefs.getBoolean("rotate_player_cover", false))
            }

            var appLanguage by remember {
                val savedLang = sharedPrefs.getString("app_language", AppLanguage.RUSSIAN.code)
                val initialLang =
                    AppLanguage.entries.find { it.code == savedLang } ?: AppLanguage.RUSSIAN
                mutableStateOf(initialLang)
            }
            val configuration = LocalConfiguration.current
            val localizedResources = remember(context, configuration, appLanguage) {
                val locale = Locale(appLanguage.code)
                Locale.setDefault(locale)
                val config = Configuration(configuration)
                config.setLocale(locale)
                context.createConfigurationContext(config).resources
            }

            CompositionLocalProvider(LocalAppResources provides localizedResources) {
                MonochromePlayerTheme(themeMode = themeMode) {
                    val viewModel = viewModel<AudioViewModel>()
                    val tracksList by viewModel.tracks.collectAsStateWithLifecycle()

                    var activeTrack by remember { mutableStateOf<AudioTrack?>(null) }
                    var isPlayerExpanded by remember { mutableStateOf(false) }
                    var isPlaying by remember { mutableStateOf(false) }
                    var mediaController by remember { mutableStateOf<MediaController?>(null) }

                    DisposableEffect(Unit) {
                        val sessionToken = SessionToken(
                            context,
                            ComponentName(context, PlaybackService::class.java)
                        )
                        val controllerFuture =
                            MediaController.Builder(context, sessionToken).buildAsync()

                        controllerFuture.addListener({
                            try {
                                val controller = controllerFuture.get()
                                mediaController = controller

                                controller.addListener(object : Player.Listener {
                                    override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                                        isPlaying = isPlayingChanged
                                    }

                                    override fun onMediaItemTransition(
                                        mediaItem: MediaItem?,
                                        reason: Int
                                    ) {
                                        val idx = controller.currentMediaItemIndex
                                        if (idx in tracksList.indices) {
                                            activeTrack = tracksList[idx]
                                        }
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
                    var selectedTab by remember { mutableStateOf(MainTab.LIBRARY) }
                    var hasPermission by remember { mutableStateOf(false) }

                    var isGridView by remember {
                        mutableStateOf(sharedPrefs.getBoolean("is_grid_view", false))
                    }
                    var hideNavOnScroll by remember {
                        mutableStateOf(sharedPrefs.getBoolean("hide_nav_on_scroll", false))
                    }
                    var hidePlayerOnScroll by remember {
                        mutableStateOf(sharedPrefs.getBoolean("hide_player_on_scroll", false))
                    }
                    var navScale by remember {
                        mutableFloatStateOf(sharedPrefs.getFloat("nav_scale", 1.0f))
                    }
                    var showNavLabels by remember {
                        mutableStateOf(sharedPrefs.getBoolean("show_nav_labels", true))
                    }
                    var showTrackCovers by remember {
                        mutableStateOf(sharedPrefs.getBoolean("show_track_covers", true))
                    }
                    var showMiniProgress by remember {
                        mutableStateOf(sharedPrefs.getBoolean("show_mini_progress", true))
                    }
                    var miniMarquee by remember {
                        mutableStateOf(sharedPrefs.getBoolean("mini_marquee", false))
                    }
                    var miniTrackAnim by remember {
                        mutableStateOf(sharedPrefs.getBoolean("mini_track_anim", true))
                    }
                    var showMiniSkipButtons by remember {
                        mutableStateOf(sharedPrefs.getBoolean("show_mini_skip_buttons", false))
                    }
                    var miniPlayerScale by remember {
                        mutableFloatStateOf(sharedPrefs.getFloat("mini_player_scale", 1.0f))
                    }

                    val listState = rememberLazyListState()
                    val gridState = rememberLazyGridState()

                    val requiredPermissions = remember {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }

                    val checkCurrentPermission = remember(appContext, requiredPermissions) {
                        {
                            requiredPermissions.all { perm ->
                                ContextCompat.checkSelfPermission(
                                    appContext, perm
                                ) == PackageManager.PERMISSION_GRANTED
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
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    appString(R.string.permission_required),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(onClick = { permissionLauncher.launch(requiredPermissions) }) {
                                    Text(appString(R.string.grant_permission))
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            MainContainerScreen(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                tracks = tracksList,
                                listState = listState,
                                gridState = gridState,
                                isGridView = isGridView,
                                onViewTypeChanged = { newValue ->
                                    isGridView = newValue
                                    sharedPrefs.edit().putBoolean("is_grid_view", newValue).apply()
                                },
                                onTrackSelected = { track ->
                                    activeTrack = track
                                    isPlayerExpanded = true
                                    mediaController?.let { controller ->
                                        val index = tracksList.indexOf(track).coerceAtLeast(0)
                                        val mediaItems = tracksList.map { MediaItem.fromUri(it.uri) }
                                        controller.setMediaItems(mediaItems)
                                        controller.seekTo(index, 0L)
                                        controller.prepare()
                                        controller.play()
                                    }
                                },
                                themeMode = themeMode,
                                onThemeModeChanged = { newMode ->
                                    themeMode = newMode
                                    sharedPrefs.edit().putString("theme_mode", newMode.name).apply()
                                },
                                activeTrack = activeTrack,
                                isPlaying = isPlaying,
                                onTogglePlayPause = {
                                    mediaController?.let {
                                        if (it.isPlaying) it.pause() else it.play()
                                    }
                                },
                                onExpandPlayer = { isPlayerExpanded = true },
                                onDismissPlayer = {
                                    mediaController?.stop()
                                    mediaController?.clearMediaItems()
                                    activeTrack = null
                                },
                                onNextTrack = { mediaController?.seekToNextMediaItem() },
                                onPreviousTrack = { mediaController?.seekToPreviousMediaItem() },
                                hideNavOnScroll = hideNavOnScroll,
                                onHideNavOnScrollChanged = { newValue ->
                                    hideNavOnScroll = newValue
                                    sharedPrefs.edit().putBoolean("hide_nav_on_scroll", newValue).apply()
                                },
                                hidePlayerOnScroll = hidePlayerOnScroll,
                                onHidePlayerOnScrollChanged = { newValue ->
                                    hidePlayerOnScroll = newValue
                                    sharedPrefs.edit().putBoolean("hide_player_on_scroll", newValue).apply()
                                },
                                navScale = navScale,
                                onNavScaleChanged = { newValue ->
                                    navScale = newValue
                                    sharedPrefs.edit().putFloat("nav_scale", newValue).apply()
                                },
                                showNavLabels = showNavLabels,
                                onShowNavLabelsChanged = { newValue ->
                                    showNavLabels = newValue
                                    sharedPrefs.edit().putBoolean("show_nav_labels", newValue).apply()
                                },
                                mediaController = mediaController,
                                showMiniProgress = showMiniProgress,
                                onShowMiniProgressChanged = { newValue ->
                                    showMiniProgress = newValue
                                    sharedPrefs.edit().putBoolean("show_mini_progress", newValue).apply()
                                },
                                miniMarquee = miniMarquee,
                                onMiniMarqueeChanged = { newValue ->
                                    miniMarquee = newValue
                                    sharedPrefs.edit().putBoolean("mini_marquee", newValue).apply()
                                },
                                miniTrackAnim = miniTrackAnim,
                                onMiniTrackAnimChanged = { newValue ->
                                    miniTrackAnim = newValue
                                    sharedPrefs.edit().putBoolean("mini_track_anim", newValue).apply()
                                },
                                showMiniSkipButtons = showMiniSkipButtons,
                                onShowMiniSkipButtonsChanged = { newValue ->
                                    showMiniSkipButtons = newValue
                                    sharedPrefs.edit().putBoolean("show_mini_skip_buttons", newValue).apply()
                                },
                                miniPlayerScale = miniPlayerScale,
                                onMiniPlayerScaleChanged = { newValue ->
                                    miniPlayerScale = newValue
                                    sharedPrefs.edit().putFloat("mini_player_scale", newValue).apply()
                                },
                                showTrackCovers = showTrackCovers,
                                onShowTrackCoversChanged = { newValue ->
                                    showTrackCovers = newValue
                                    sharedPrefs.edit().putBoolean("show_track_covers", newValue).apply()
                                },
                                playerSliderStyle = playerSliderStyle,
                                onPlayerSliderStyleChanged = { newStyle ->
                                    playerSliderStyle = newStyle
                                    sharedPrefs.edit().putString("player_slider_style", newStyle.name).apply()
                                },
                                appLanguage = appLanguage,
                                onAppLanguageChanged = { newLang ->
                                    appLanguage = newLang
                                    sharedPrefs.edit().putString("app_language", newLang.code).apply()
                                },
                                coverShape = coverShape,
                                onCoverShapeChanged = { newShape ->
                                    coverShape = newShape
                                    sharedPrefs.edit().putString("cover_shape", newShape.name).apply()
                                },
                                rotatePlayerCover = rotatePlayerCover,
                                onRotatePlayerCoverChanged = { newValue ->
                                    rotatePlayerCover = newValue
                                    sharedPrefs.edit().putBoolean("rotate_player_cover", newValue).apply()
                                }
                            )

                            AnimatedVisibility(
                                visible = isPlayerExpanded && activeTrack != null,
                                enter = slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(durationMillis = 350)
                                ),
                                exit = slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight },
                                    animationSpec = tween(durationMillis = 300)
                                ),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                activeTrack?.let { currentTrack ->
                                    AudioPlayerScreen(
                                        tracks = tracksList,
                                        activeTrack = currentTrack,
                                        isPlaying = isPlaying,
                                        coverShape = coverShape,
                                        rotateCover = rotatePlayerCover,
                                        showCover = showTrackCovers,
                                        sliderStyle = playerSliderStyle,
                                        mediaController = mediaController,
                                        onBackToLibrary = { isPlayerExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
