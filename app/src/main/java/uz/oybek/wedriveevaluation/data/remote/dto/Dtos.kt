package uz.oybek.wedriveevaluation.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class CreateUserRequest(val phone: String)
@Serializable data class UserResponse(val message: String? = null)

@Serializable data class WalletResponseDto(
    val balance: Double,
    @SerialName("active_method") val activeMethod: String,
    @SerialName("active_card_id") val activeCardId: Int? = null
)

@Serializable data class CardItemDto(
    val id: Int,
    val number: String,
    @SerialName("expire_date") val expireDate: String
)

@Serializable data class AddCardRequest(
    val number: String,
    @SerialName("expire_date") val expireDate: String
)
@Serializable data class AddCardResponse(val message: String? = null, val card: CardItemDto? = null)

@Serializable data class PromoCodeRequest(val code: String)

@Serializable data class UpdatePaymentMethodRequest(
    @SerialName("active_method") val activeMethod: String,
    @SerialName("active_card_id") val activeCardId: Int? = null
)