package com.hamudenes.fsolat.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamudenes.fsolat.R
import com.hamudenes.fsolat.data.AppSettings
import com.hamudenes.fsolat.data.DayLog
import com.hamudenes.fsolat.prayer.Prayer
import com.hamudenes.fsolat.prayer.label
import com.hamudenes.fsolat.ui.MainViewModel
import com.hamudenes.fsolat.ui.UiState
import com.hamudenes.fsolat.ui.components.Hairline
import com.hamudenes.fsolat.ui.components.InvertedCard
import com.hamudenes.fsolat.ui.components.MetricTile
import com.hamudenes.fsolat.ui.components.MonoCard
import com.hamudenes.fsolat.ui.components.RingProgress
import com.hamudenes.fsolat.ui.components.SectionLabel
import com.hamudenes.fsolat.ui.components.SegmentedControl
import com.hamudenes.fsolat.ui.theme.mono
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class Range(val days: Int, val labelRes: Int) {
    WEEK(7, R.string.range_week),
    MONTH(30, R.string.range_month),
    SEASON(90, R.string.range_season)
}

@Composable
fun StatsScreen(
    viewModel: MainViewModel,
    ui: UiState,
    settings: AppSettings
) {
    var range by rememberSaveable { mutableStateOf(Range.WEEK) }
    var selectedDay by rememberSaveable { mutableStateOf<String?>(null) }

    val logs = ui.logs.take(range.days)
    val total = logs.sumOf { it.count }
    val trackedDays = logs.count { it.count > 0 }
    val avg = if (logs.size > 0) total.toFloat() / (logs.size * 5f) else 0f
    val completionDays = logs.count { it.count >= 5 }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        SectionLabel(stringResource(R.string.stats_title))
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.stats_headline),
            style = MaterialTheme.typography.displaySmall,
            color = mono.onBackground
        )
        Spacer(Modifier.height(20.dp))

        SegmentedControl(
            options = Range.entries.map { it to stringResource(it.labelRes) },
            selected = range,
            onSelect = { range = it }
        )

        Spacer(Modifier.height(20.dp))

        InvertedCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RingProgress(
                    progress = ui.monthAverage.coerceIn(0f, 1f),
                    modifier = Modifier.size(112.dp),
                    strokeWidth = 5f,
                    trackColor = mono.invertedMuted.copy(alpha = 0.25f),
                    progressColor = mono.invertedContent
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(ui.monthAverage * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = mono.invertedContent
                        )
                        Text(
                            text = stringResource(R.string.this_month),
                            style = MaterialTheme.typography.labelSmall,
                            color = mono.invertedMuted
                        )
                    }
                }
                Spacer(Modifier.width(22.dp))
                Column {
                    Text(
                        text = stringResource(R.string.streak_current, ui.currentStreak),
                        style = MaterialTheme.typography.titleLarge,
                        color = mono.invertedContent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.streak_longest, ui.longestStreak),
                        style = MaterialTheme.typography.bodyMedium,
                        color = mono.invertedMuted
                    )
                    Spacer(Modifier.height(12.dp))
                    Hairline(color = mono.invertedMuted.copy(alpha = 0.25f))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.total_completed, ui.totalCompleted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = mono.invertedMuted
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                value = "$total",
                label = stringResource(R.string.prayers_label),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                value = "${(avg * 100).toInt()}%",
                label = stringResource(R.string.rate_label),
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                value = "$completionDays",
                label = stringResource(R.string.full_days),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(22.dp))
        SectionLabel(stringResource(R.string.heatmap))
        Spacer(Modifier.height(10.dp))

        MonoCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                HeatmapGrid(
                    logs = ui.logs,
                    weeks = if (range == Range.SEASON) 13 else 8
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.less),
                        style = MaterialTheme.typography.labelSmall,
                        color = mono.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    listOf(0, 1, 3, 5).forEach { level ->
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(levelColor(level))
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = stringResource(R.string.more),
                        style = MaterialTheme.typography.labelSmall,
                        color = mono.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.tracked_days, trackedDays, logs.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = mono.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        SectionLabel(stringResource(R.string.prayer_breakdown))
        Spacer(Modifier.height(10.dp))
        MonoCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                Prayer.prayersOnly.forEachIndexed { index, prayer ->
                    val count = logs.sumOf { if (prayer.key in it.done) 1 else 0 }
                    val ratio = if (logs.isEmpty()) 0f else count.toFloat() / logs.size
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prayer.label(),
                            style = MaterialTheme.typography.titleMedium,
                            color = mono.onBackground,
                            modifier = Modifier.width(84.dp)
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(mono.outline)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                                    .height(6.dp)
                                    .background(mono.onBackground)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.labelLarge,
                            color = mono.onSurfaceVariant,
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    if (index != Prayer.prayersOnly.lastIndex) {
                        Hairline(color = mono.outline.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        SectionLabel(stringResource(R.string.daily_log))
        Spacer(Modifier.height(10.dp))
        MonoCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                logs.take(if (range == Range.WEEK) 7 else 10).forEachIndexed { index, log ->
                    HistoryRow(
                        log = log,
                        expanded = selectedDay == log.date.toString(),
                        onToggle = {
                            selectedDay = if (selectedDay == log.date.toString()) null else log.date.toString()
                        },
                        onMark = { prayer, done -> viewModel.setDone(log.date, prayer, done) }
                    )
                    if (index != logs.take(if (range == Range.WEEK) 7 else 10).lastIndex) {
                        Hairline(color = mono.outline.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.stats_footer),
            style = MaterialTheme.typography.bodyMedium,
            color = mono.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun HistoryRow(
    log: DayLog,
    expanded: Boolean,
    onToggle: () -> Unit,
    onMark: (Prayer, Boolean) -> Unit
) {
    val isToday = log.date == LocalDate.now()
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val formatter = remember { DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale.getDefault()) }
            Column(Modifier.weight(1f)) {
                Text(
                    text = log.date.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = mono.onBackground
                )
                if (isToday) {
                    Text(
                        text = stringResource(R.string.today),
                        style = MaterialTheme.typography.labelSmall,
                        color = mono.onSurfaceVariant
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Prayer.prayersOnly.forEach { prayer ->
                    val marked = prayer.key in log.done
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (marked) mono.onBackground else mono.outline.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (marked) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = prayer.label(),
                                tint = mono.background,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(mono.surfaceHigh)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Prayer.prayersOnly.forEach { prayer ->
                    val done = prayer.key in log.done
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (done) mono.invertedBackground else Color.Transparent)
                            .clickable { onMark(prayer, !done) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = prayer.label().take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (done) mono.invertedContent else mono.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapGrid(logs: List<DayLog>, weeks: Int) {
    val byDate = remember(logs) { logs.associateBy { it.date } }
    val today = LocalDate.now()
    val lastMonday = today.minusDays(((today.dayOfWeek.value - 1).toLong()))
    val start = lastMonday.minusWeeks((weeks - 1).toLong())
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(weeks) { week ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { dayOfWeek ->
                    val date = start.plusDays((week * 7 + dayOfWeek).toLong())
                    val count = byDate[date]?.count ?: 0
                    val future = date.isAfter(today)
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    future -> Color.Transparent
                                    date == today -> mono.onBackground
                                    else -> levelColor(count)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun levelColor(level: Int): Color = when (level) {
    0 -> mono.surfaceHigh
    1, 2 -> mono.outline
    3, 4 -> mono.onSurfaceVariant
    else -> mono.onBackground
}
