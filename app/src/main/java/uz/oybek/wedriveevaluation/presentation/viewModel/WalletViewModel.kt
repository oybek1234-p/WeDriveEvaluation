package uz.oybek.wedriveevaluation.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.oybek.wedriveevaluation.data.repository.AppRepository
import uz.oybek.wedriveevaluation.data.repository.RepoResult
import uz.oybek.wedriveevaluation.domain.model.PaymentMethod
import uz.oybek.wedriveevaluation.domain.model.WalletData

data class WalletScreenState(
    val isLoading: Boolean = true,
    val walletData: WalletData? = null,
    val error: String? = null,
    val isUpdatingPayment: Boolean = false,
    val showPromoSheet: Boolean = false,
    val promoCodeInput: String = "",
    val promoCodeError: String? = null,
    val isApplyingPromo: Boolean = false,
)

sealed class WalletEffect {
    data object NavigateToAddCard : WalletEffect()
    data class ShowToast(val message: String) : WalletEffect()
}


class WalletViewModel(private val repository: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(WalletScreenState())
    val state = _state.asStateFlow()

    private val _effect = Channel<WalletEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadWalletData()
    }

    fun loadWalletData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getWalletData()) {
                is RepoResult.Success -> _state.update { it.copy(isLoading = false, walletData = result.data) }
                is RepoResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        if (method == state.value.walletData?.activeMethod || state.value.isUpdatingPayment) {
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isUpdatingPayment = true, error = null) }
            when (val result = repository.updatePaymentMethod(method)) {
                is RepoResult.Success -> {
                    loadWalletData()
                }
                is RepoResult.Error -> {
                    _state.update { it.copy(isUpdatingPayment = false, error = result.message) }
                    _effect.send(WalletEffect.ShowToast(result.message))
                }
            }
            if (_state.value.isUpdatingPayment) {
                _state.update { it.copy(isUpdatingPayment = false)}
            }
        }
    }

    fun onAddCardClicked() {
        viewModelScope.launch { _effect.send(WalletEffect.NavigateToAddCard) }
    }

    fun showPromoSheet() {
        _state.update { it.copy(showPromoSheet = true, promoCodeError = null, error = null) }
    }
    fun dismissPromoSheet() {
        _state.update { it.copy(showPromoSheet = false, promoCodeInput = "", promoCodeError = null) }
    }
    fun onPromoCodeChanged(code: String) {
        _state.update { it.copy(promoCodeInput = code, promoCodeError = null) }
    }
    fun applyPromoCode() {
        val code = state.value.promoCodeInput
        if (code.isBlank()){
            _state.update { it.copy(promoCodeError = "Promokod kiritilmagan") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isApplyingPromo = true, promoCodeError = null) }
            when(val result = repository.applyPromoCode(code)) {
                is RepoResult.Success -> {
                    _state.update { it.copy(showPromoSheet = false, promoCodeInput = "", promoCodeError = null) }
                    _effect.send(WalletEffect.ShowToast("Promokod qabul qilindi!"))
                    loadWalletData()
                }
                is RepoResult.Error -> _state.update { it.copy(promoCodeError = result.message) }
            }
            _state.update { it.copy(isApplyingPromo = false) }
        }
    }
}