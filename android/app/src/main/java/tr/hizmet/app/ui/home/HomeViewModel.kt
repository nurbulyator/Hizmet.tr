package tr.hizmet.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tr.hizmet.app.data.ApiService
import tr.hizmet.app.data.Booking
import tr.hizmet.app.data.SpecialistItem
import tr.hizmet.app.ui.appFactory

data class HomeState(
    val nextBooking: Booking? = null,
    val favorites: List<SpecialistItem> = emptyList(),
    val loading: Boolean = true,
    val error: Boolean = false,
)

class HomeViewModel(private val api: ApiService) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = false) }
            try {
                val next = api.myBookings("upcoming").firstOrNull()
                val favorites = api.specialists(city = null, category = null, query = null, sort = "rating")
                    .filter { it.isFavorite }
                _state.update { it.copy(nextBooking = next, favorites = favorites, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    companion object {
        val Factory = appFactory { app, _ -> HomeViewModel(app.api) }
    }
}
