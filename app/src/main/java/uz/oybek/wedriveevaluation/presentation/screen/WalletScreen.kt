package uz.oybek.wedriveevaluation.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import uz.oybek.wedriveevaluation.presentation.viewModel.WalletEffect
import uz.oybek.wedriveevaluation.presentation.viewModel.WalletViewModel


const val CARD_ADDED_SUCCESS_KEY = "card_added_success"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = koinViewModel(),
    navController: NavController,
    onNavigateToAddCard: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
                CARD_ADDED_SUCCESS_KEY
            )?.observe(navController.currentBackStackEntry!!) { success ->
                if (success) {
                    viewModel.loadWalletData()
                    navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>(
                        CARD_ADDED_SUCCESS_KEY
                    )
                }
            }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is WalletEffect.NavigateToAddCard -> onNavigateToAddCard()
                is WalletEffect.ShowToast -> Toast.makeText(
                    context, effect.message, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Wallet") },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
    }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.walletData == null -> CircularProgressIndicator(
                    Modifier.align(
                        Alignment.Center
                    )
                )

                state.error != null && state.walletData == null -> ErrorState(
                    state.error ?: "Xatolik",
                    viewModel::loadWalletData,
                    Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )

                state.walletData != null -> WalletContent(
                    walletData = state.walletData!!,
                    isLoading = state.isLoading,
                    isUpdatingPayment = state.isUpdatingPayment,
                    generalError = state.error,
                    onPaymentMethodSelected = viewModel::onPaymentMethodSelected,
                    onAddCard = viewModel::onAddCardClicked,
                    onAddPromoCode = viewModel::showPromoSheet,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (state.showPromoSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.dismissPromoSheet() },
                ) {
                    PromoCodeSheetContent(
                        code = state.promoCodeInput,
                        error = state.promoCodeError,
                        isLoading = state.isApplyingPromo,
                        onCodeChange = viewModel::onPromoCodeChanged,
                        onApply = viewModel::applyPromoCode,
                        onDismiss = viewModel::dismissPromoSheet
                    )
                }
            }
        }
    }
}