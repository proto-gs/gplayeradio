package sound.gplayeraudio.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sound.gplayeraudio.R
import sound.gplayeraudio.ui.theme.appString
import androidx.compose.ui.platform.LocalUriHandler
import sound.gplayeraudio.BuildConfig
@ExperimentalMaterial3Api
@Composable
fun AboutAppScreen(
    onBack: () -> Unit
) {
    BackHandler(enabled = true) {
        onBack()
    }

    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    val headerBgColor = MaterialTheme.colorScheme.primary
    val headerContentColor = MaterialTheme.colorScheme.onPrimary
    val bottomBgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bottomBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .background(headerBgColor)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = appString(R.string.back),
                            tint = headerContentColor
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(headerContentColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = headerContentColor,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "gplayeraudio",
                            color = headerContentColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = appString(R.string.about_version),
                            color = headerContentColor.copy(alpha = 0.75f),
                            fontSize = 14.sp
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            text = appString(R.string.settings_about),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = appString(R.string.about_description),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        if (BuildConfig.SUPPORT_URL.isNotBlank()) {
                            try {
                                uriHandler.openUri(BuildConfig.SUPPORT_URL)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appString(R.string.about_support_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = appString(R.string.about_support_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
