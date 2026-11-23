package com.hamudenes.fsolat.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamudenes.fsolat.ui.theme.LocalMono
import com.hamudenes.fsolat.ui.theme.mono

/** Uppercase, wide tracked section label — the app's editorial signature. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = mono.onSurfaceVariant) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
    )
}

/** Thin hairline divider. */
@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = mono.outline) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/** Rounded monochrome card container. */
@Composable
fun MonoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = mono.surface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, mono.outline)
    ) {
        content()
    }
}

/** Inverted (ink on paper) card used for the hero sections. */
@Composable
fun InvertedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = mono.invertedBackground,
        shape = RoundedCornerShape(28.dp)
    ) {
        content()
    }
}

/** Small circular icon button with a hairline border. */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false
) {
    val mono = LocalMono.current
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (filled) mono.invertedBackground else Color.Transparent)
            .then(
                if (filled) Modifier else Modifier.clip(CircleShape).background(Color.Transparent)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!filled) {
            Canvas(Modifier.size(40.dp)) {
                drawCircle(
                    color = mono.outline,
                    radius = size.minDimension / 2 - 1,
                    style = Stroke(width = 1.5f)
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (filled) mono.invertedContent else mono.onBackground,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** Horizontal progress line used for "time until next prayer". */
@Composable
fun HairlineProgress(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(mono.outline)
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(mono.onBackground)
        )
    }
}

/** Segmented monochrome control. */
@Composable
fun <T> SegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(mono.surfaceHigh)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isSelected) mono.invertedBackground else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) mono.invertedContent else mono.onSurfaceVariant
                )
            }
        }
    }
}

/** Row of five prayer pips showing today's progress. */
@Composable
fun ProgressPips(done: Int, total: Int = 5, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .height(4.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i < done) mono.onBackground else mono.outline)
            )
        }
    }
}

/** Circular ring progress with a centered label. */
@Composable
fun RingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 6f,
    trackColor: Color = mono.outline,
    progressColor: Color = mono.onBackground,
    content: @Composable () -> Unit
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = strokeWidth.dp.toPx()
            val inset = stroke / 2
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        content()
    }
}

/** Metric tile used on the stats screen. */
@Composable
fun MetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    inverted: Boolean = false
) {
    val bg = if (inverted) mono.invertedBackground else mono.surface
    val fg = if (inverted) mono.invertedContent else mono.onBackground
    val sub = if (inverted) mono.invertedMuted else mono.onSurfaceVariant
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(20.dp),
        border = if (inverted) null else androidx.compose.foundation.BorderStroke(1.dp, mono.outline)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(value, style = MaterialTheme.typography.displaySmall, color = fg)
            Spacer(Modifier.height(6.dp))
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = sub
            )
        }
    }
}
