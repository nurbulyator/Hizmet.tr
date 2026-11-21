package tr.hizmet.app.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Switches the app between Turkish and English. AppCompat recreates the activity. */
fun toggleLanguage(current: String) {
    val next = if (current == "tr") "en" else "tr"
    setLanguage(next)
}

fun setLanguage(tag: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
}
