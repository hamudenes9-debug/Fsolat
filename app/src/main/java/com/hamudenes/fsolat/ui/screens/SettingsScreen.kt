package com.hamudenes.fsolat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamudenes.fsolat.BuildConfig
import com.hamudenes.fsolat.R
import com.hamudenes.fsolat.data.AppLanguage
import com.hamudenes.fsolat.data.AppSettings
import com.hamudenes.fsolat.data.City
import com.hamudenes.fsolat.data.ThemeMode
import com.hamudenes.fsolat.prayer.CalcMethod
import com.hamudenes.fsolat.prayer.Madhab
import com.hamudenes.fsolat.prayer.Prayer
import com.hamudenes.fsolat.prayer.description
import com.hamudenes.fsolat.prayer.label
import com.hamudenes.fsolat.ui.MainViewModel
import com.hamudenes.fsolat.ui.components.Hairline
import com.hamudenes.fsolat.ui.components.InvertedCard
import com.hamudenes.fsolat.ui.components.MonoCard
import com.hamudenes.fsolat.ui.components.SectionLabel
import com.hamudenes.fsolat.ui.components.SegmentedControl
import com.hamudenes.fsolat.ui.theme.mono
import com.hamudenes.fsolat.util.CoordFormat
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    settings: AppSettings,
    onRequestNotificationPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit
) {
    var showCityPicker by remember { mutableStateOf(false) }
    var showMethodPicker by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showTuneSheet by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        SectionLabel(stringResource(R.string.settings_title))
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_headline),
            style = MaterialTheme.typography.displaySmall,
            color = mono.onBackground
        )

        Spacer(Modifier.height(22.dp))

        // ---------------- Theme ----------------
        SectionLabel(stringResource(R.string.section_appearance))
        Spacer(Modifier.height(10.dp))
        ThemeSelector(
            selected = settings.themeMode,
            onSelect = viewModel::setTheme
        )

        Spacer(Modifier.height(22.dp))

        // ---------------- Language ----------------
        SectionLabel(stringResource(R.string.section_language))
        Spacer(Modifier.height(10.dp))
        MonoCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                SegmentedControl(
                    options = listOf(
                        AppLanguage.SYSTEM to stringResource(R.string.lang_system),
                        AppLanguage.TURKISH to "Türkçe",
                        AppLanguage.ENGLISH to "English"
                    ),
                    selected = settings.language,
                    onSelect = viewModel::setLanguage
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.language_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = mono.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ---------------- Location ----------------
        SectionLabel(stringResource(R.string.section_location))
        Spacer(Modifier.height(10.dp))
        MonoCard(Modifier.fillMaxWidth()) {
            Column {
                SettingRow(
                    icon = Icons.Outlined.LocationOn,
                    title = stringResource(R.string.city),
                    subtitle = settings.location.cityName + "\n" +
                        CoordFormat.short(settings.location.latitude, settings.location.longitude),
                    onClick = { showCityPicker = true }
                )
                Hairline()
                SettingRow(
                    icon = Icons.Outlined.Public,
                    title = stringResource(R.string.use_my_location),
                    subtitle = stringResource(R.string.use_my_location_desc),
                    onClick = onRequestLocationPermission
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ---------------- Calculation ----------------
        SectionLabel(stringResource(R.string.section_calculation))
        Spacer(Modifier.height(10.dp))
        MonoCard(Modifier.fillMaxWidth()) {
            Column {
                SettingRow(
                    icon = Icons.Outlined.Tune,
                    title = stringResource(R.string.calc_method),
                    subtitle = settings.method.label(),
                    onClick = { showMethodPicker = true }
                )
                Hairline()
                Column(Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(R.string.asr_madhab).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = mono.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SegmentedControl(
                        options = listOf(
                            Madhab.SHAFI to stringResource(R.string.madhab_shafi),
                            Madhab.HANAFI to stringResource(R.string.madhab_hanafi)
                        ),
                        selected = settings.madhab,
                        onSelect = viewModel::setMadhab
                    )
                }
                Hairline()
                SettingRow(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(R.string.adjustments),
                    subtitle = if (settings.adjustments.values.any { it != 0 })
                        stringResource(R.string.adjustments_custom)
                    else stringResource(R.string.adjustments_default),
                    onClick = { showTuneSheet = true }
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ---------------- Notifications ----------------
        SectionLabel(stringResource(R.string.section_notifications))
        Spacer(Modifier.height(10.dp))
        MonoCard(Modifier.fillMaxWidth()) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = !settings.notificationsEnabled
                            if (next) onRequestNotificationPermission()
                            viewModel.setNotifications(next)
                        }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(mono.surfaceHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = mono.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.notifications),
                            style = MaterialTheme.typography.titleMedium,
                            color = mono.onBackground
                        )
                        Text(
                            text = stringResource(R.string.notifications_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = mono.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) onRequestNotificationPermission()
                            viewModel.setNotifications(enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = mono.background,
                            checkedTrackColor = mono.onBackground,
                            checkedBorderColor = mono.onBackground,
                            uncheckedThumbColor = mono.onSurfaceVariant,
                            uncheckedTrackColor = Color.Transparent,
                            uncheckedBorderColor = mono.outline
                        )
                    )
                }
                Hairline()
                SettingRow(
                    icon = Icons.Outlined.Schedule,
                    title = stringResource(R.string.reminder_before),
                    subtitle = reminderLabel(settings.reminderBeforeMinutes),
                    onClick = { showReminderPicker = true },
                    enabled = settings.notificationsEnabled
                )
                Hairline()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setUse24Hour(!settings.use24Hour) }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(mono.surfaceHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = mono.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.time_format),
                            style = MaterialTheme.typography.titleMedium,
                            color = mono.onBackground
                        )
                        Text(
                            text = if (settings.use24Hour) stringResource(R.string.time_24h)
                            else stringResource(R.string.time_12h),
                            style = MaterialTheme.typography.bodyMedium,
                            color = mono.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.use24Hour,
                        onCheckedChange = viewModel::setUse24Hour,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = mono.background,
                            checkedTrackColor = mono.onBackground,
                            checkedBorderColor = mono.onBackground,
                            uncheckedThumbColor = mono.onSurfaceVariant,
                            uncheckedTrackColor = Color.Transparent,
                            uncheckedBorderColor = mono.outline
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // ---------------- About ----------------
        SectionLabel(stringResource(R.string.section_about))
        Spacer(Modifier.height(10.dp))
        InvertedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = "Fsolat",
                    style = MaterialTheme.typography.displaySmall,
                    color = mono.invertedContent
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.about_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = mono.invertedMuted
                )
                Spacer(Modifier.height(18.dp))
                Hairline(color = mono.invertedMuted.copy(alpha = 0.25f))
                Spacer(Modifier.height(14.dp))
                AboutLine(stringResource(R.string.about_version), BuildConfig.VERSION_NAME)
                Spacer(Modifier.height(8.dp))
                AboutLine(stringResource(R.string.about_package), BuildConfig.APPLICATION_ID)
                Spacer(Modifier.height(8.dp))
                AboutLine(stringResource(R.string.about_developer), "@hamudenes")
            }
        }

        Spacer(Modifier.height(26.dp))
        Text(
            text = stringResource(R.string.made_with_care),
            style = MaterialTheme.typography.labelSmall,
            color = mono.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(26.dp))
    }

    if (showCityPicker) {
        CityPickerDialog(
            current = settings.location.cityName,
            onDismiss = { showCityPicker = false },
            onPick = { city ->
                viewModel.setCity(city.name, city.latitude, city.longitude)
                showCityPicker = false
            }
        )
    }

    if (showMethodPicker) {
        MethodPickerDialog(
            current = settings.method,
            onDismiss = { showMethodPicker = false },
            onPick = {
                viewModel.setMethod(it)
                showMethodPicker = false
            }
        )
    }

    if (showReminderPicker) {
        ReminderPickerDialog(
            current = settings.reminderBeforeMinutes,
            onDismiss = { showReminderPicker = false },
            onPick = {
                viewModel.setReminderBefore(it)
                showReminderPicker = false
            }
        )
    }

    if (showTuneSheet) {
        AdjustmentsDialog(
            settings = settings,
            onChange = viewModel::setAdjustment,
            onReset = viewModel::resetAdjustments,
            onDismiss = { showTuneSheet = false }
        )
    }
}

@Composable
private fun AboutLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = mono.invertedMuted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = mono.invertedContent
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.4f
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(mono.surfaceHigh.copy(alpha = alpha)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = mono.onBackground.copy(alpha = alpha),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = mono.onBackground.copy(alpha = alpha)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = mono.onSurfaceVariant.copy(alpha = alpha)
            )
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = mono.onSurfaceVariant.copy(alpha = alpha),
            modifier = Modifier.size(18.dp)
        )
    }
}

/** Large, tactile black / white theme chooser with live swatches. */
@Composable
private fun ThemeSelector(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.theme_dark),
                subtitle = stringResource(R.string.theme_dark_desc),
                bg = Color(0xFF000000),
                fg = Color(0xFFF5F5F2),
                icon = Icons.Outlined.DarkMode,
                selected = selected == ThemeMode.DARK,
                onClick = { onSelect(ThemeMode.DARK) }
            )
            ThemeCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.theme_light),
                subtitle = stringResource(R.string.theme_light_desc),
                bg = Color(0xFFF7F7F5),
                fg = Color(0xFF0A0A0A),
                icon = Icons.Outlined.LightMode,
                selected = selected == ThemeMode.LIGHT,
                onClick = { onSelect(ThemeMode.LIGHT) }
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(mono.surface)
                .border(1.dp, mono.outline, RoundedCornerShape(16.dp))
                .clickable { onSelect(ThemeMode.SYSTEM) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Language,
                contentDescription = null,
                tint = if (selected == ThemeMode.SYSTEM) mono.onBackground else mono.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.theme_system),
                    style = MaterialTheme.typography.titleMedium,
                    color = mono.onBackground
                )
                Text(
                    text = if (selected == ThemeMode.SYSTEM) stringResource(R.string.theme_system_hint)
                    else stringResource(R.string.theme_system_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = mono.onSurfaceVariant
                )
            }
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected == ThemeMode.SYSTEM) mono.invertedBackground else Color.Transparent
                    )
                    .border(
                        1.dp,
                        if (selected == ThemeMode.SYSTEM) mono.onBackground else mono.outline,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected == ThemeMode.SYSTEM) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = mono.invertedContent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    bg: Color,
    fg: Color,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) mono.onBackground else mono.outline,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) fg else Color.Transparent)
                    .border(1.dp, if (selected) fg else fg.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = bg,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = fg
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = fg.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun CityPickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onPick: (City) -> Unit
) {
    val context = LocalContext.current
    val cities = remember { City.all(context) }
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) cities
        else cities.filter { it.name.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = mono.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(stringResource(R.string.choose_city), color = mono.onBackground)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.search_city)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = mono.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    filtered.forEach { city ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPick(city) }
                                .padding(horizontal = 10.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = city.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = mono.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = city.countryCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = mono.onSurfaceVariant
                            )
                            if (city.name == current) {
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = mono.onBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Hairline(color = mono.outline.copy(alpha = 0.5f))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = mono.onBackground)
            }
        }
    )
}

@Composable
private fun MethodPickerDialog(
    current: CalcMethod,
    onDismiss: () -> Unit,
    onPick: (CalcMethod) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = mono.surface,
        shape = RoundedCornerShape(26.dp),
        title = { Text(stringResource(R.string.calc_method), color = mono.onBackground) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                CalcMethod.entries.forEach { method ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPick(method) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = method.label(),
                                style = MaterialTheme.typography.titleMedium,
                                color = mono.onBackground
                            )
                            Text(
                                text = method.description(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = mono.onSurfaceVariant
                            )
                        }
                        if (method == current) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = mono.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = mono.onBackground)
            }
        }
    )
}

@Composable
private fun ReminderPickerDialog(
    current: Int,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit
) {
    val options = listOf(0, 5, 10, 15, 20, 30)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = mono.surface,
        shape = RoundedCornerShape(26.dp),
        title = { Text(stringResource(R.string.reminder_before), color = mono.onBackground) },
        text = {
            Column {
                options.forEach { minutes ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPick(minutes) }
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reminderLabel(minutes),
                            style = MaterialTheme.typography.titleMedium,
                            color = mono.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        if (minutes == current) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                tint = mono.onBackground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = mono.onBackground)
            }
        }
    )
}

@Composable
private fun reminderLabel(minutes: Int): String =
    if (minutes == 0) stringResource(R.string.at_prayer_time)
    else stringResource(R.string.minutes_before, minutes)

@Composable
private fun AdjustmentsDialog(
    settings: AppSettings,
    onChange: (String, Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = mono.surface,
        shape = RoundedCornerShape(26.dp),
        title = { Text(stringResource(R.string.adjustments), color = mono.onBackground) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.adjustments_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = mono.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                listOf(
                    "fajr" to Prayer.FAJR,
                    "sunrise" to Prayer.SUNRISE,
                    "dhuhr" to Prayer.DHUHR,
                    "asr" to Prayer.ASR,
                    "maghrib" to Prayer.MAGHRIB,
                    "isha" to Prayer.ISHA
                ).forEach { (key, prayer) ->
                    val value = settings.adjustments[key] ?: 0
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prayer.label(),
                            style = MaterialTheme.typography.titleMedium,
                            color = mono.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        StepButton("−") { onChange(key, value - 1) }
                        Text(
                            text = if (value > 0) "+$value" else "$value",
                            style = MaterialTheme.typography.titleMedium,
                            color = mono.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(52.dp)
                        )
                        StepButton("+") { onChange(key, value + 1) }
                    }
                    Hairline(color = mono.outline.copy(alpha = 0.5f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done), color = mono.onBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.reset), color = mono.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun StepButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(mono.surfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = mono.onBackground
        )
    }
}
