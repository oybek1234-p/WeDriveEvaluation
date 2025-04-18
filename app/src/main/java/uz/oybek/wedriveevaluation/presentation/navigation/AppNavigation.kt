package uz.oybek.wedriveevaluation.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import uz.oybek.wedriveevaluation.data.repository.AppRepository
import uz.oybek.wedriveevaluation.presentation.screen.AddCardScreen
import uz.oybek.wedriveevaluation.presentation.screen.PhoneEntryScreen
import uz.oybek.wedriveevaluation.presentation.screen.WalletScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val repository: AppRepository = koinInject()

    val startDestination = if (repository.isUserLoggedIn()) Screen.Wallet.route else Screen.PhoneEntry.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.PhoneEntry.route) {
            PhoneEntryScreen(
                onNavigateToWallet = {
                    navController.navigate(Screen.Wallet.route) {
                        popUpTo(Screen.PhoneEntry.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Wallet.route) {
            WalletScreen(
                onNavigateToAddCard = { navController.navigate(Screen.AddCard.route) },
                navController = navController
            )
        }
        composable(Screen.AddCard.route) {AddCardScreen(navController = navController)
        }
    }
}