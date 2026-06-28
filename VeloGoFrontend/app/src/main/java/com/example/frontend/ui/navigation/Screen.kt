package com.example.frontend.ui.navigation

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Search : Screen("search")
    object SearchResults : Screen("search_results")
    object VehicleDetails : Screen("vehicle_details/{id}") {
        fun createRoute(id: String) = "vehicle_details/$id"
    }
}
