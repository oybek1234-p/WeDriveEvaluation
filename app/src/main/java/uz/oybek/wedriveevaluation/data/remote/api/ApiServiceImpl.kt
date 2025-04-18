package uz.oybek.wedriveevaluation.data.remote.api

import uz.oybek.wedriveevaluation.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class ApiServiceImpl(private val client: HttpClient) : ApiService {

    override suspend fun createUser(request: CreateUserRequest): HttpResponse =
        client.post("/users") { setBody(request) }

    override suspend fun getWalletDetails(): WalletResponseDto =
        client.get("/wallet").body()

    override suspend fun getCardList(): List<CardItemDto> =
        client.get("/cards").body()

    override suspend fun addCard(request: AddCardRequest): HttpResponse =
        client.post("/cards") { setBody(request) }

    override suspend fun applyPromoCode(request: PromoCodeRequest): HttpResponse =
        client.post("/promocode") { setBody(request) }

    override suspend fun updatePaymentMethod(request: UpdatePaymentMethodRequest): HttpResponse =
        client.put("/wallet/method") { setBody(request) }
}