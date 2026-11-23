package com.hamudenes.fsolat.ui.screens

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
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamudenes.fsolat.R
import com.hamudenes.fsolat.data.AppSettings
import com.hamudenes.fsolat.prayer.HijriDate
import com.hamudenes.fsolat.prayer.Prayer
import com.hamudenes.fsolat.prayer.PrayerService
import com.hamudenes.fsolat.prayer.arabicName
import com.hamudenes.fsolat.prayer.label
import com.hamudenes.fsolat.ui.MainViewModel
import com.hamudenes.fsolat.ui.UiState
import com.hamudenes.fsolat.ui.components.Hairline
import com.hamudenes.fsolat.ui.components.InvertedCard
import com.hamudenes.fsolat.ui.components.MonoCard
import com.hamudenes.fsolat.ui.components.ProgressPips
import com.hamudenes.fsolat.ui.components.SectionLabel
import com.hamudenes.fsolat.ui.theme.LocalMono
import com.hamudenes.fsolat.ui.theme.mono
import com.hamudenes.fsolat.util.CoordFormat
import com.hamudenes.fsolat.util.Qibla
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    ui: UiState,
    settings: AppSettings,
    onOpenStats: () -> Unit
) {
    val scroll = rememberScrollState()
    val todayLog = ui.logs.firstOrNull { it.date == ui.today }
    val done = todayLog?.done ?: emptySet()
    val doneCount = done.count { key -> Prayer.prayersOnly.any { it.key == key } }
    val next = ui.next
    val current = ui.current
    val remaining = if (next != null) PrayerService.remainingSeconds(next.epochMillis, ui.now) else 0L
    val nextTimeLabel = next?.let { viewModel.formatTime(it.epochMillis, settings.use24Hour) } ?: "--:--"
    val dayLabel = ui.today.format(
        DateTimeFormatter.ofPattern(
            if (java.util.Locale.getDefault().language == "tr") "d MMMM EEEE" else "EEEE, d MMMM",
            Locale.getDefault()
        )
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        Spacer(Modifier.height(18.dp))

        // ---------- Header ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                SectionLabel(text = dayLabel, color = mono.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = settings.location.cityName,
                    style = MaterialTheme.typography.displaySmall,
                    color = mono.onBackground
                )
            }
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(mono.invertedBackground)
                    .clickable(onClick = onOpenStats),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$doneCount/5",
                    color = mono.invertedContent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = mono.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = CoordFormat.short(settings.location.latitude, settings.location.longitude),
                style = MaterialTheme.typography.labelMedium,
                color = mono.onSurfaceVariant
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.width(1.dp).height(10.dp).background(mono.outline))
            Spacer(Modifier.width(10.dp))
            Text(
                text = HijriDate.format(ui.today),
                style = MaterialTheme.typography.labelMedium,
                color = mono.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        // ---------- Hero: next prayer ----------
        InvertedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(26.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(mono.invertedContent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.next_prayer).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = mono.invertedMuted
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = next?.prayer?.arabicName() ?: "",
                        fontSize = 20.sp,
                        color = mono.invertedMuted
                    )
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = next?.prayer?.label() ?: "—",
                        style = MaterialTheme.typography.displayMedium,
                        color = mono.invertedContent,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = nextTimeLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = mono.invertedContent
                    )
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = formatCountdown(remaining),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                    color = mono.invertedContent
                )
                Text(
                    text = stringResource(R.string.time_remaining),
                    style = MaterialTheme.typography.labelMedium,
                    color = mono.invertedMuted
                )
                Spacer(Modifier.height(20.dp))
                Hairline(color = mono.invertedMuted.copy(alpha = 0.28f))
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = current?.prayer?.label() ?: "—",
                        style = MaterialTheme.typography.labelLarge,
                        color = mono.invertedMuted
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(mono.invertedMuted.copy(alpha = 0.3f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(ui.progress)
                                .height(2.dp)
                                .background(mono.invertedContent)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = next?.prayer?.label() ?: "—",
                        style = MaterialTheme.typography.labelLarge,
                        color = mono.invertedMuted
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // ---------- Today's tracking ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(stringResource(R.string.today_prayers), Modifier.weight(1f))
            Text(
                text = stringResource(R.string.done_of, doneCount, 5),
                style = MaterialTheme.typography.labelMedium,
                color = mono.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(10.dp))
        ProgressPips(done = doneCount, total = 5)
        Spacer(Modifier.height(4.dp))

        MonoCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                val times = ui.dayTimes?.list ?: emptyList()
                times.forEachIndexed { index, pt ->
                    PrayerRow(
                        prayer = pt.prayer,
                        time = viewModel.formatTime(pt.epochMillis, settings.use24Hour),
                        isNext = pt.prayer == next?.prayer,
                        isDone = pt.prayer.key in done,
                        onToggle = { viewModel.togglePrayer(pt.prayer) }
                    )
                    if (index != times.lastIndex) {
                        Hairline(color = mono.outline.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // ---------- Qibla teaser ----------
        QiblaMiniCard(settings)

        Spacer(Modifier.height(22.dp))

        // ---------- Tomorrow preview ----------
        SectionLabel(stringResource(R.string.tomorrow))
        Spacer(Modifier.height(10.dp))
        MonoCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ui.tomorrowTimes?.list?.filter { it.prayer.isPrayer }?.forEach { pt ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = pt.prayer.label(),
                            style = MaterialTheme.typography.labelSmall,
                            color = mono.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = viewModel.formatTime(pt.epochMillis, settings.use24Hour),
                            style = MaterialTheme.typography.titleMedium,
                            color = mono.onBackground
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(26.dp))
        Text(
            text = stringResource(R.string.home_footer),
            style = MaterialTheme.typography.bodyMedium,
            color = mono.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PrayerRow(
    prayer: Prayer,
    time: String,
    isNext: Boolean,
    isDone: Boolean,
    onToggle: () -> Unit
) {
    val isPrayer = prayer.isPrayer
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = isPrayer, onClick = onToggle)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> mono.invertedBackground
                        isNext -> mono.surfaceHigh
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isDone -> Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = mono.invertedContent,
                    modifier = Modifier.size(16.dp)
                )
                prayer == Prayer.SUNRISE -> Icon(
                    Icons.Outlined.WbSunny,
                    contentDescription = null,
                    tint = mono.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                else -> Canvas(Modifier.size(30.dp)) {
                    drawCircle(
                        color = if (isNext) mono.onBackground else mono.outline,
                        radius = size.minDimension / 2 - 1.5f,
                        style = Stroke(width = if (isNext) 3f else 1.5f)
                    )
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = prayer.label(),
                style = MaterialTheme.typography.titleMedium,
                color = if (prayer == Prayer.SUNRISE) mono.onSurfaceVariant else mono.onBackground,
                fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal
            )
            if (isNext) {
                Text(
                    text = stringResource(R.string.up_next),
                    style = MaterialTheme.typography.labelSmall,
                    color = mono.onSurfaceVariant
                )
            }
        }
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = if (prayer == Prayer.SUNRISE) mono.onSurfaceVariant else mono.onBackground
        )
    }
}

@Composable
private fun QiblaMiniCard(settings: AppSettings) {
    val bearing = Qibla.bearing(settings.location.latitude, settings.location.longitude)
    val distance = Qibla.distanceKm(settings.location.latitude, settings.location.longitude)
    MonoCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(mono.surfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                QiblaDial(bearing = bearing.toFloat(), modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.qibla),
                    style = MaterialTheme.typography.titleLarge,
                    color = mono.onBackground
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.qibla_distance, distance.toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = mono.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${bearing.toInt()}°",
                    style = MaterialTheme.typography.headlineSmall,
                    color = mono.onBackground
                )
                Text(
                    text = stringResource(R.string.from_north),
                    style = MaterialTheme.typography.labelSmall,
                    color = mono.onSurfaceVariant
                )
            }
        }
    }
}

/** Small compass dial pointing to the Qibla. */
@Composable
fun QiblaDial(
    bearing: Float,
    modifier: Modifier = Modifier,
    showNorth: Boolean = true
) {
    val density = LocalDensity.current
    val mono = LocalMono.current
    Canvas(modifier) {
        val stroke = with(density) { 1.5.dp.toPx() }
        val r = size.minDimension / 2 - stroke
        drawCircle(
            color = mono.outline,
            radius = r,
            style = Stroke(width = stroke)
        )
        // ticks
        for (i in 0 until 36) {
            val angle = Math.toRadians(i * 10.0)
            val inner = if (i % 9 == 0) r * 0.78f else r * 0.88f
            drawLine(
                color = if (i % 9 == 0) mono.onBackground else mono.outline,
                start = Offset(
                    center.x + (cos(angle) * inner).toFloat(),
                    center.y + (sin(angle) * inner).toFloat()
                ),
                end = Offset(
                    center.x + (cos(angle) * r).toFloat(),
                    center.y + (sin(angle) * r).toFloat()
                ),
                strokeWidth = if (i % 9 == 0) stroke * 1.2f else stroke * 0.8f,
                cap = StrokeCap.Round
            )
        }
        if (showNorth) {
            drawLine(
                color = mono.onSurfaceVariant,
                start = Offset(center.x, center.y - r * 0.5f),
                end = Offset(center.x, center.y - r * 0.72f),
                strokeWidth = stroke
            )
        }
        rotate(degrees = bearing, pivot = center) {
            val needle = r * 0.52f
            drawLine(
                color = mono.onBackground,
                start = Offset(center.x, center.y + needle * 0.35f),
                end = Offset(center.x, center.y - needle),
                strokeWidth = stroke * 3f,
                cap = StrokeCap.Round
            )
            // arrow head
            val head = needle * 0.22f
            drawLine(
                color = mono.onBackground,
                start = Offset(center.x, center.y - needle),
                end = Offset(center.x - head * 0.6f, center.y - needle + head),
                strokeWidth = stroke * 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = mono.onBackground,
                start = Offset(center.x, center.y - needle),
                end = Offset(center.x + head * 0.6f, center.y - needle + head),
                strokeWidth = stroke * 3f,
                cap = StrokeCap.Round
            )
        }
        drawCircle(color = mono.onBackground, radius = stroke * 1.6f, center = center)
    }
}

/** Countdown formatted as HH:MM:SS with a leading label-friendly look. */
private fun formatCountdown(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}
