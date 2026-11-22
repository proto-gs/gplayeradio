package sound.gplayeraudio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sound.gplayeraudio.model.MainTab
import sound.gplayeraudio.ui.theme.appString


@Composable
fun FloatingBottomIsland(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    scale: Float = 1.0f,
    showLabels: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .padding((6 * scale).dp),
        horizontalArrangement = Arrangement.spacedBy((6 * scale).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(tab) }
                    .padding(
                        horizontal = (24 * scale).dp,
                        vertical = ((if (showLabels) 10f else 12f) * scale).dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                    contentDescription = appString(tab.titleRes),
                    tint = contentColor,
                    modifier = Modifier.size((22 * scale).dp)
                )
                if (showLabels) {
                    Spacer(modifier = Modifier.height((4 * scale).dp))
                    Text(
                        text = appString(tab.titleRes),
                        color = contentColor,
                        fontSize = (11 * scale).sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
