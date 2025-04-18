package uz.oybek.wedriveevaluation.data.remote.api

import uz.oybek.wedriveevaluation.data.remote.dto.*
import io.ktor.client.statement.*

interface ApiService {
    companion object {
        const val BASE_URL = "https://wedrive-assignment-api.onrender.com"
    }

    suspend fun createUser(request: CreateUserRequest): HttpResponse
    suspend fun getWalletDetails(): WalletResponseDto
    suspend fun getCardList(): List<CardItemDto>
    suspend fun addCard(request: AddCardRequest): HttpResponse
    suspend fun applyPromoCode(request: PromoCodeRequest): HttpResponse
    suspend fun updatePaymentMethod(request: UpdatePaymentMethodRequest): HttpResponse
}