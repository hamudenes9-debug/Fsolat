package com.hamudenes.fsolat.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hamudenes.fsolat.ui.screens.HomeScreen
import com.hamudenes.fsolat.ui.screens.SettingsScreen
import com.hamudenes.fsolat.ui.screens.StatsScreen
import com.hamudenes.fsolat.ui.theme.mono

enum class Tab(val label: Int, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME(com.hamudenes.fsolat.R.string.tab_home, Icons.Outlined.Home, Icons.Rounded.Home),
    STATS(com.hamudenes.fsolat.R.string.tab_stats, Icons.Outlined.BarChart, Icons.Rounded.BarChart),
    SETTINGS(com.hamudenes.fsolat.R.string.tab_settings, Icons.Outlined.Settings, Icons.Rounded.Settings);

    val index: Int get() = entries.indexOf(this)
}

@Composable
fun FsolatApp(
    viewModel: MainViewModel,
    onRequestNotificationPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Surface(color = mono.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 24 }) togetherWith
                            (fadeOut(tween(160)) + slideOutVertically(tween(160)) { -it / 24 })
                    },
                    label = "tab"
                ) { index ->
                    val current = Tab.entries[index]
                    when (current) {
                        Tab.HOME -> HomeScreen(
                            viewModel = viewModel,
                            ui = ui,
                            settings = settings,
                            onOpenStats = { tab = Tab.STATS.index }
                        )
                        Tab.STATS -> StatsScreen(viewModel = viewModel, ui = ui, settings = settings)
                        Tab.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            settings = settings,
                            onRequestNotificationPermission = onRequestNotificationPermission,
                            onRequestLocationPermission = onRequestLocationPermission
                        )
                    }
                }
            }
            BottomBar(
                selected = Tab.entries[tab],
                onSelect = { tab = it.index }
            )
        }
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(mono.outline)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(mono.background)
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                BottomItem(
                    tab = tab,
                    selected = tab == selected,
                    onClick = { onSelect(tab) }
                )
            }
        }
    }
}

@Composable
private fun BottomItem(tab: Tab, selected: Boolean, onClick: () -> Unit) {
    val contentColor = if (selected) mono.invertedContent else mono.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) mono.invertedBackground else Color.Transparent)
                .padding(horizontal = 22.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(tab.label),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) mono.onBackground else mono.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
