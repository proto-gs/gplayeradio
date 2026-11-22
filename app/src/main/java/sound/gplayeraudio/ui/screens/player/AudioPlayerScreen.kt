package sound.gplayeraudio.ui.screens.player



import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import kotlinx.coroutines.delay
import sound.gplayeraudio.R
import sound.gplayeraudio.model.AudioTrack
import sound.gplayeraudio.model.CoverShape
import sound.gplayeraudio.model.PlayerSliderStyle
import sound.gplayeraudio.ui.components.MaterialCoverShape
import sound.gplayeraudio.ui.components.PlayerProgressSlider
import sound.gplayeraudio.ui.components.TrackCoverImage
import sound.gplayeraudio.ui.theme.appString
import sound.gplayeraudio.util.formatTime
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayerScreen(
    tracks: List<AudioTrack>,
    coverShape: CoverShape = CoverShape.ROUNDED,
    rotateCover: Boolean = false,
    activeTrack: AudioTrack,
    isPlaying: Boolean,
    showCover: Boolean = true,
    mediaController: MediaController?,
    sliderStyle: PlayerSliderStyle = PlayerSliderStyle.WAVY,
    onBackToLibrary: () -> Unit
) {
    var playbackPosition by remember { mutableLongStateOf(mediaController?.currentPosition ?: 0L) }
    var duration by remember { mutableLongStateOf(mediaController?.duration?.coerceAtLeast(0L) ?: activeTrack.duration) }

    var repeatMode by remember {
        mutableIntStateOf(mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF)
    }
    val speedOptions = remember { listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f) }
    var playbackSpeed by remember {
        mutableFloatStateOf(mediaController?.playbackParameters?.speed ?: 1.0f)
    }
    var isSpeedMenuExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        onBackToLibrary()
    }

    var currentRotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying, rotateCover) {
        if (isPlaying && rotateCover) {
            var lastFrameTime = withFrameNanos { it }
            while (true) {
                val frameTime = withFrameNanos { it }
                val deltaSeconds = (frameTime - lastFrameTime) / 1_000_000_000f
                lastFrameTime = frameTime
                currentRotation = (currentRotation + 18f * deltaSeconds) % 360f
            }
        }
    }

    LaunchedEffect(isPlaying, activeTrack) {
        while (true) {
            mediaController?.let {
                playbackPosition = it.currentPosition
                if (it.duration > 0) {
                    duration = it.duration
                }
                repeatMode = it.repeatMode
                playbackSpeed = it.playbackParameters.speed
            }
            if (!isPlaying) break
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

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedDragY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = tween(durationMillis = 150),
        label = "PlayerSwipeDownAnimation"
    )

    var coverDragX by remember { mutableFloatStateOf(0f) }
    val animatedCoverDragX by animateFloatAsState(
        targetValue = coverDragX,
        animationSpec = tween(durationMillis = 150),
        label = "CoverSwipeTrackAnimation"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(x = 0, y = animatedDragY.roundToInt().coerceAtLeast(0)) }
            .background(MaterialTheme.colorScheme.background)
            .padding(all = 24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragOffsetY = 0f },
                    onDragEnd = {
                        if (dragOffsetY > 120f) {
                            onBackToLibrary()
                        }
                        dragOffsetY = 0f
                    },
                    onDragCancel = { dragOffsetY = 0f },
                    onDrag = { change, dragAmount ->
                        if (dragAmount.y > 0f || dragOffsetY > 0f) {
                            change.consume()
                            dragOffsetY = (dragOffsetY + dragAmount.y).coerceAtLeast(0f)
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToLibrary) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Box {
                IconButton(onClick = { isSpeedMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Скорость воспроизведения",
                        tint = if (playbackSpeed != 1.0f) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                }

                DropdownMenu(
                    expanded = isSpeedMenuExpanded,
                    onDismissRequest = { isSpeedMenuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    speedOptions.forEach { speed ->
                        val isSelected = abs(playbackSpeed - speed) < 0.05f
                        val label = when (speed) {
                            1.0f -> appString(R.string.speed_normal)
                            else -> "${speed}x"
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Выбрано",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            onClick = {
                                playbackSpeed = speed
                                mediaController?.setPlaybackSpeed(speed)
                                isSpeedMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .offset { IntOffset(x = (animatedCoverDragX * 0.4f).roundToInt(), y = 0) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { coverDragX = 0f },
                        onDragEnd = {
                            val threshold = 70f
                            if (coverDragX < -threshold) {
                                mediaController?.seekToNextMediaItem()
                            } else if (coverDragX > threshold) {
                                mediaController?.seekToPreviousMediaItem()
                            }
                            coverDragX = 0f
                        },
                        onDragCancel = { coverDragX = 0f },
                        onDrag = { change, dragAmount ->
                            if (abs(dragAmount.x) > abs(dragAmount.y)) {
                                change.consume()
                                coverDragX += dragAmount.x
                            }
                        }
                    )
                }
        ) {
            TrackCoverImage(
                albumId = activeTrack.albumId,
                showCover = showCover,
                modifier = Modifier
                    .size(300.dp)
                    .scale(coverScale)
                    .graphicsLayer {
                        rotationZ = if (rotateCover) currentRotation else 0f
                    }
                    .clip(MaterialCoverShape(coverShape)),
                iconSize = 110.dp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                activeTrack.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                activeTrack.artist,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(50.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            PlayerProgressSlider(
                value = if (duration > 0) playbackPosition.toFloat() / duration else 0f,
                onValueChange = { percent ->
                    val targetPosition = (percent * duration).toLong()
                    mediaController?.seekTo(targetPosition)
                    playbackPosition = targetPosition
                },
                style = sliderStyle,
                isPlaying = isPlaying
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(playbackPosition),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    text = formatTime(duration),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.size(44.dp))

            IconButton(
                onClick = { mediaController?.seekToPreviousMediaItem() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.FastRewind,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

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

            IconButton(
                onClick = { mediaController?.seekToNextMediaItem() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.FastForward,
                    contentDescription = "Вперед",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = {
                    val nextMode = when (repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                        else -> Player.REPEAT_MODE_OFF
                    }
                    repeatMode = nextMode
                    mediaController?.repeatMode = nextMode
                },
                modifier = Modifier.size(44.dp)
            ) {
                val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) {
                    Icons.Default.RepeatOne
                } else {
                    Icons.Default.Repeat
                }
                val repeatTint = if (repeatMode != Player.REPEAT_MODE_OFF) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = "Повтор",
                    tint = repeatTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
