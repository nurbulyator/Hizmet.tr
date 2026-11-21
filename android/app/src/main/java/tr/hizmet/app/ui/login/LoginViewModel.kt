package tr.hizmet.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tr.hizmet.app.data.ApiService
import tr.hizmet.app.data.PhoneRequest
import tr.hizmet.app.data.SessionStore
import tr.hizmet.app.data.VerifyRequest
import tr.hizmet.app.data.httpCode
import tr.hizmet.app.ui.appFactory

enum class LoginError { PHONE, CODE, NETWORK }

data class LoginState(
    val phoneDigits: String = "",
    val code: String = "",
    val codeSent: Boolean = false,
    val resendSeconds: Int = 0,
    val loading: Boolean = false,
    val error: LoginError? = null,
)

class LoginViewModel(
    private val api: ApiService,
    private val session: SessionStore,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val fullPhone: String
        get() = "+90" + _state.value.phoneDigits

    fun onPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(PHONE_LENGTH)
        _state.update { it.copy(phoneDigits = digits, error = null) }
    }

    fun onCodeChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(CODE_LENGTH)
        _state.update { it.copy(code = digits, error = null) }
        if (digits.length == CODE_LENGTH) signIn()
    }

    fun editPhone() {
        _state.update { it.copy(codeSent = false, code = "", error = null) }
    }

    fun tick() {
        _state.update { it.copy(resendSeconds = (it.resendSeconds - 1).coerceAtLeast(0)) }
    }

    fun sendCode() {
        if (_state.value.phoneDigits.length != PHONE_LENGTH) {
            _state.update { it.copy(error = LoginError.PHONE) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val response = api.requestCode(PhoneRequest(fullPhone))
                _state.update {
                    it.copy(loading = false, codeSent = true, code = "", resendSeconds = response.resendAfterSeconds)
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = LoginError.NETWORK) }
            }
        }
    }

    fun signIn() {
        if (_state.value.loading || _state.value.code.length != CODE_LENGTH) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val response = api.verify(VerifyRequest(fullPhone, _state.value.code))
                session.savePhone(response.user.phone)
                // Saving tokens changes session.isLoggedIn, MainActivity then opens the app.
                session.saveTokens(response.accessToken, response.refreshToken)
            } catch (e: Exception) {
                val error = if (e.httpCode() == 401) LoginError.CODE else LoginError.NETWORK
                _state.update { it.copy(loading = false, error = error, code = "") }
            }
        }
    }

    companion object {
        const val PHONE_LENGTH = 10
        const val CODE_LENGTH = 4

        val Factory = appFactory { app, _ -> LoginViewModel(app.api, app.session) }
    }
}
