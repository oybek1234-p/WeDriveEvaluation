package uz.oybek.wedriveevaluation.domain.model

import java.text.NumberFormat
import java.util.Locale

data class WalletData(
    val balance: Double = 0.0,
    val cards: List<PaymentMethod.Card> = emptyList(),
    val activeMethod: PaymentMethod = PaymentMethod.Unknown
) {
    val balanceFormatted: String
        get() = try {
            NumberFormat.getCurrencyInstance(Locale("uz", "UZ")).format(balance)
        } catch (_: Exception) {
            balance.toString()
        }

    val paymentMethods: List<PaymentMethod>
        get() = listOf(PaymentMethod.Cash) + cards
}

sealed class PaymentMethod {
    data object Cash : PaymentMethod()
    data class Card(
        val id: Int,
        val last4: String,
        val displayNumber: String,
        val expiryDate: String
    ) : PaymentMethod()
    data object Unknown : PaymentMethod()
}