package tr.hizmet.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colors taken from the sketches in the project report.
val Purple = Color(0xFF5B30D9)
val PurpleLight = Color(0xFFEDE8FC)
val Ink = Color(0xFF16161D)
val Muted = Color(0xFF6B6B78)
val Field = Color(0xFFF2F2F6)
val Border = Color(0xFFE3E3EA)
val Star = Color(0xFFF5A524)
val Heart = Color(0xFFE11D48)

val PendingBg = Color(0xFFFBE3DC)
val PendingText = Color(0xFFB4441F)
val ConfirmedBg = Color(0xFFDDF2EA)
val ConfirmedText = Color(0xFF136850)

private val colors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = Purple,
    secondaryContainer = PurpleLight,
    onSecondaryContainer = Purple,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Field,
    onSurfaceVariant = Muted,
    surfaceContainer = Color.White,
    outline = Border,
    outlineVariant = Border,
)

private val typography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp, color = Muted),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
)

private val shapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
)

@Composable
fun HizmetTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, shapes = shapes, content = content)
}
