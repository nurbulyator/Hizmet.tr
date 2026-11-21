package tr.hizmet.app.ui.appointments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import tr.hizmet.app.R
import tr.hizmet.app.data.Booking
import tr.hizmet.app.ui.LocalSnackbar
import tr.hizmet.app.ui.components.Avatar
import tr.hizmet.app.ui.components.CardBox
import tr.hizmet.app.ui.components.ErrorBox
import tr.hizmet.app.ui.components.LoadingBox
import tr.hizmet.app.ui.components.SegmentedTabs
import tr.hizmet.app.ui.components.StatusChip
import tr.hizmet.app.ui.currentLocale
import tr.hizmet.app.ui.formatDay
import tr.hizmet.app.ui.formatPrice
import tr.hizmet.app.ui.formatVisit
import tr.hizmet.app.ui.parseTime
import tr.hizmet.app.ui.theme.ConfirmedBg
import tr.hizmet.app.ui.theme.ConfirmedText
import tr.hizmet.app.ui.theme.Field
import tr.hizmet.app.ui.theme.Muted
import tr.hizmet.app.ui.theme.PendingBg
import tr.hizmet.app.ui.theme.PendingText
import tr.hizmet.app.ui.theme.Star

@Composable
fun AppointmentsScreen(
    openSpecialist: (Int) -> Unit,
    openCatalog: () -> Unit,
    viewModel: AppointmentsViewModel = viewModel(factory = AppointmentsViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbar.current
    val context = LocalContext.current
    var toCancel by remember { mutableStateOf<Booking?>(null) }

    // Reload every time the screen is opened, e.g. right after a new booking.
    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.messageShown()
            snackbar.showSnackbar(context.getString(it))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(stringResource(R.string.my_appointments), style = MaterialTheme.typography.headlineSmall)
        }
        item {
            SegmentedTabs(
                options = listOf(stringResource(R.string.tab_upcoming), stringResource(R.string.tab_past)),
                selected = state.tab,
                onSelect = viewModel::selectTab,
            )
        }

        when {
            state.loading && state.upcoming.isEmpty() && state.past.isEmpty() -> item { LoadingBox() }
            state.error -> item { ErrorBox(onRetry = viewModel::load) }
            state.tab == 0 -> {
                if (state.upcoming.isEmpty()) {
                    item { EmptyText(stringResource(R.string.no_upcoming), openCatalog) }
                }
                items(state.upcoming, key = { it.id }) { booking ->
                    UpcomingCard(
                        booking = booking,
                        onCancel = { toCancel = booking },
                        onDetails = { openSpecialist(booking.specialist.id) },
                    )
                }
                if (state.recentlyCompleted.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.recently_completed),
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(state.recentlyCompleted, key = { "done-${it.id}" }) { booking ->
                        CompletedCard(booking, onRate = { viewModel.rate(booking, it) })
                    }
                }
            }
            else -> {
                if (state.past.isEmpty()) {
                    item { EmptyText(stringResource(R.string.no_past), null) }
                }
                items(state.past, key = { it.id }) { booking ->
                    CompletedCard(booking, onRate = { viewModel.rate(booking, it) })
                }
            }
        }
    }

    toCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { toCancel = null },
            title = { Text(stringResource(R.string.cancel_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.cancel_text,
                        booking.specialist.name,
                        formatVisit(booking.startsAt, booking.endsAt, currentLocale()),
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.cancel(booking); toCancel = null }) {
                    Text(stringResource(R.string.yes_cancel), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { toCancel = null }) { Text(stringResource(R.string.no)) }
            },
        )
    }
}

@Composable
private fun EmptyText(text: String, onFind: (() -> Unit)?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text(text, color = Muted, textAlign = TextAlign.Center)
        if (onFind != null) {
            TextButton(onClick = onFind) { Text(stringResource(R.string.find_specialist)) }
        }
    }
}

@Composable
private fun StatusLabel(status: String) {
    when (status) {
        "PENDING" -> StatusChip(stringResource(R.string.status_pending), PendingBg, PendingText)
        "CONFIRMED" -> StatusChip(stringResource(R.string.status_confirmed), ConfirmedBg, ConfirmedText)
        "COMPLETED" -> StatusChip(stringResource(R.string.status_completed), Field, MaterialTheme.colorScheme.onSurface)
        "REJECTED" -> StatusChip(stringResource(R.string.status_rejected), Field, Muted)
        else -> StatusChip(stringResource(R.string.status_cancelled), Field, Muted)
    }
}

@Composable
private fun UpcomingCard(booking: Booking, onCancel: () -> Unit, onDetails: () -> Unit) {
    CardBox {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusLabel(booking.status)
                Spacer(Modifier.weight(1f))
                Text(formatPrice(booking.price), style = MaterialTheme.typography.titleSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                Avatar(booking.specialist.name, booking.specialist.avatarColor, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(booking.specialist.name, style = MaterialTheme.typography.titleSmall)
                    Text(booking.service.name, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    formatVisit(booking.startsAt, booking.endsAt, currentLocale()),
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(40.dp),
                ) { Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface) }
                FilledTonalButton(
                    onClick = onDetails,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(40.dp),
                ) { Text(stringResource(R.string.details)) }
            }
        }
    }
}

@Composable
private fun CompletedCard(booking: Booking, onRate: (Int) -> Unit) {
    val locale = currentLocale()
    CardBox {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(booking.specialist.name, booking.specialist.avatarColor, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.specialist.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${booking.service.name} · ${formatDay(parseTime(booking.startsAt), locale)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                StatusLabel(booking.status)
            }
            if (booking.status == "COMPLETED") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        stringResource(R.string.rate_visit),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    val rating = booking.review?.rating ?: 0
                    repeat(5) { i ->
                        val filled = i < rating
                        Icon(
                            imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = "${i + 1}",
                            tint = if (filled) Star else Muted,
                            modifier = Modifier
                                .size(26.dp)
                                // A visit can be rated only once.
                                .clickable(enabled = booking.review == null) { onRate(i + 1) },
                        )
                    }
                }
            }
        }
    }
}
