package tr.hizmet.app.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Keeps the JWT tokens in SharedPreferences. */
class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(accessToken != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)

    val phone: String?
        get() = prefs.getString(KEY_PHONE, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString(KEY_ACCESS, accessToken)
            putString(KEY_REFRESH, refreshToken)
        }
        _isLoggedIn.value = true
    }

    fun savePhone(phone: String) {
        prefs.edit { putString(KEY_PHONE, phone) }
    }

    fun clear() {
        prefs.edit { clear() }
        _isLoggedIn.value = false
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_PHONE = "phone"
    }
}
