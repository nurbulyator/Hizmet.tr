package tr.hizmet.app.ui.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tr.hizmet.app.R
import tr.hizmet.app.data.ApiService
import tr.hizmet.app.data.Booking
import tr.hizmet.app.data.CreateReviewRequest
import tr.hizmet.app.ui.appFactory

data class AppointmentsState(
    val tab: Int = 0,
    val upcoming: List<Booking> = emptyList(),
    val past: List<Booking> = emptyList(),
    val loading: Boolean = true,
    val error: Boolean = false,
    val message: Int? = null,
) {
    /** Completed visits shown under the upcoming list ("Recently completed"). */
    val recentlyCompleted: List<Booking>
        get() = past.filter { it.status == "COMPLETED" }.take(3)
}

class AppointmentsViewModel(private val api: ApiService) : ViewModel() {

    private val _state = MutableStateFlow(AppointmentsState())
    val state: StateFlow<AppointmentsState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = false) }
            try {
                val upcoming = api.myBookings("upcoming")
                val past = api.myBookings("past")
                _state.update { it.copy(upcoming = upcoming, past = past, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun selectTab(tab: Int) = _state.update { it.copy(tab = tab) }

    fun cancel(booking: Booking) {
        viewModelScope.launch {
            try {
                api.cancelBooking(booking.id)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(message = R.string.error_network) }
            }
        }
    }

    fun rate(booking: Booking, rating: Int) {
        viewModelScope.launch {
            try {
                api.reviewBooking(booking.id, CreateReviewRequest(rating))
                _state.update { it.copy(message = R.string.thanks_for_review) }
                load()
            } catch (e: Exception) {
                _state.update { it.copy(message = R.string.error_network) }
            }
        }
    }

    fun messageShown() = _state.update { it.copy(message = null) }

    companion object {
        val Factory = appFactory { app, _ -> AppointmentsViewModel(app.api) }
    }
}
