package sound.gplayeraudio.ui.components



import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sound.gplayeraudio.model.AudioTrack
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MiniPlayerBar(
    track: AudioTrack,
    isPlaying: Boolean,
    progress: Float = 0f,
    showProgress: Boolean = true,
    useMarquee: Boolean = false,
    animateTrackChange: Boolean = true,
    showSkipButtons: Boolean = false,
    scale: Float = 1.0f,
    enableGestures: Boolean = true,
    showCover: Boolean = true,
    onTogglePlayPause: () -> Unit,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = tween(durationMillis = 150),
        label = "MiniPlayerDragX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = tween(durationMillis = 150),
        label = "MiniPlayerDragY"
    )
    val gestureModifier = if (enableGestures) {
        Modifier
            .offset {
                IntOffset(
                    x = (animatedOffsetX * 0.35f).roundToInt(),
                    y = (animatedOffsetY * 0.35f).roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    },
                    onDragEnd = {
                        val threshold = 60f
                        if (abs(dragOffsetX) > abs(dragOffsetY)) {
                            if (dragOffsetX < -threshold) {
                                onNext()
                            } else if (dragOffsetX > threshold) {
                                onPrevious()
                            }
                        } else {
                            if (dragOffsetY < -threshold) {
                                onClick()
                            } else if (dragOffsetY > threshold) {
                                onDismiss()
                            }
                        }
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    },
                    onDragCancel = {
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                    }
                )
            }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .then(gestureModifier)
            .shadow(elevation = 12.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = (12 * scale).dp, vertical = (8 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrackCoverImage(
                albumId = track.albumId,
                showCover = showCover,
                modifier = Modifier
                    .size((40 * scale).dp)
                    .clip(CircleShape),
                iconSize = (20 * scale).dp
            )

            Spacer(modifier = Modifier.width((12 * scale).dp))

            Box(modifier = Modifier.weight(1f)) {
                if (animateTrackChange) {
                    AnimatedContent(
                        targetState = track,
                        transitionSpec = {
                            (slideInHorizontally(tween(250)) { it / 2 } + fadeIn(tween(250)))
                                .togetherWith(slideOutHorizontally(tween(200)) { -it / 2 } + fadeOut(
                                    tween(200)
                                ))
                        },
                        label = "MiniPlayerTrackAnim"
                    ) { currentTrack ->
                        MiniPlayerTrackInfo(
                            track = currentTrack,
                            useMarquee = useMarquee,
                            scale = scale
                        )
                    }
                } else {
                    MiniPlayerTrackInfo(
                        track = track,
                        useMarquee = useMarquee,
                        scale = scale
                    )
                }
            }

            Spacer(modifier = Modifier.width((8 * scale).dp))

            if (showSkipButtons) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size((36 * scale).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Предыдущий трек",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size((22 * scale).dp)
                    )
                }
            }

            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size((40 * scale).dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Пауза / Воспроизведение",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size((24 * scale).dp)
                )
            }

            if (showSkipButtons) {
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size((36 * scale).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Следующий трек",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size((22 * scale).dp)
                    )
                }
            }
        }

        if (showProgress) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height((2.5f * scale).dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun MiniPlayerTrackInfo(
    track: AudioTrack,
    useMarquee: Boolean,
    scale: Float
) {
    val textModifier = if (useMarquee) {
        Modifier.basicMarquee()
    } else {
        Modifier
    }

    Column {
        Text(
            text = track.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = (14 * scale).sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = if (useMarquee) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = textModifier
        )
        Text(
            text = track.artist,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = (12 * scale).sp,
            maxLines = 1,
            overflow = if (useMarquee) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = textModifier
        )
    }
}
