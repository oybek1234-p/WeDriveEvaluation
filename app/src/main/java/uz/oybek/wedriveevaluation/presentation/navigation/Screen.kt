package uz.oybek.wedriveevaluation.presentation.navigation

sealed class Screen(val route: String) {
    data object PhoneEntry : Screen("phone_entry")
    data object Wallet : Screen("wallet")
    data object AddCard : Screen("add_card")
}