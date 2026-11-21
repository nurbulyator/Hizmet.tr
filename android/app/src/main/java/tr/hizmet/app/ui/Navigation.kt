package tr.hizmet.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import tr.hizmet.app.R
import tr.hizmet.app.ui.appointments.AppointmentsScreen
import tr.hizmet.app.ui.booking.SlotScreen
import tr.hizmet.app.ui.catalog.CatalogScreen
import tr.hizmet.app.ui.home.HomeScreen
import tr.hizmet.app.ui.profile.ProfileScreen
import tr.hizmet.app.ui.specialist.SpecialistScreen
import tr.hizmet.app.ui.theme.Muted
import tr.hizmet.app.ui.theme.PurpleLight

object Routes {
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val APPOINTMENTS = "appointments"
    const val PROFILE = "profile"
    const val SPECIALIST = "specialist/{id}"
    const val SLOTS = "slots/{specialistId}/{serviceId}"

    fun specialist(id: Int) = "specialist/$id"
    fun slots(specialistId: Int, serviceId: Int) = "slots/$specialistId/$serviceId"
}

private data class Tab(val route: String, val title: Int, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, R.string.nav_home, Icons.Outlined.Home),
    Tab(Routes.CATALOG, R.string.nav_catalog, Icons.Outlined.GridView),
    Tab(Routes.APPOINTMENTS, R.string.nav_appointments, Icons.Outlined.CalendarMonth),
    Tab(Routes.PROFILE, R.string.nav_profile, Icons.Outlined.Person),
)

/** Snackbar that every screen can use to show a short message. */
val LocalSnackbar = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun HizmetNavHost() {
    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = tabs.any { it.route == route }

    CompositionLocalProvider(LocalSnackbar provides snackbar) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (showBottomBar) BottomBar(navController, route)
            },
            containerColor = Color.White,
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.CATALOG,
                modifier = Modifier.padding(padding),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        openSpecialist = { navController.navigate(Routes.specialist(it)) },
                        openTab = { navController.openTab(it) },
                    )
                }
                composable(Routes.CATALOG) {
                    CatalogScreen(openSpecialist = { navController.navigate(Routes.specialist(it)) })
                }
                composable(Routes.APPOINTMENTS) {
                    AppointmentsScreen(
                        openSpecialist = { navController.navigate(Routes.specialist(it)) },
                        openCatalog = { navController.openTab(Routes.CATALOG) },
                    )
                }
                composable(Routes.PROFILE) { ProfileScreen() }
                composable(
                    Routes.SPECIALIST,
                    arguments = listOf(navArgument("id") { type = NavType.IntType }),
                ) {
                    SpecialistScreen(
                        onBack = { navController.popBackStack() },
                        chooseTime = { specialistId, serviceId ->
                            navController.navigate(Routes.slots(specialistId, serviceId))
                        },
                    )
                }
                composable(
                    Routes.SLOTS,
                    arguments = listOf(
                        navArgument("specialistId") { type = NavType.IntType },
                        navArgument("serviceId") { type = NavType.IntType },
                    ),
                ) {
                    SlotScreen(
                        onBack = { navController.popBackStack() },
                        onBooked = { navController.openTab(Routes.APPOINTMENTS) },
                    )
                }
            }
        }
    }
}

/** Opens a bottom-bar tab and clears the screens opened on top of it. */
private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = Color.White) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { navController.openTab(tab.route) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = {
                    Text(
                        stringResource(tab.title),
                        fontSize = 11.sp,
                        fontWeight = if (currentRoute == tab.route) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = PurpleLight,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted,
                ),
            )
        }
    }
}
