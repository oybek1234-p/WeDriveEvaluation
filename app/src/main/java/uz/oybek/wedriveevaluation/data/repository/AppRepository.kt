package uz.oybek.wedriveevaluation.data.repository

import android.content.SharedPreferences
import android.util.Log
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import uz.oybek.wedriveevaluation.data.remote.api.ApiService
import uz.oybek.wedriveevaluation.data.remote.dto.*
import uz.oybek.wedriveevaluation.di.PREF_KEY_PHONE
import uz.oybek.wedriveevaluation.domain.model.PaymentMethod
import uz.oybek.wedriveevaluation.domain.model.WalletData

sealed class RepoResult<out T> {
    data class Success<out T>(val data: T) : RepoResult<T>()
    data class Error(val message: String) : RepoResult<Nothing>()
}

class AppRepository(
    private val apiService: ApiService, private val prefs: SharedPreferences
) {
    private val tag = "AppRepository"

    fun isUserLoggedIn(): Boolean = prefs.contains(PREF_KEY_PHONE)
    fun getPhoneNumber(): String? = prefs.getString(PREF_KEY_PHONE, null)

    suspend fun createUser(phone: String): RepoResult<Unit> {
        return try {
            val response = apiService.createUser(CreateUserRequest(phone))
            if (response.status.isSuccess()) {
                prefs.edit().putString(PREF_KEY_PHONE, phone).apply()
                Log.i(tag, "User created successfully: $phone")
                RepoResult.Success(Unit)
            } else {
                Log.w(tag, "Create user failed: ${response.status}")
                RepoResult.Error("Ro'yxatdan o'tishda xatolik (${response.status.value})")
            }
        } catch (e: Exception) {
            Log.e(tag, "Create user exception", e)
            handleException(e, "Ro'yxatdan o'tishda xatolik")
        }
    }

    suspend fun getWalletData(): RepoResult<WalletData> {
        return try {
            coroutineScope {
                val walletDeferred = async { apiService.getWalletDetails() }
                val cardsDeferred = async { apiService.getCardList() }

                val walletDto = walletDeferred.await()
                val cardDtoList = cardsDeferred.await()

                val cards = cardDtoList.map {
                    PaymentMethod.Card(it.id, it.number.takeLast(4), it.number, it.expireDate)
                }
                val activeMethod = when (walletDto.activeMethod) {
                    "cash" -> PaymentMethod.Cash
                    "card" -> cards.find { it.id == walletDto.activeCardId }
                        ?: PaymentMethod.Unknown

                    else -> PaymentMethod.Unknown
                }
                val walletData = WalletData(
                    balance = walletDto.balance, cards = cards, activeMethod = activeMethod
                )
                RepoResult.Success(walletData)
            }
        } catch (e: Exception) {
            Log.e(tag, "Get wallet data exception", e)
            handleException(e, "Ma'lumotlarni yuklashda xatolik")
        }
    }

    suspend fun addCard(number: String, expiry: String): RepoResult<Unit> {
        return try {
            val request =
                AddCardRequest(number = number.filter { it.isDigit() }, expireDate = expiry)
            val response = apiService.addCard(request)
            if (response.status.isSuccess()) {
                Log.i(tag, "Card added successfully")
                RepoResult.Success(Unit)
            } else {
                Log.w(tag, "Add card failed: ${response.status}")
                RepoResult.Error("Kartani qo'shishda xatolik (${response.status.value})")
            }
        } catch (e: Exception) {
            Log.e(tag, "Add card exception", e)
            handleException(e, "Kartani qo'shishda xatolik")
        }
    }

    suspend fun applyPromoCode(code: String): RepoResult<Unit> {
        return try {
            val response = apiService.applyPromoCode(PromoCodeRequest(code))
            if (response.status.isSuccess()) {
                Log.i(tag, "Promo code applied")
                RepoResult.Success(Unit)
            } else {
                Log.w(tag, "Apply promo code failed: ${response.status}")
                RepoResult.Error("Promokod xato yoki muddati o'tgan (${response.status.value})")
            }
        } catch (e: Exception) {
            Log.e(tag, "Apply promo code exception", e)
            handleException(e, "Promokodni qo'llashda xatolik")
        }
    }

    suspend fun updatePaymentMethod(method: PaymentMethod): RepoResult<Unit> {
        return try {
            val request = when (method) {
                is PaymentMethod.Cash -> UpdatePaymentMethodRequest("cash", null)
                is PaymentMethod.Card -> UpdatePaymentMethodRequest("card", method.id)
                else -> return RepoResult.Error("Noma'lum to'lov usuli")
            }
            val response = apiService.updatePaymentMethod(request)
            if (response.status.isSuccess()) {
                Log.i(tag, "Payment method updated")
                RepoResult.Success(Unit)
            } else {
                Log.w(tag, "Update payment method failed: ${response.status}")
                RepoResult.Error("To'lov usulini yangilashda xatolik (${response.status.value})")
            }
        } catch (e: Exception) {
            Log.e(tag, "Update payment method exception", e)
            handleException(e, "To'lov usulini yangilashda xatolik")
        }
    }

    private fun handleException(e: Throwable, defaultMsg: String): RepoResult.Error {
        val message = when (e) {
            is RedirectResponseException, is ClientRequestException, is ServerResponseException -> "$defaultMsg (${(e as? ResponseException)?.response?.status?.value ?: "Server"})"

            is HttpRequestTimeoutException -> "Serverga ulanish vaqti tugadi. Keyinroq urinib ko'ring."
            else -> "$defaultMsg (${e.message ?: "Noma'lum xato"})"
        }
        return RepoResult.Error(message)
    }
}