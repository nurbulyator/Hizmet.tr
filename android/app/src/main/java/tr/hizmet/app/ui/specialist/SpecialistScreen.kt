package tr.hizmet.app.ui.specialist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import tr.hizmet.app.R
import tr.hizmet.app.data.Review
import tr.hizmet.app.data.Service
import tr.hizmet.app.data.SpecialistDetails
import tr.hizmet.app.ui.components.Avatar
import tr.hizmet.app.ui.components.CardBox
import tr.hizmet.app.ui.components.ErrorBox
import tr.hizmet.app.ui.components.LoadingBox
import tr.hizmet.app.ui.components.PrimaryButton
import tr.hizmet.app.ui.components.SegmentedTabs
import tr.hizmet.app.ui.currentLocale
import tr.hizmet.app.ui.formatDay
import tr.hizmet.app.ui.formatDuration
import tr.hizmet.app.ui.formatPrice
import tr.hizmet.app.ui.formatWorkingHours
import tr.hizmet.app.ui.parseTime
import tr.hizmet.app.ui.theme.Border
import tr.hizmet.app.ui.theme.Field
import tr.hizmet.app.ui.theme.Heart
import tr.hizmet.app.ui.theme.Muted
import tr.hizmet.app.ui.theme.Purple
import tr.hizmet.app.ui.theme.PurpleLight
import tr.hizmet.app.ui.theme.Star

@Composable
fun SpecialistScreen(
    onBack: () -> Unit,
    chooseTime: (specialistId: Int, serviceId: Int) -> Unit,
    viewModel: SpecialistViewModel = viewModel(factory = SpecialistViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val specialist = state.specialist

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            RoundIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Spacer(Modifier.weight(1f))
            if (specialist != null) {
                RoundIconButton(onClick = viewModel::toggleFavorite) {
                    Icon(
                        if (specialist.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorite),
                        tint = if (specialist.isFavorite) Heart else Muted,
                    )
                }
            }
        }

        when {
            specialist == null && state.loading -> LoadingBox()
            specialist == null -> ErrorBox(onRetry = viewModel::load)
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { ProfileHeader(specialist) }
                    item {
                        SegmentedTabs(
                            options = listOf(
                                stringResource(R.string.tab_services),
                                stringResource(R.string.tab_reviews),
                                stringResource(R.string.tab_about),
                            ),
                            selected = state.tab,
                            onSelect = viewModel::selectTab,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                    when (state.tab) {
                        0 -> {
                            items(specialist.services, key = { it.id }) { service ->
                                ServiceRow(
                                    service = service,
                                    selected = service.id == state.selectedServiceId,
                                    onClick = { viewModel.selectService(service.id) },
                                )
                            }
                            item { WorkingHoursLine(specialist) }
                        }
                        1 -> {
                            if (state.reviews.isEmpty()) {
                                item { Text(stringResource(R.string.no_reviews), color = Muted) }
                            }
                            items(state.reviews, key = { it.id }) { ReviewRow(it) }
                        }
                        else -> item { AboutTab(specialist) }
                    }
                }
                val serviceId = state.selectedServiceId
                PrimaryButton(
                    text = stringResource(R.string.choose_time),
                    onClick = { if (serviceId != null) chooseTime(specialist.id, serviceId) },
                    enabled = serviceId != null,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Field),
    ) { content() }
}

@Composable
private fun ProfileHeader(specialist: SpecialistDetails) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Avatar(specialist.name, specialist.avatarColor, size = 76.dp)
        Spacer(Modifier.size(10.dp))
        Text(specialist.name, style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.experience_years, specialist.title, specialist.experienceYears),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = Star, modifier = Modifier.size(16.dp))
            Text(" %.1f  ".format(java.util.Locale.US, specialist.rating), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.reviews_count, specialist.reviewCount), style = MaterialTheme.typography.bodySmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
            Text(" ${specialist.address}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ServiceRow(service: Service, selected: Boolean, onClick: () -> Unit) {
    CardBox(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        borderColor = if (selected) Purple else Border,
    ) {
        Row(
            modifier = Modifier
                .background(if (selected) PurpleLight else MaterialTheme.colorScheme.surface)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, style = MaterialTheme.typography.titleSmall)
                Text(formatDuration(service.durationMin), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                formatPrice(service.price),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
    }
}

@Composable
private fun WorkingHoursLine(specialist: SpecialistDetails) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(R.string.working_hours, formatWorkingHours(specialist.workingHours, currentLocale())),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ReviewRow(review: Review) {
    CardBox {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.clientName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                repeat(5) { i ->
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (i < review.rating) Star else Border,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Text(
                "${review.serviceName} · ${formatDay(parseTime(review.createdAt), currentLocale())}",
                style = MaterialTheme.typography.bodySmall,
            )
            review.comment?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun AboutTab(specialist: SpecialistDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(specialist.about, style = MaterialTheme.typography.bodyMedium)
        Box(Modifier.fillMaxWidth()) {
            Column {
                Text(stringResource(R.string.address), style = MaterialTheme.typography.labelSmall, color = Muted)
                Text(specialist.address, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
        WorkingHoursLine(specialist)
    }
}
