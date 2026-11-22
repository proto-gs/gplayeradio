package sound.gplayeraudio.ui.screens.settings



import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sound.gplayeraudio.R
import sound.gplayeraudio.model.AppLanguage
import sound.gplayeraudio.model.CoverShape
import sound.gplayeraudio.model.PlayerSliderStyle
import sound.gplayeraudio.ui.components.MaterialCoverShape
import sound.gplayeraudio.ui.components.PlayerProgressSlider
import sound.gplayeraudio.ui.theme.appString

@ExperimentalMaterial3Api
@Composable
fun PlayerAppearanceScreen(
    currentStyle: PlayerSliderStyle,
    onStyleSelected: (PlayerSliderStyle) -> Unit,
    currentShape: CoverShape,
    onShapeSelected: (CoverShape) -> Unit,
    rotateCover: Boolean,
    onRotateCoverChanged: (Boolean) -> Unit,
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) {
        onBack()
    }

    var previewProgress by remember { mutableFloatStateOf(0.45f) }
    var previewRotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(rotateCover) {
        if (rotateCover) {
            var lastTime = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                val dt = (now - lastTime) / 1_000_000_000f
                lastTime = now
                previewRotation = (previewRotation + 24f * dt) % 360f
            }
        } else {
            previewRotation = 0f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appString(R.string.settings_player_appearance),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer {
                            rotationZ = if (rotateCover) previewRotation else 0f
                        }
                        .clip(MaterialCoverShape(currentShape))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PlayerProgressSlider(
                    value = previewProgress,
                    onValueChange = { previewProgress = it },
                    style = currentStyle,
                    isPlaying = true
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRotateCoverChanged(!rotateCover) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = if (appLanguage == AppLanguage.RUSSIAN) "Вращение обложки" else "Rotate cover art",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (appLanguage == AppLanguage.RUSSIAN) "Плавно крутить обложку во время воспроизведения" else "Smoothly spin cover art during playback",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                Switch(
                    checked = rotateCover,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            Text(
                text = if (appLanguage == AppLanguage.RUSSIAN) "Форма обложки" else "Cover shape",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(CoverShape.entries) { shape ->
                    val isSelected = shape == currentShape
                    val cardBg = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                    val borderMod = if (isSelected) {
                        Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    }

                    Column(
                        modifier = Modifier
                            .width(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .then(borderMod)
                            .clickable { onShapeSelected(shape) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(MaterialCoverShape(shape))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = shape.getTitle(appLanguage),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Text(
                text = appString(R.string.player_slider_style_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp)
            )

            PlayerSliderStyle.entries.forEach { style ->
                val isSelected = style == currentStyle

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStyleSelected(style) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appString(style.titleRes),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = appString(style.subtitleRes),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
