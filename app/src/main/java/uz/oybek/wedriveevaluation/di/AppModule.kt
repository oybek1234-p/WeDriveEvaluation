package uz.oybek.wedriveevaluation.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import uz.oybek.wedriveevaluation.data.remote.api.ApiService
import uz.oybek.wedriveevaluation.data.remote.api.ApiServiceImpl
import uz.oybek.wedriveevaluation.data.repository.AppRepository
import uz.oybek.wedriveevaluation.presentation.viewModel.PhoneEntryViewModel
import uz.oybek.wedriveevaluation.presentation.viewModel.WalletViewModel
import uz.oybek.wedriveevaluation.presentation.viewModel.AddCardViewModel

const val PREFS_NAME = "wedrive_evaluation_prefs"
const val PREF_KEY_PHONE = "user_phone_number"

val appModule = module {

    single<SharedPreferences> {
        androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    single<HttpClient> {
        val prefs: SharedPreferences = get()
        HttpClient(Android) {
            defaultRequest {
                url(ApiService.BASE_URL)
                contentType(ContentType.Application.Json)
                val phoneNumber = prefs.getString(PREF_KEY_PHONE, null)
                if (!url.encodedPath.endsWith("/users") && phoneNumber != null) {
                    header("X-Account-Phone", phoneNumber)
                } else if (!url.encodedPath.endsWith("/users")) {
                    Log.w("KtorClient", "Auth header missing for ${url.encodedPath}")
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            install(Logging) { level = LogLevel.BODY }
            engine { connectTimeout = 20_000; socketTimeout = 20_000 }
        }
    }

    single<ApiService> { ApiServiceImpl(get()) }
    single { AppRepository(get(), get()) }
    viewModel { PhoneEntryViewModel(get()) }
    viewModel { WalletViewModel(get()) }
    viewModel { AddCardViewModel(get()) }
}