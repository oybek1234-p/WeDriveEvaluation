package uz.oybek.wedriveevaluation.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.oybek.wedriveevaluation.data.repository.AppRepository
import uz.oybek.wedriveevaluation.data.repository.RepoResult
import java.util.Calendar

data class AddCardState(
    val cardNumber: String = "",
    val expiryInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInputPotentiallyValid: Boolean = false
)

sealed class AddCardEffect {
    data class NavigateBack(val success: Boolean) : AddCardEffect()
}

class AddCardViewModel(private val repository: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(AddCardState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AddCardEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onCardNumberChanged(number: String) {
        val digits = number.filter { it.isDigit() }.take(16)
        _state.update {
            it.copy(
                cardNumber = digits,
                error = null,
                isInputPotentiallyValid = checkPotentialValidity(digits, it.expiryInput)
            )
        }
    }

    fun onExpiryDateChanged(date: String) {
        val expiryDigits = date.filter { it.isDigit() }.take(4)
        _state.update {
            it.copy(
                expiryInput = expiryDigits,
                error = null,
                isInputPotentiallyValid = checkPotentialValidity(it.cardNumber, expiryDigits)
            )
        }
    }

    fun saveCard() {
        val currentState = state.value
        val cardDigits = currentState.cardNumber
        val expiryDigits = currentState.expiryInput

        if (!validateInputs(cardDigits, expiryDigits)) {
            return
        }

        val expiryFormattedForApi = "${expiryDigits.substring(0, 2)}/${expiryDigits.substring(2)}"

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when(val result = repository.addCard(cardDigits, expiryFormattedForApi)) {
                is RepoResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(AddCardEffect.NavigateBack(success = true))
                }
                is RepoResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private fun checkPotentialValidity(card: String, expiry: String): Boolean {
        val isValid = card.length == 16 && expiry.length == 4
        Log.d("AddCardViewModel", "checkPotentialValidity: cardLen=${card.length}, expiryLen=${expiry.length}, isValid=$isValid")
        return isValid
    }

    private fun validateInputs(cardDigits: String, expiryDigits: String): Boolean {
        Log.d("AddCardViewModel", "Validating Inputs: Card=$cardDigits, Expiry=$expiryDigits")
        if (cardDigits.length != 16) {
            _state.update { it.copy(error = "Karta raqami 16 ta raqamdan iborat bo'lishi kerak.") }
            return false
        }
        if (expiryDigits.length != 4) {
            _state.update { it.copy(error = "Amal qilish muddati to'liq kiritilmagan (OOYY).") }
            return false
        }
        try {
            val month = expiryDigits.substring(0, 2).toInt()
            val year = expiryDigits.substring(2, 4).toInt()

            val currentYearFull = Calendar.getInstance().get(Calendar.YEAR)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentYearLastTwoDigits = currentYearFull % 100

            if (month < 1 || month > 12) {
                _state.update { it.copy(error = "Oy raqami noto'g'ri (01-12).") }
                Log.w("AddCardViewModel", "Validation Failed: Invalid month $month")
                return false
            }
            if (year < currentYearLastTwoDigits || (year == currentYearLastTwoDigits && month < currentMonth)) {
                _state.update { it.copy(error = "Kartaning amal qilish muddati o'tib ketgan.") }
                Log.w("AddCardViewModel", "Failed")
                return false
            }
            if (year > currentYearLastTwoDigits + 15) {
                _state.update { it.copy(error = "Amal qilish muddati juda uzoq.") }
                return false
            }

        } catch (e: Exception) {
            _state.update { it.copy(error = "Amal qilish muddatini tekshirishda xatolik.") }
            return false
        }

        _state.update { it.copy(error = null) }
        return true
    }
}