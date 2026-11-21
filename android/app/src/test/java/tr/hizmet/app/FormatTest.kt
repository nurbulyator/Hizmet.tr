package tr.hizmet.app

import org.junit.Assert.assertEquals
import org.junit.Test
import tr.hizmet.app.data.WorkingHours
import tr.hizmet.app.ui.formatPrice
import tr.hizmet.app.ui.formatVisit
import tr.hizmet.app.ui.formatWorkingHours
import tr.hizmet.app.ui.initials
import java.util.Locale

class FormatTest {

    private val tr = Locale.forLanguageTag("tr")

    @Test
    fun priceUsesTurkishThousandsSeparator() {
        assertEquals("900₺", formatPrice(900))
        assertEquals("1.800₺", formatPrice(1800))
    }

    @Test
    fun initialsTakeFirstLetters() {
        assertEquals("EY", initials("Elif Yıldız"))
        assertEquals("M", initials("Mert"))
    }

    @Test
    fun visitIsShownInIstanbulTime() {
        // 07:00 UTC is 10:00 in Istanbul
        val text = formatVisit("2026-10-21T07:00:00.000Z", "2026-10-21T08:30:00.000Z", tr)
        assertEquals("Çar, 21 Eki · 10:00–11:30", text)
    }

    @Test
    fun workingHoursAreGrouped() {
        val hours = (1..6).map { WorkingHours(it, 600, 1140) }
        assertEquals("Pzt–Cmt, 10:00–19:00", formatWorkingHours(hours, tr))
    }

    @Test
    fun differentHoursMakeSeparateGroups() {
        val hours = (1..5).map { WorkingHours(it, 540, 1200) } + WorkingHours(6, 600, 1080)
        assertEquals("Pzt–Cum, 09:00–20:00; Cmt, 10:00–18:00", formatWorkingHours(hours, tr))
    }
}
