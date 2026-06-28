package com.example.frontend.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.frontend.ui.components.MainLayoutScaffold
import com.example.frontend.ui.screens.auth.*
import com.example.frontend.ui.screens.search.*
import com.example.frontend.ui.screens.home.HomeScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    searchViewModel: SearchFlowViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentSession by authViewModel.currentSession.collectAsState()

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val isCustomerRoute = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.SearchResults.route,
        Screen.VehicleDetails.route
    )

    val startDestination = if (currentSession != null) {
        Screen.Home.route
    } else {
        Screen.SignIn.route
    }

    if (isCustomerRoute) {
        MainLayoutScaffold(
            navController = navController,
            userName = currentSession?.username ?: "Shreyas",
            userEmail = currentSession?.email ?: "shreyas@rentaride.com",
            onSignOut = {
                authViewModel.signOut()
                navController.navigate(Screen.SignIn.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        ) { innerPadding ->
            CustomerNavHost(
                navController = navController,
                startDestination = startDestination,
                authViewModel = authViewModel,
                searchViewModel = searchViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    } else {
        CustomerNavHost(
            navController = navController,
            startDestination = startDestination,
            authViewModel = authViewModel,
            searchViewModel = searchViewModel,
            modifier = modifier
        )
    }
}

@Composable
fun CustomerNavHost(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel,
    searchViewModel: SearchFlowViewModel,
    modifier: Modifier = Modifier
) {
    val currentSession by authViewModel.currentSession.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(
                viewModel = authViewModel,
                onNavigateToUser = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToVendor = {
                    // Mapped to placeholder / vendor dashboard not active in this module
                },
                onNavigateToAdmin = {
                    // Mapped to placeholder / admin dashboard not active in this module
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToVendorSignIn = {
                    // Vendor auth not active in this module
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateToSignIn = {
                    navController.popBackStack()
                },
                onNavigateToUser = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = searchViewModel,
                username = currentSession?.username
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController
            )
        }

        composable(Screen.SearchResults.route) {
            SearchResultsScreen(
                navController = navController,
                viewModel = searchViewModel
            )
        }

        composable(
            route = Screen.VehicleDetails.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("id") ?: ""
            VehicleDetailsScreen(
                navController = navController,
                vehicleId = vehicleId,
                searchFlowViewModel = searchViewModel
            )
        }
    }
}
