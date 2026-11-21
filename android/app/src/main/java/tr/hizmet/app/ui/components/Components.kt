package tr.hizmet.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tr.hizmet.app.R
import tr.hizmet.app.ui.initials
import tr.hizmet.app.ui.theme.Border
import tr.hizmet.app.ui.theme.Muted
import tr.hizmet.app.ui.theme.Star

/** Round avatar with initials, as in the sketches ("EY", "ZK", "MD"). */
@Composable
fun Avatar(name: String, color: String, size: Dp = 40.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(parseColor(color)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials(name),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp,
        )
    }
}

fun parseColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)

/** Grey hatched box with an image icon: the place for a photo in the sketches. */
@Composable
fun PhotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEDEDF1)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 7.dp.toPx()
            var x = -size.height
            while (x < size.width) {
                drawLine(
                    color = Color(0xFFDADAE0),
                    start = Offset(x, size.height),
                    end = Offset(x + size.height, 0f),
                    strokeWidth = 1.5f,
                )
                x += step
            }
        }
        Icon(Icons.Outlined.Image, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun RatingText(rating: Double, reviewCount: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Star, contentDescription = null, tint = Star, modifier = Modifier.size(15.dp))
        Text(
            text = " %.1f".format(java.util.Locale.US, rating),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Text(text = " ($reviewCount)", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Two-option switch like "Hizmetler | Yorumlar | Hakkında" or "Yaklaşan | Geçmiş". */
@Composable
fun SegmentedTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
    ) {
        options.forEachIndexed { index, title ->
            val isSelected = index == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(index) },
                color = if (isSelected) Color.White else Color.Transparent,
                shadowElevation = if (isSelected) 1.dp else 0.dp,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Muted,
                )
            }
        }
    }
}

@Composable
fun StatusChip(text: String, background: Color, textColor: Color) {
    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun CardBox(modifier: Modifier = Modifier, borderColor: Color = Border, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        content()
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorBox(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.error_network), color = Muted)
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}
