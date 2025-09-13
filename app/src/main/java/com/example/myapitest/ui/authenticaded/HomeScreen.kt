package com.example.myapitest.ui.authenticaded

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapitest.R
import com.example.myapitest.ui.SCREEN_TRANSITION_MILLIS
import com.example.myapitest.ui.authenticaded.cars.Cars
import com.example.myapitest.ui.authenticaded.cars.carsLIst.CarsScreen
import com.example.myapitest.ui.authenticaded.profile.ProfileScreen
import com.example.myapitest.ui.theme.Blue200
import com.example.myapitest.ui.theme.Grey500
import com.example.myapitest.ui.theme.Grey800
import com.example.myapitest.ui.theme.LightGray
import com.example.myapitest.ui.theme.MyApiTestTheme
import com.example.myapitest.ui.theme.White30

sealed class BottomTab(val route: String, val iconRes: Int, val label: String, val index: Int) {
    object Cars : BottomTab("cars", R.drawable.baseline_directions_car_24, "Cars", 0)
    object Profile : BottomTab("profile", R.drawable.baseline_person_24, "Profile", 1)
}

private fun getTabIndex(route: String?): Int {
    return when (route) {
        BottomTab.Cars.route -> 0
        BottomTab.Profile.route -> 1
        else -> 0
    }
}

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }

    val items = listOf(
        BottomTab.Cars,
        BottomTab.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier.background(color = LightGray)
                ) {
                    NavigationBar(
                        containerColor = White30,
                        tonalElevation = 0.dp,
                    ) {
                        items.forEach { tab ->
                            val isSelected = currentRoute == tab.route

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { navController.navigate(tab.route) },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) Grey800 else Color.Transparent,
                                                shape = RoundedCornerShape(40)
                                            )
                                            .padding(vertical = 7.dp, horizontal = 15.dp)
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(id = tab.iconRes),
                                            contentDescription = tab.label,
                                            tint = if (isSelected) Blue200 else Grey500
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        tab.label,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Grey800 else Grey500
                                    )
                                },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Cars.route,
            modifier = Modifier.padding(
                bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp
            )
        ) {
            composable(
                route = BottomTab.Cars.route,
                enterTransition = {
                    val currentIndex = getTabIndex(initialState.destination.route)
                    val targetIndex = getTabIndex(targetState.destination.route)

                    if (targetIndex > currentIndex) {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(SCREEN_TRANSITION_MILLIS)
                        )
                    } else {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(SCREEN_TRANSITION_MILLIS)
                        )
                    }
                },
                exitTransition = {
                    val currentIndex = getTabIndex(initialState.destination.route)
                    val targetIndex = getTabIndex(targetState.destination.route)

                    if (targetIndex > currentIndex) {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(SCREEN_TRANSITION_MILLIS)
                        )
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(SCREEN_TRANSITION_MILLIS)
                        )
                    }
                }
            ) {
                LaunchedEffect(Unit) {
                    showBottomBar = true
                }
                Cars(
                    onBottomBarVisibilityChanged = { visible ->
                        showBottomBar = visible
                    }
                )
            }
            composable(
                route = BottomTab.Profile.route,
                enterTransition = {
                    val currentIndex = getTabIndex(initialState.destination.route)
                    val targetIndex = getTabIndex(targetState.destination.route)

                    if (targetIndex > currentIndex) {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(SCREEN_TRANSITION_MILLIS)
                        )
                    } else {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(SCREEN_TRANSITION_MILLIS)
                        )
                    }
                },
                exitTransition = {
                    val currentIndex = getTabIndex(initialState.destination.route)
                    val targetIndex = getTabIndex(targetState.destination.route)

                    if (targetIndex > currentIndex) {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(SCREEN_TRANSITION_MILLIS)
                        )
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(SCREEN_TRANSITION_MILLIS)
                        )
                    }
                }
            ) {
                LaunchedEffect(Unit) {
                    showBottomBar = true
                }
                ProfileScreen(
                    onLogout = onLogout
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(modifier: Modifier = Modifier) {
    MyApiTestTheme {
        HomeScreen()
    }
}