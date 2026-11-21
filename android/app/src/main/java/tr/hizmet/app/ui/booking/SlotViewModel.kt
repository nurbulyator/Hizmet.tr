package tr.hizmet.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tr.hizmet.app.R
import tr.hizmet.app.data.ApiService
import tr.hizmet.app.data.CreateBookingRequest
import tr.hizmet.app.data.Slot
import tr.hizmet.app.data.SpecialistDetails
import tr.hizmet.app.data.httpCode
import tr.hizmet.app.ui.ISTANBUL
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import tr.hizmet.app.ui.appFactory

data class SlotState(
    val specialist: SpecialistDetails? = null,
    val today: LocalDate = LocalDate.now(ISTANBUL),
    val weekStart: LocalDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val selectedDate: LocalDate = today,
    val slots: List<Slot> = emptyList(),
    val selectedSlot: Slot? = null,
    val loadingSlots: Boolean = false,
    val booking: Boolean = false,
    val error: Boolean = false,
    /** One-time message for the snackbar (string resource id). */
    val message: Int? = null,
    val booked: Boolean = false,
) {
    val week: List<LocalDate> get() = (0L until 7L).map { weekStart.plusDays(it) }

    fun isWorkingDay(date: LocalDate): Boolean =
        specialist?.workingHours?.any { it.weekday == date.dayOfWeek.value } ?: true

    fun canGoBack(): Boolean = weekStart > today
}

class SlotViewModel(
    private val api: ApiService,
    private val specialistId: Int,
    val serviceId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(SlotState())
    val state: StateFlow<SlotState> = _state

    private var slotsJob: Job? = null

    init {
        loadSpecialist()
    }

    fun loadSpecialist() {
        viewModelScope.launch {
            _state.update { it.copy(error = false) }
            try {
                val specialist = api.specialist(specialistId)
                _state.update { it.copy(specialist = specialist) }
                // Start from the first working day, today or later.
                var date = _state.value.today
                while (!_state.value.isWorkingDay(date)) date = date.plusDays(1)
                selectDate(date)
            } catch (e: Exception) {
                _state.update { it.copy(error = true) }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _state.update {
            it.copy(
                selectedDate = date,
                weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                selectedSlot = null,
            )
        }
        loadSlots()
    }

    fun changeWeek(weeks: Long) {
        val s = _state.value
        val newStart = s.weekStart.plusWeeks(weeks)
        // Select the first day of the new week that is not in the past.
        val date = if (newStart < s.today) s.today else newStart
        selectDate(date)
    }

    fun selectSlot(slot: Slot) {
        if (slot.available) _state.update { it.copy(selectedSlot = slot) }
    }

    private fun loadSlots() {
        slotsJob?.cancel()
        slotsJob = viewModelScope.launch {
            _state.update { it.copy(loadingSlots = true, slots = emptyList()) }
            try {
                val slots = api.slots(specialistId, serviceId, _state.value.selectedDate.toString())
                _state.update { it.copy(slots = slots, loadingSlots = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(loadingSlots = false, message = R.string.error_network) }
            }
        }
    }

    fun book() {
        val slot = _state.value.selectedSlot ?: return
        viewModelScope.launch {
            _state.update { it.copy(booking = true) }
            try {
                api.createBooking(CreateBookingRequest(serviceId, slot.startsAt))
                _state.update { it.copy(booking = false, booked = true) }
            } catch (e: Exception) {
                if (e.httpCode() == 409) {
                    // Someone else took this time a moment ago: show fresh slots.
                    _state.update { it.copy(booking = false, message = R.string.slot_taken) }
                    loadSlots()
                } else {
                    _state.update { it.copy(booking = false, message = R.string.error_network) }
                }
            }
        }
    }

    fun messageShown() = _state.update { it.copy(message = null) }

    companion object {
        val Factory = appFactory { app, args ->
            SlotViewModel(
                api = app.api,
                specialistId = checkNotNull(args.get<Int>("specialistId")),
                serviceId = checkNotNull(args.get<Int>("serviceId")),
            )
        }
    }
}
