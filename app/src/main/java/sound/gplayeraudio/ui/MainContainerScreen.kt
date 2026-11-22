package sound.gplayeraudio.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController
import kotlinx.coroutines.delay
import sound.gplayeraudio.model.*
import sound.gplayeraudio.ui.components.FloatingBottomIsland
import sound.gplayeraudio.ui.components.MiniPlayerBar
import sound.gplayeraudio.ui.screens.library.MediaLibraryScreen
import sound.gplayeraudio.ui.screens.settings.*
import kotlin.math.abs

@ExperimentalMaterial3Api
@Composable
fun MainContainerScreen(
    selectedTab: MainTab,
    coverShape: CoverShape,
    onCoverShapeChanged: (CoverShape) -> Unit,
    rotatePlayerCover: Boolean,
    onRotatePlayerCoverChanged: (Boolean) -> Unit,
    playerSliderStyle: PlayerSliderStyle,
    onPlayerSliderStyleChanged: (PlayerSliderStyle) -> Unit,
    onTabSelected: (MainTab) -> Unit,
    tracks: List<AudioTrack>,
    listState: LazyListState,
    gridState: LazyGridState,
    isGridView: Boolean,
    onViewTypeChanged: (Boolean) -> Unit,
    onTrackSelected: (AudioTrack) -> Unit,
    appLanguage: AppLanguage,
    onAppLanguageChanged: (AppLanguage) -> Unit,
    themeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    hideNavOnScroll: Boolean,
    onHideNavOnScrollChanged: (Boolean) -> Unit,
    hidePlayerOnScroll: Boolean,
    onHidePlayerOnScrollChanged: (Boolean) -> Unit,
    navScale: Float,
    onNavScaleChanged: (Float) -> Unit,
    showNavLabels: Boolean,
    onShowNavLabelsChanged: (Boolean) -> Unit,
    showTrackCovers: Boolean,
    onShowTrackCoversChanged: (Boolean) -> Unit,
    activeTrack: AudioTrack?,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onExpandPlayer: () -> Unit,
    onDismissPlayer: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    mediaController: MediaController?,
    showMiniProgress: Boolean,
    onShowMiniProgressChanged: (Boolean) -> Unit,
    miniMarquee: Boolean,
    onMiniMarqueeChanged: (Boolean) -> Unit,
    miniTrackAnim: Boolean,
    onMiniTrackAnimChanged: (Boolean) -> Unit,
    showMiniSkipButtons: Boolean,
    onShowMiniSkipButtonsChanged: (Boolean) -> Unit,
    miniPlayerScale: Float,
    onMiniPlayerScaleChanged: (Float) -> Unit,
) {
    var isThemeScreenOpen by remember { mutableStateOf(false) }
    var isLanguageScreenOpen by remember { mutableStateOf(false) }
    var isAboutScreenOpen by remember { mutableStateOf(false) }
    var isLibraryAppearanceOpen by remember { mutableStateOf(false) }
    var isPlayerAppearanceOpen by remember { mutableStateOf(false) }
    var isMiniPlayerAppearanceOpen by remember { mutableStateOf(false) }

    var isScrollingDown by remember { mutableStateOf(false) }

    val currentIndex = if (isGridView) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
    val currentOffset = if (isGridView) gridState.firstVisibleItemScrollOffset else listState.firstVisibleItemScrollOffset

    var prevIndex by remember { mutableIntStateOf(currentIndex) }
    var prevOffset by remember { mutableIntStateOf(currentOffset) }
    var miniProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying, activeTrack, showMiniProgress) {
        if (!showMiniProgress || activeTrack == null) {
            miniProgress = 0f
            return@LaunchedEffect
        }
        while (true) {
            mediaController?.let { controller ->
                val dur = controller.duration.takeIf { it > 0 } ?: activeTrack.duration
                if (dur > 0) {
                    miniProgress = (controller.currentPosition.toFloat() / dur).coerceIn(0f, 1f)
                }
            }
            if (!isPlaying) break
            delay(300)
        }
    }

    LaunchedEffect(currentIndex, currentOffset, selectedTab) {
        if (selectedTab != MainTab.LIBRARY) {
            isScrollingDown = false
            return@LaunchedEffect
        }
        if (currentIndex != prevIndex) {
            isScrollingDown = currentIndex > prevIndex
        } else if (currentOffset != prevOffset) {
            if (abs(currentOffset - prevOffset) > 10) {
                isScrollingDown = currentOffset > prevOffset
            }
        }
        prevIndex = currentIndex
        prevOffset = currentOffset
    }

    val isAtTop = currentIndex == 0 && currentOffset == 0
    val shouldHideNav = hideNavOnScroll && isScrollingDown && !isAtTop && selectedTab == MainTab.LIBRARY
    val shouldHidePlayer = hidePlayerOnScroll && isScrollingDown && !isAtTop && selectedTab == MainTab.LIBRARY
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally(animationSpec = tween(250)) { width -> width / 3 } + fadeIn(tween(250)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(250)) { width -> -width / 3 } + fadeOut(tween(200)))
                } else {
                    (slideInHorizontally(animationSpec = tween(250)) { width -> -width / 3 } + fadeIn(tween(250)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(250)) { width -> width / 3 } + fadeOut(tween(200)))
                }
            },
            label = "TabTransitionAnimation"
        ) { currentTab ->
            when (currentTab) {
                MainTab.LIBRARY -> {
                    MediaLibraryScreen(
                        tracks = tracks,
                        activeTrack = activeTrack,
                        hasPermission = true,
                        listState = listState,
                        gridState = gridState,
                        isGridView = isGridView,
                        showTrackCovers = showTrackCovers,
                        onViewTypeChanged = onViewTypeChanged,
                        onTrackSelected = onTrackSelected,
                        onRequestPermissionAgain = {}
                    )
                }

                MainTab.SETTINGS -> {
                    SettingsScreen(
                        currentTheme = themeMode,
                        currentLanguage = appLanguage,
                        onOpenThemeSettings = { isThemeScreenOpen = true },
                        onOpenLanguageSettings = { isLanguageScreenOpen = true },
                        onOpenPlayerAppearance = { isPlayerAppearanceOpen = true },
                        onOpenMiniPlayerAppearance = { isMiniPlayerAppearanceOpen = true },
                        onOpenLibraryAppearance = { isLibraryAppearanceOpen = true },
                        onOpenAbout = { isAboutScreenOpen = true }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedVisibility(
                visible = activeTrack != null && !shouldHidePlayer,
                enter = slideInVertically(animationSpec = tween(250)) { it * 2 } + fadeIn(tween(200)),
                exit = slideOutVertically(animationSpec = tween(250)) { it * 2 } + fadeOut(tween(200))
            ) {
                if (activeTrack != null) {
                    MiniPlayerBar(
                        track = activeTrack,
                        isPlaying = isPlaying,
                        progress = miniProgress,
                        showProgress = showMiniProgress,
                        useMarquee = miniMarquee,
                        animateTrackChange = miniTrackAnim,
                        showSkipButtons = showMiniSkipButtons,
                        scale = miniPlayerScale,
                        showCover = showTrackCovers,
                        onTogglePlayPause = onTogglePlayPause,
                        onClick = onExpandPlayer,
                        onDismiss = onDismissPlayer,
                        onNext = onNextTrack,
                        onPrevious = onPreviousTrack
                    )
                }
            }

            AnimatedVisibility(
                visible = !shouldHideNav,
                enter = slideInVertically(animationSpec = tween(250)) { it * 2 } + fadeIn(tween(200)),
                exit = slideOutVertically(animationSpec = tween(250)) { it * 2 } + fadeOut(tween(200))
            ) {
                FloatingBottomIsland(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    scale = navScale,
                    showLabels = showNavLabels
                )
            }
        }

        AnimatedVisibility(
            visible = isThemeScreenOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            ThemeSelectionScreen(
                currentTheme = themeMode,
                onThemeSelected = { onThemeModeChanged(it) },
                onBack = { isThemeScreenOpen = false }
            )
        }

        AnimatedVisibility(
            visible = isLibraryAppearanceOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            LibraryAppearanceScreen(
                hideNavOnScroll = hideNavOnScroll,
                onHideNavOnScrollChanged = onHideNavOnScrollChanged,
                hidePlayerOnScroll = hidePlayerOnScroll,
                onHidePlayerOnScrollChanged = onHidePlayerOnScrollChanged,
                navScale = navScale,
                onNavScaleChanged = onNavScaleChanged,
                showNavLabels = showNavLabels,
                onShowNavLabelsChanged = onShowNavLabelsChanged,
                showTrackCovers = showTrackCovers,
                onShowTrackCoversChanged = onShowTrackCoversChanged,
                onBack = { isLibraryAppearanceOpen = false }
            )
        }

        AnimatedVisibility(
            visible = isMiniPlayerAppearanceOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            MiniPlayerAppearanceScreen(
                showProgress = showMiniProgress,
                onShowProgressChanged = onShowMiniProgressChanged,
                useMarquee = miniMarquee,
                onUseMarqueeChanged = onMiniMarqueeChanged,
                animateTrackChange = miniTrackAnim,
                onAnimateTrackChangeChanged = onMiniTrackAnimChanged,
                showSkipButtons = showMiniSkipButtons,
                onShowSkipButtonsChanged = onShowMiniSkipButtonsChanged,
                scale = miniPlayerScale,
                onScaleChanged = onMiniPlayerScaleChanged,
                onBack = { isMiniPlayerAppearanceOpen = false }
            )
        }

        AnimatedVisibility(
            visible = isPlayerAppearanceOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerAppearanceScreen(
                currentStyle = playerSliderStyle,
                onStyleSelected = onPlayerSliderStyleChanged,
                currentShape = coverShape,
                onShapeSelected = onCoverShapeChanged,
                rotateCover = rotatePlayerCover,
                onRotateCoverChanged = onRotatePlayerCoverChanged,
                appLanguage = appLanguage,
                onBack = { isPlayerAppearanceOpen = false }
            )
        }

        AnimatedVisibility(
            visible = isLanguageScreenOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            LanguageSelectionScreen(
                currentLanguage = appLanguage,
                onLanguageSelected = { onAppLanguageChanged(it) },
                onBack = { isLanguageScreenOpen = false }
            )
        }

        AnimatedVisibility(
            visible = isAboutScreenOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            AboutAppScreen(
                onBack = { isAboutScreenOpen = false }
            )
        }
    }
}
