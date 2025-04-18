package uz.oybek.wedriveevaluation.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.oybek.wedriveevaluation.data.repository.AppRepository
import uz.oybek.wedriveevaluation.data.repository.RepoResult

data class PhoneEntryState(
    val phoneNumber: String = "+998",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class PhoneEntryEffect {
    data object NavigateToWallet : PhoneEntryEffect()
}

class PhoneEntryViewModel(private val repository: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(PhoneEntryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<PhoneEntryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onPhoneChanged(newPhone: String) {
        val filtered = newPhone.filter { it.isDigit() || it == '+' }
        _state.update { it.copy(phoneNumber = filtered, error = null) }
    }

    fun submitPhone() {
        val phone = state.value.phoneNumber
        if (!phone.startsWith("+998") || phone.length != 13) {
            _state.update { it.copy(error = "Telefon raqam formati noto'g'ri (+998XXXXXXXXX)") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.createUser(phone)) {
                is RepoResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(PhoneEntryEffect.NavigateToWallet)
                }
                is RepoResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}