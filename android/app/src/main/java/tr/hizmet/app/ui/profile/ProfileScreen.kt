package tr.hizmet.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tr.hizmet.app.BuildConfig
import tr.hizmet.app.HizmetApp
import tr.hizmet.app.R
import tr.hizmet.app.ui.components.Avatar
import tr.hizmet.app.ui.components.CardBox
import tr.hizmet.app.ui.components.SegmentedTabs
import tr.hizmet.app.ui.currentLocale
import tr.hizmet.app.ui.setLanguage
import tr.hizmet.app.ui.theme.Muted

private val languages = listOf("tr" to "Türkçe", "en" to "English")

@Composable
fun ProfileScreen() {
    val session = (LocalContext.current.applicationContext as HizmetApp).session
    val phone = session.phone.orEmpty()
    val language = currentLocale().language

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.profile), style = MaterialTheme.typography.headlineSmall)

        CardBox {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                Avatar(name = "H T", color = "#5B30D9", size = 48.dp)
                Spacer(Modifier.width(12.dp))
                Text(formatPhone(phone), style = MaterialTheme.typography.titleMedium)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Language, contentDescription = null, tint = Muted)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleSmall)
        }
        SegmentedTabs(
            options = languages.map { it.second },
            selected = languages.indexOfFirst { it.first == language }.coerceAtLeast(0),
            onSelect = { setLanguage(languages[it].first) },
        )

        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = { session.clear() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.log_out))
        }
        Text(
            stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

/** "+905321234567" -> "+90 532 123 45 67" */
private fun formatPhone(phone: String): String {
    if (phone.length != 13) return phone
    return "${phone.substring(0, 3)} ${phone.substring(3, 6)} ${phone.substring(6, 9)} " +
        "${phone.substring(9, 11)} ${phone.substring(11)}"
}
