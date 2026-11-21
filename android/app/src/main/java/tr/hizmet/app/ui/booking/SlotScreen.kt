package tr.hizmet.app.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import tr.hizmet.app.R
import tr.hizmet.app.data.Slot
import tr.hizmet.app.ui.LocalSnackbar
import tr.hizmet.app.ui.components.Avatar
import tr.hizmet.app.ui.components.ErrorBox
import tr.hizmet.app.ui.components.LoadingBox
import tr.hizmet.app.ui.components.PrimaryButton
import tr.hizmet.app.ui.currentLocale
import tr.hizmet.app.ui.formatDuration
import tr.hizmet.app.ui.formatPrice
import tr.hizmet.app.ui.shortDayName
import tr.hizmet.app.ui.specialist.RoundIconButton
import tr.hizmet.app.ui.theme.Border
import tr.hizmet.app.ui.theme.Field
import tr.hizmet.app.ui.theme.Ink
import tr.hizmet.app.ui.theme.Muted
import tr.hizmet.app.ui.theme.Purple
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SlotScreen(
    onBack: () -> Unit,
    onBooked: () -> Unit,
    viewModel: SlotViewModel = viewModel(factory = SlotViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val context = LocalContext.current
    val locale = currentLocale()

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.messageShown()
            snackbar.showSnackbar(context.getString(it))
        }
    }
    LaunchedEffect(state.booked) {
        if (state.booked) {
            onBooked()
            snackbar.showSnackbar(context.getString(R.string.booking_created))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            RoundIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.slot_title), style = MaterialTheme.typography.titleMedium)
        }

        val specialist = state.specialist
        if (specialist == null) {
            if (state.error) ErrorBox(onRetry = viewModel::loadSpecialist) else LoadingBox()
            return@Column
        }
        val service = specialist.services.first { it.id == viewModel.serviceId }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // Selected service
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Field)
                    .padding(12.dp),
            ) {
                Avatar(specialist.name, specialist.avatarColor, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(service.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${specialist.name} · ${formatDuration(service.durationMin)} · ${formatPrice(service.price)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Month and week arrows
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                val month = state.selectedDate.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
                Text(month.replaceFirstChar { it.titlecase(locale) }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.changeWeek(-1) }, enabled = state.canGoBack()) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = null)
                }
                IconButton(onClick = { viewModel.changeWeek(1) }) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                }
            }

            // Days of the week
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.week.forEach { date ->
                    DayCell(
                        date = date,
                        selected = date == state.selectedDate,
                        enabled = date >= state.today && state.isWorkingDay(date),
                        onClick = { viewModel.selectDate(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)) {
                Text(stringResource(R.string.available_times), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.istanbul_time), style = MaterialTheme.typography.bodySmall)
            }

            when {
                state.loadingSlots -> LoadingBox()
                state.slots.isEmpty() -> Text(stringResource(R.string.day_off), color = Muted)
                else -> SlotGrid(state.slots, state.selectedSlot, viewModel::selectSlot)
            }

            Legend(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp))
        }

        val selected = state.selectedSlot
        val buttonText = if (selected != null) {
            val day = state.selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM", locale))
            stringResource(R.string.continue_with, "$day, ${selected.time}")
        } else {
            stringResource(R.string.available_times)
        }
        PrimaryButton(
            text = buttonText,
            onClick = viewModel::book,
            enabled = selected != null,
            loading = state.booking,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun DayCell(date: LocalDate, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val locale = currentLocale()
    val background = when {
        selected -> Ink
        else -> Field
    }
    val textColor = when {
        selected -> Color.White
        enabled -> Ink
        else -> Muted.copy(alpha = 0.5f)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(shortDayName(date.dayOfWeek, locale), fontSize = 11.sp, color = if (selected) Color.White else textColor.copy(alpha = 0.7f))
        Text(date.dayOfMonth.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun SlotGrid(slots: List<Slot>, selected: Slot?, onSelect: (Slot) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slots.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { slot ->
                    SlotChip(slot, slot == selected, { onSelect(slot) }, Modifier.weight(1f))
                }
                // Keep the same width in the last, shorter row.
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun SlotChip(slot: Slot, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(10.dp)
    val base = modifier
        .height(40.dp)
        .clip(shape)
    val styled = when {
        selected -> base.background(Purple)
        slot.available -> base.border(1.dp, Border, shape).background(Color.White)
        else -> base.background(Field)
    }
    Box(
        modifier = styled.clickable(enabled = slot.available, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = slot.time,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = when {
                selected -> Color.White
                slot.available -> Ink
                else -> Muted.copy(alpha = 0.5f)
            },
            textDecoration = if (slot.available) null else TextDecoration.LineThrough,
        )
    }
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        LegendDot(Color.White, border = true)
        Text(" " + stringResource(R.string.legend_free) + "   ", style = MaterialTheme.typography.bodySmall)
        LegendDot(Field)
        Text(" " + stringResource(R.string.legend_busy) + "   ", style = MaterialTheme.typography.bodySmall)
        LegendDot(Purple)
        Text(" " + stringResource(R.string.legend_selected), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LegendDot(color: Color, border: Boolean = false) {
    Box(
        Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (border) Modifier.border(1.dp, Border, CircleShape) else Modifier),
    )
}
