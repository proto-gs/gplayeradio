package sound.gplayeraudio.ui.screens.settings



import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sound.gplayeraudio.R
import sound.gplayeraudio.model.MainTab
import sound.gplayeraudio.ui.components.FloatingBottomIsland
import sound.gplayeraudio.ui.theme.appString
import kotlin.math.roundToInt

@ExperimentalMaterial3Api
@Composable
fun LibraryAppearanceScreen(
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
    onBack: () -> Unit
) {
    BackHandler(enabled = true) {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appString(R.string.settings_library_appearance),
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
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHideNavOnScrollChanged(!hideNavOnScroll) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appString(R.string.hide_nav_on_scroll),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )

                Switch(
                    checked = hideNavOnScroll,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHidePlayerOnScrollChanged(!hidePlayerOnScroll) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = appString(R.string.hide_player_on_scroll),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = appString(R.string.hide_player_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                Switch(
                    checked = hidePlayerOnScroll,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowTrackCoversChanged(!showTrackCovers) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = appString(R.string.show_track_covers),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = appString(R.string.show_track_covers_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                Switch(
                    checked = showTrackCovers,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowNavLabelsChanged(!showNavLabels) }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appString(R.string.show_nav_labels),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )

                Switch(
                    checked = showNavLabels,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appString(R.string.nav_size),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${(navScale * 100).roundToInt()}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = navScale,
                    onValueChange = { rawValue ->
                        val stepped = (rawValue * 20).roundToInt() / 20f
                        onNavScaleChanged(stepped)
                    },
                    valueRange = 0.70f..1.15f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingBottomIsland(
                    selectedTab = MainTab.LIBRARY,
                    onTabSelected = {},
                    scale = navScale,
                    showLabels = showNavLabels
                )
            }
        }
    }
}
