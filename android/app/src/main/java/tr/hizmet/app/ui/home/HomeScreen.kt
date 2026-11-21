package tr.hizmet.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import tr.hizmet.app.R
import tr.hizmet.app.ui.Routes
import tr.hizmet.app.ui.components.Avatar
import tr.hizmet.app.ui.components.CardBox
import tr.hizmet.app.ui.components.ErrorBox
import tr.hizmet.app.ui.components.LoadingBox
import tr.hizmet.app.ui.components.RatingText
import tr.hizmet.app.ui.components.PrimaryButton
import tr.hizmet.app.ui.currentLocale
import tr.hizmet.app.ui.formatVisit
import tr.hizmet.app.ui.theme.Muted
import tr.hizmet.app.ui.theme.Purple

@Composable
fun HomeScreen(
    openSpecialist: (Int) -> Unit,
    openTab: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(stringResource(R.string.hello), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.hello_subtitle), style = MaterialTheme.typography.bodySmall)
        }

        if (state.loading && state.favorites.isEmpty() && state.nextBooking == null) {
            item { LoadingBox() }
            return@LazyColumn
        }
        if (state.error) {
            item { ErrorBox(onRetry = viewModel::load) }
            return@LazyColumn
        }

        state.nextBooking?.let { booking ->
            item { SectionTitle(stringResource(R.string.next_visit)) }
            item {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { openTab(Routes.APPOINTMENTS) }
                        .padding(0.dp),
                ) {
                    androidx.compose.material3.Surface(color = Purple, shape = RoundedCornerShape(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                            Avatar(booking.specialist.name, "#FFFFFF33", size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(booking.specialist.name, color = Color.White, style = MaterialTheme.typography.titleSmall)
                                Text(booking.service.name, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        formatVisit(booking.startsAt, booking.endsAt, currentLocale()),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.favorites)) }
        if (state.favorites.isEmpty()) {
            item { Text(stringResource(R.string.no_favorites), color = Muted, style = MaterialTheme.typography.bodySmall) }
        }
        items(state.favorites, key = { it.id }) { item ->
            CardBox(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { openSpecialist(item.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Avatar(item.name, item.avatarColor, size = 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(item.name, style = MaterialTheme.typography.titleSmall)
                        Text("${item.title} · ${item.district}", style = MaterialTheme.typography.bodySmall)
                        RatingText(item.rating, item.reviewCount)
                    }
                }
            }
        }

        item {
            PrimaryButton(
                text = stringResource(R.string.find_specialist),
                onClick = { openTab(Routes.CATALOG) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
}
