package tr.hizmet.app.ui.specialist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tr.hizmet.app.data.ApiService
import tr.hizmet.app.data.Review
import tr.hizmet.app.data.SpecialistDetails
import tr.hizmet.app.ui.appFactory

data class SpecialistState(
    val specialist: SpecialistDetails? = null,
    val reviews: List<Review> = emptyList(),
    val selectedServiceId: Int? = null,
    val tab: Int = 0,
    val loading: Boolean = true,
    val error: Boolean = false,
)

class SpecialistViewModel(
    private val api: ApiService,
    private val specialistId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(SpecialistState())
    val state: StateFlow<SpecialistState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = false) }
            try {
                val specialist = api.specialist(specialistId)
                val reviews = api.reviews(specialistId)
                _state.update {
                    it.copy(
                        specialist = specialist,
                        reviews = reviews,
                        selectedServiceId = it.selectedServiceId ?: specialist.services.firstOrNull()?.id,
                        loading = false,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun selectTab(tab: Int) = _state.update { it.copy(tab = tab) }

    fun selectService(id: Int) = _state.update { it.copy(selectedServiceId = id) }

    fun toggleFavorite() {
        val specialist = _state.value.specialist ?: return
        val newValue = !specialist.isFavorite
        setFavorite(newValue)
        viewModelScope.launch {
            try {
                if (newValue) api.addFavorite(specialist.id) else api.removeFavorite(specialist.id)
            } catch (e: Exception) {
                setFavorite(!newValue)
            }
        }
    }

    private fun setFavorite(value: Boolean) {
        _state.update { s -> s.copy(specialist = s.specialist?.copy(isFavorite = value)) }
    }

    companion object {
        val Factory = appFactory { app, args ->
            SpecialistViewModel(app.api, checkNotNull(args.get<Int>("id")))
        }
    }
}
