package sound.gplayeraudio.ui.screens.library


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sound.gplayeraudio.R
import sound.gplayeraudio.model.AudioTrack
import sound.gplayeraudio.ui.components.TrackCoverImage
import sound.gplayeraudio.ui.theme.appString
import sound.gplayeraudio.util.formatTime

@ExperimentalMaterial3Api
@Composable
fun MediaLibraryScreen(
    tracks: List<AudioTrack>,
    activeTrack: AudioTrack? = null,
    hasPermission: Boolean,
    listState: LazyListState,
    gridState: LazyGridState,
    isGridView: Boolean,
    showTrackCovers: Boolean = true,
    onViewTypeChanged: (Boolean) -> Unit,
    onTrackSelected: (AudioTrack) -> Unit,
    onRequestPermissionAgain: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        appString(R.string.tab_library),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { onViewTypeChanged(!isGridView) }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Сменить вид",
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
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    appString(R.string.no_music_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tracks) { track ->
                        TrackGridItem(
                            track = track,
                            isCurrent = activeTrack?.id == track.id,
                            showCover = showTrackCovers,
                            onClick = { onTrackSelected(track) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tracks) { track ->
                        TrackListItem(
                            track = track,
                            isCurrent = activeTrack?.id == track.id,
                            showCover = showTrackCovers,
                            onClick = { onTrackSelected(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrackListItem(
    track: AudioTrack,
    onClick: () -> Unit,
    isCurrent: Boolean = false,
    showCover: Boolean = true
) {
    val containerColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderModifier = if (isCurrent) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackCoverImage(
            albumId = track.albumId,
            showCover = showCover,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            iconSize = 24.dp
        )

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCurrent) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Сейчас играет",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                }
                Text(
                    text = track.title,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = formatTime(track.duration),
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun TrackGridItem(
    track: AudioTrack,
    onClick: () -> Unit,
    isCurrent: Boolean = false,
    showCover: Boolean = true
) {
    val containerColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderModifier = if (isCurrent) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TrackCoverImage(
            albumId = track.albumId,
            showCover = showCover,
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            iconSize = 40.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = track.title,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (isCurrent) "Играет • ${formatTime(track.duration)}" else track.artist,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
