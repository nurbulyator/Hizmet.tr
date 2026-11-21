package tr.hizmet.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tr.hizmet.app.data.ApiService
import tr.hizmet.app.data.Category
import tr.hizmet.app.data.SpecialistItem
import tr.hizmet.app.ui.appFactory

enum class Sort(val apiValue: String) { RATING("rating"), PRICE("price") }

data class CatalogState(
    val cities: List<String> = emptyList(),
    val city: String? = "İstanbul",
    val categories: List<Category> = emptyList(),
    val category: String? = null,
    val query: String = "",
    val sort: Sort = Sort.RATING,
    val specialists: List<SpecialistItem> = emptyList(),
    val loading: Boolean = true,
    val error: Boolean = false,
)

class CatalogViewModel(private val api: ApiService) : ViewModel() {

    private val _state = MutableStateFlow(CatalogState())
    val state: StateFlow<CatalogState> = _state

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadFilters()
        loadSpecialists()
    }

    fun loadFilters() {
        viewModelScope.launch {
            try {
                val categories = api.categories()
                val cities = api.cities()
                _state.update { it.copy(categories = categories, cities = cities) }
            } catch (e: Exception) {
                // The list below shows the error, filters stay empty.
            }
        }
    }

    fun loadSpecialists() {
        // Only the newest request matters, so an older one is cancelled.
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = false) }
            val s = _state.value
            try {
                val list = api.specialists(
                    city = s.city,
                    category = s.category,
                    query = s.query.trim().ifEmpty { null },
                    sort = s.sort.apiValue,
                )
                _state.update { it.copy(specialists = list, loading = false) }
            } catch (e: CancellationException) {
                throw e // a newer request replaced this one, it is not an error
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = true) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        // Wait until the user stops typing before calling the server.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadSpecialists()
        }
    }

    fun selectCity(city: String?) {
        _state.update { it.copy(city = city) }
        loadSpecialists()
    }

    fun selectCategory(slug: String?) {
        _state.update { it.copy(category = if (it.category == slug) null else slug) }
        loadSpecialists()
    }

    fun selectSort(sort: Sort) {
        _state.update { it.copy(sort = sort) }
        loadSpecialists()
    }

    fun toggleFavorite(item: SpecialistItem) {
        val newValue = !item.isFavorite
        setFavorite(item.id, newValue)
        viewModelScope.launch {
            try {
                if (newValue) api.addFavorite(item.id) else api.removeFavorite(item.id)
            } catch (e: Exception) {
                setFavorite(item.id, !newValue)
            }
        }
    }

    private fun setFavorite(id: Int, value: Boolean) {
        _state.update { s ->
            s.copy(specialists = s.specialists.map { if (it.id == id) it.copy(isFavorite = value) else it })
        }
    }

    companion object {
        val Factory = appFactory { app, _ -> CatalogViewModel(app.api) }
    }
}
