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
    object Checkout : Screen("checkout")
    object BookingSuccess : Screen("booking_success")
    object Orders : Screen("orders")
    object VendorSignIn : Screen("vendor_signin")
    object VendorSignUp : Screen("vendor_signup")
    object VendorDashboard : Screen("vendor_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
}
