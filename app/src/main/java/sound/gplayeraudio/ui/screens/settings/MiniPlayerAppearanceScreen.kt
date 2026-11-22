package sound.gplayeraudio.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sound.gplayeraudio.R
import sound.gplayeraudio.model.AudioTrack
import sound.gplayeraudio.ui.components.MiniPlayerBar
import sound.gplayeraudio.ui.theme.appString
import kotlin.math.roundToInt

@ExperimentalMaterial3Api
@Composable
fun MiniPlayerAppearanceScreen(
    showProgress: Boolean,
    onShowProgressChanged: (Boolean) -> Unit,
    useMarquee: Boolean,
    onUseMarqueeChanged: (Boolean) -> Unit,
    animateTrackChange: Boolean,
    onAnimateTrackChangeChanged: (Boolean) -> Unit,
    showSkipButtons: Boolean,
    onShowSkipButtonsChanged: (Boolean) -> Unit,
    scale: Float,
    onScaleChanged: (Float) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) {
        onBack()
    }

    var previewIndex by remember { mutableIntStateOf(0) }
    var previewPlaying by remember { mutableStateOf(true) }

    val track1Title = appString(R.string.preview_track_1_title)
    val track1Artist = appString(R.string.preview_track_1_artist)
    val track2Title = appString(R.string.preview_track_2_title)
    val track2Artist = appString(R.string.preview_track_2_artist)

    val sampleTracks = remember(track1Title, track1Artist, track2Title, track2Artist) {
        listOf(
            AudioTrack(1L, Uri.EMPTY, track1Title, track1Artist, 180000L, 1L),
            AudioTrack(2L, Uri.EMPTY, track2Title, track2Artist, 210000L, 1L)
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appString(R.string.settings_mini_player_appearance),
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MiniPlayerBar(
                        track = sampleTracks[previewIndex],
                        isPlaying = previewPlaying,
                        progress = 0.65f,
                        showProgress = showProgress,
                        useMarquee = useMarquee,
                        animateTrackChange = animateTrackChange,
                        showSkipButtons = showSkipButtons,
                        scale = scale,
                        enableGestures = false,
                        onTogglePlayPause = { previewPlaying = !previewPlaying },
                        onClick = { previewIndex = (previewIndex + 1) % sampleTracks.size },
                        onDismiss = {},
                        onNext = { previewIndex = (previewIndex + 1) % sampleTracks.size },
                        onPrevious = { previewIndex = (previewIndex + 1) % sampleTracks.size }
                    )
                }
            }

            item {
                MiniPlayerSwitchRow(
                    title = appString(R.string.mini_progress),
                    subtitle = appString(R.string.mini_progress_subtitle),
                    checked = showProgress,
                    onCheckedChange = onShowProgressChanged
                )
            }

            item {
                MiniPlayerSwitchRow(
                    title = appString(R.string.mini_marquee),
                    subtitle = appString(R.string.mini_marquee_subtitle),
                    checked = useMarquee,
                    onCheckedChange = onUseMarqueeChanged
                )
            }

            item {
                MiniPlayerSwitchRow(
                    title = appString(R.string.mini_track_anim),
                    subtitle = appString(R.string.mini_track_anim_subtitle),
                    checked = animateTrackChange,
                    onCheckedChange = onAnimateTrackChangeChanged
                )
            }

            item {
                MiniPlayerSwitchRow(
                    title = appString(R.string.mini_skip_buttons),
                    subtitle = appString(R.string.mini_skip_buttons_subtitle),
                    checked = showSkipButtons,
                    onCheckedChange = onShowSkipButtonsChanged
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = appString(R.string.mini_player_size),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${(scale * 100).roundToInt()}%",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = scale,
                        onValueChange = { rawValue ->
                            val stepped = (rawValue * 20).roundToInt() / 20f
                            onScaleChanged(stepped)
                        },
                        valueRange = 0.75f..1.15f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}