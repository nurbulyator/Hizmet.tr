package tr.hizmet.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import tr.hizmet.app.R
import tr.hizmet.app.data.WorkingHours
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val ISTANBUL: ZoneId = ZoneId.of("Europe/Istanbul")

@Composable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

/** 1800 -> "1.800₺" */
fun formatPrice(price: Int): String =
    NumberFormat.getIntegerInstance(Locale.forLanguageTag("tr")).format(price) + "₺"

@Composable
fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> stringResource(R.string.duration_min, m)
        m == 0 -> stringResource(R.string.duration_h, h)
        else -> stringResource(R.string.duration_h_min, h, m)
    }
}

fun parseTime(iso: String): ZonedDateTime = Instant.parse(iso).atZone(ISTANBUL)

/** "Wed, 21 Oct" / "Çar, 21 Eki" */
fun formatDay(time: ZonedDateTime, locale: Locale): String =
    time.format(DateTimeFormatter.ofPattern("EEE, d MMM", locale))

/** "Çar, 21 Eki · 10:00–11:30" */
fun formatVisit(startsAt: String, endsAt: String, locale: Locale): String {
    val start = parseTime(startsAt)
    val end = parseTime(endsAt)
    val hm = DateTimeFormatter.ofPattern("HH:mm")
    return "${formatDay(start, locale)} · ${start.format(hm)}–${end.format(hm)}"
}

fun formatMinute(minute: Int): String = "%02d:%02d".format(minute / 60, minute % 60)

fun shortDayName(day: DayOfWeek, locale: Locale): String =
    day.getDisplayName(TextStyle.SHORT, locale)

/** Groups days with the same hours: "Mon–Sat, 10:00–19:00" */
fun formatWorkingHours(hours: List<WorkingHours>, locale: Locale): String {
    if (hours.isEmpty()) return "—"
    val groups = mutableListOf<MutableList<WorkingHours>>()
    for (h in hours.sortedBy { it.weekday }) {
        val last = groups.lastOrNull()?.last()
        if (last != null && last.weekday + 1 == h.weekday &&
            last.startMinute == h.startMinute && last.endMinute == h.endMinute
        ) {
            groups.last().add(h)
        } else {
            groups.add(mutableListOf(h))
        }
    }
    return groups.joinToString("; ") { group ->
        val first = shortDayName(DayOfWeek.of(group.first().weekday), locale)
        val last = shortDayName(DayOfWeek.of(group.last().weekday), locale)
        val days = if (group.size == 1) first else "$first–$last"
        "$days, ${formatMinute(group.first().startMinute)}–${formatMinute(group.first().endMinute)}"
    }
}

/** "Elif Yıldız" -> "EY" */
fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
