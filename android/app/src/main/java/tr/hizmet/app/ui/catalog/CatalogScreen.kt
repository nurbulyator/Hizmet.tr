package tr.hizmet.app.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tr.hizmet.app.R
import tr.hizmet.app.data.Category
import tr.hizmet.app.data.SpecialistItem
import tr.hizmet.app.ui.LocalSnackbar
import tr.hizmet.app.ui.components.Avatar
import tr.hizmet.app.ui.components.CardBox
import tr.hizmet.app.ui.components.ErrorBox
import tr.hizmet.app.ui.components.LoadingBox
import tr.hizmet.app.ui.components.PhotoPlaceholder
import tr.hizmet.app.ui.components.RatingText
import tr.hizmet.app.ui.currentLocale
import tr.hizmet.app.ui.formatPrice
import tr.hizmet.app.ui.theme.Field
import tr.hizmet.app.ui.theme.Heart
import tr.hizmet.app.ui.theme.Muted
import tr.hizmet.app.ui.theme.Purple

@Composable
fun CatalogScreen(
    openSpecialist: (Int) -> Unit,
    viewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Header(state, viewModel::selectCity) }
        item { SearchField(state.query, viewModel::onQueryChange) }
        item {
            Categories(
                categories = state.categories,
                selected = state.category,
                onSelect = viewModel::selectCategory,
            )
        }
        item { SpecialistsHeader(state.sort, viewModel::selectSort) }

        when {
            state.loading && state.specialists.isEmpty() -> item { LoadingBox() }
            state.error -> item { ErrorBox(onRetry = viewModel::loadSpecialists) }
            state.specialists.isEmpty() -> item {
                Text(
                    stringResource(R.string.nothing_found),
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
            else -> items(state.specialists, key = { it.id }) { item ->
                SpecialistCard(
                    item = item,
                    onClick = { openSpecialist(item.id) },
                    onFavorite = { viewModel.toggleFavorite(item) },
                )
            }
        }
    }
}

@Composable
private fun Header(state: CatalogState, onCity: (String?) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val soonText = stringResource(R.string.notifications_soon)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.city), style = MaterialTheme.typography.bodySmall)
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { menuOpen = true },
                ) {
                    Text(state.city ?: stringResource(R.string.all_cities), style = MaterialTheme.typography.titleLarge)
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.all_cities)) },
                        onClick = { menuOpen = false; onCity(null) },
                    )
                    state.cities.forEach { city ->
                        DropdownMenuItem(text = { Text(city) }, onClick = { menuOpen = false; onCity(city) })
                    }
                }
            }
        }
        IconButton(
            onClick = { scope.launch { snackbar.showSnackbar(soonText) } },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Field),
        ) {
            Icon(Icons.Outlined.NotificationsNone, contentDescription = stringResource(R.string.notifications))
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Field)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (query.isEmpty()) Text(stringResource(R.string.search_hint), color = Muted, fontSize = 14.sp)
                inner()
            },
        )
    }
}

@Composable
private fun Categories(categories: List<Category>, selected: String?, onSelect: (String?) -> Unit) {
    val turkish = currentLocale().language == "tr"
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)) {
            Text(stringResource(R.string.categories), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                stringResource(R.string.all),
                color = Purple,
                style = MaterialTheme.typography.bodySmall.copy(color = Purple, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.clickable { onSelect(null) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.forEach { category ->
                val isSelected = category.slug == selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelect(category.slug) },
                ) {
                    PhotoPlaceholder(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .then(
                                if (isSelected) Modifier.border(2.dp, Purple, RoundedCornerShape(12.dp)) else Modifier,
                            ),
                    )
                    Text(
                        text = if (turkish) category.nameTr else category.nameEn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isSelected) Purple else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecialistsHeader(sort: Sort, onSort: (Sort) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Text(stringResource(R.string.specialists), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Field)
                    .clickable { menuOpen = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(stringResource(sort.title()), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                Sort.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(stringResource(option.title())) },
                        onClick = { menuOpen = false; onSort(option) },
                    )
                }
            }
        }
    }
}

private fun Sort.title() = when (this) {
    Sort.RATING -> R.string.sort_rating
    Sort.PRICE -> R.string.sort_price
}

@Composable
private fun SpecialistCard(item: SpecialistItem, onClick: () -> Unit, onFavorite: () -> Unit) {
    val turkish = currentLocale().language == "tr"
    CardBox(modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(item.name, item.avatarColor, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall)
                val category = if (turkish) item.category.nameTr else item.category.nameEn
                Text("$category · ${item.district}", style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(20.dp)) {
                    RatingText(item.rating, item.reviewCount)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.currency_from, formatPrice(item.priceFrom)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(R.string.favorite),
                    tint = if (item.isFavorite) Heart else Muted,
                )
            }
        }
    }
}
