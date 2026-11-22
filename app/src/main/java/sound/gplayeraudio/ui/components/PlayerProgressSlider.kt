package sound.gplayeraudio.ui.components



import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import sound.gplayeraudio.model.PlayerSliderStyle
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun PlayerProgressSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    style: PlayerSliderStyle,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    when (style) {
        PlayerSliderStyle.CLASSIC -> {
            Slider(
                value = value.coerceIn(0f, 1f),
                onValueChange = onValueChange,
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = inactiveColor
                ),
                modifier = modifier.fillMaxWidth()
            )
        }

        PlayerSliderStyle.WAVY -> {
            val infiniteTransition = rememberInfiniteTransition(label = "WavePhase")
            val phase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = (2f * PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "WavePhaseValue"
            )

            val currentPhase = if (isPlaying) phase else 0f

            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onValueChange(newProgress)
                        }
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                            onValueChange(newProgress)
                        }
                    }
            ) {
                val width = size.width
                val centerY = size.height / 2f
                val strokeWidth = 3.5.dp.toPx()
                val thumbHalfHeight = 9.dp.toPx()
                val thumbGap = 6.dp.toPx()

                val progressX = width * value.coerceIn(0f, 1f)

                val inactiveStartX = (progressX + thumbGap).coerceAtMost(width)
                if (inactiveStartX < width) {
                    drawLine(
                        color = inactiveColor,
                        start = Offset(inactiveStartX, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                val waveEndX = (progressX - thumbGap).coerceAtLeast(0f)
                if (waveEndX > 0f) {
                    val path = Path()
                    val amplitude = if (isPlaying) 3.2.dp.toPx() else 2.5.dp.toPx()
                    val wavelength = 36.dp.toPx()
                    val frequency = (2f * PI / wavelength).toFloat()

                    path.moveTo(0f, centerY + amplitude * sin(currentPhase))
                    var x = 2f
                    while (x <= waveEndX) {
                        val y = centerY + amplitude * sin(x * frequency + currentPhase)
                        path.lineTo(x, y)
                        x += 2f
                    }
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                drawLine(
                    color = primaryColor,
                    start = Offset(progressX, centerY - thumbHalfHeight),
                    end = Offset(progressX, centerY + thumbHalfHeight),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        PlayerSliderStyle.MINIMAL -> {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onValueChange(newProgress)
                        }
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                            onValueChange(newProgress)
                        }
                    }
            ) {
                val width = size.width
                val centerY = size.height / 2f
                val trackHeight = 3.dp.toPx()
                val progressX = width * value.coerceIn(0f, 1f)

                drawLine(
                    color = inactiveColor,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(0f, centerY),
                    end = Offset(progressX, centerY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = primaryColor,
                    radius = 5.5.dp.toPx(),
                    center = Offset(progressX, centerY)
                )
            }
        }
    }
}
