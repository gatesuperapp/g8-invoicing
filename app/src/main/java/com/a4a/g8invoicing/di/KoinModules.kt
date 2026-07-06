package com.a4a.g8invoicing.di

import app.cash.sqldelight.db.SqlDriver
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.AlertDialogDataSourceInterface
import com.a4a.g8invoicing.data.AlertDialogLocalDataSource
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSource
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.CreditNoteLocalDataSource
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.DatabaseDriverFactory
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSource
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSource
import com.a4a.g8invoicing.data.CurrencyManager
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSource
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductTaxLocalDataSource
import com.a4a.g8invoicing.data.ProductTaxLocalDataSourceInterface
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.AuthApiClient
import com.a4a.g8invoicing.data.auth.AuthInterceptor
import com.a4a.g8invoicing.data.auth.AuthRepository
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.data.auth.TokenStorage
import com.a4a.g8invoicing.ui.screens.AccountViewModel
import com.a4a.g8invoicing.ui.screens.GStoreViewModel
import com.a4a.g8invoicing.ui.viewmodels.AlertDialogViewModel
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerListViewModel
import com.a4a.g8invoicing.ui.viewmodels.CreditNoteAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.CreditNoteListViewModel
import com.a4a.g8invoicing.ui.viewmodels.DeliveryNoteAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.DeliveryNoteListViewModel
import com.a4a.g8invoicing.ui.viewmodels.InvoiceAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.InvoiceListViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductAddEditViewModel
import com.a4a.g8invoicing.ui.viewmodels.ProductListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Locale Manager (singleton)
    single { LocaleManager() }

    // Currency Manager (singleton)
    single { CurrencyManager() }

    single { DatabaseDriverFactory(androidContext()) }

    single<SqlDriver> { get<DatabaseDriverFactory>().createDriver() }

    single { Database(get()) }

    // Auth
    single { AuthInterceptor(get()) }
    single {
        val interceptor: AuthInterceptor = get()
        val localeManager: LocaleManager = get()
        HttpClient(OkHttp) {
            engine {
                addInterceptor(interceptor)
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            // DefaultRequest re-evaluates its block on every request, so we always
            // send the currently effective locale — the user changing language
            // mid-session is picked up on the next call.
            install(DefaultRequest) {
                header(HttpHeaders.AcceptLanguage, localeManager.effectiveLanguageCode)
            }
        }
    }
    // Encrypted token storage (EncryptedSharedPreferences wrapped as Settings)
    single<Settings> {
        val context = androidContext()
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            "auth_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        SharedPreferencesSettings(prefs)
    }
    single { TokenStorage(get()) }
    single { AuthApiClient(get(), "https://api.the-gate.fr") }
    single { AuthRepository(get(), get(), get()) }
    single { SubscriptionRepository(get(), get(), get()) }
    single { ActivatedModulesRepository(get()) }

    single<ClientOrIssuerLocalDataSourceInterface> { ClientOrIssuerLocalDataSource(get()) }
    single<ProductLocalDataSourceInterface> { ProductLocalDataSource(get()) }
    single<ProductTaxLocalDataSourceInterface> { ProductTaxLocalDataSource(get()) }
    single<DeliveryNoteLocalDataSourceInterface> { DeliveryNoteLocalDataSource(get(), get(), get(), get()) }
    single<InvoiceLocalDataSourceInterface> { InvoiceLocalDataSource(get(), get(), get(), get()) }
    single<CreditNoteLocalDataSourceInterface> { CreditNoteLocalDataSource(get(), get(), get(), get()) }
    single<AlertDialogDataSourceInterface> { AlertDialogLocalDataSource(get()) }

    single { get<Database>().invoiceQueries }
    single { get<Database>().productQueries }
    single { get<Database>().deliveryNoteQueries }
    single { get<Database>().clientOrIssuerQueries }

    // ViewModels
    viewModel { AlertDialogViewModel(get()) }
    viewModel { ClientOrIssuerListViewModel(get()) }
    viewModel { params ->
        val itemId: String? = if (params.size() > 0) params[0] else null
        val type: String? = if (params.size() > 1) params[1] else null
        ClientOrIssuerAddEditViewModel(get(), itemId, type)
    }
    viewModel { ProductListViewModel(get()) }
    viewModel { params ->
        val itemId: String? = if (params.size() > 0) params[0] else null
        val type: String? = if (params.size() > 1) params[1] else null
        ProductAddEditViewModel(get(), get(), get(), itemId, type)
    }
    viewModel { DeliveryNoteListViewModel(get(), get()) }
    viewModel { params ->
        val itemId: String? = if (params.size() > 0) params[0] else null
        DeliveryNoteAddEditViewModel(get(), get(), itemId)
    }
    viewModel { InvoiceListViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { params ->
        val itemId: String? = if (params.size() > 0) params[0] else null
        InvoiceAddEditViewModel(get(), get(), itemId)
    }
    viewModel { CreditNoteListViewModel(get()) }
    viewModel { params ->
        val itemId: String? = if (params.size() > 0) params[0] else null
        CreditNoteAddEditViewModel(get(), get(), itemId)
    }
    viewModel { AccountViewModel(get(), get()) }
    viewModel { GStoreViewModel(get(), get()) }
}
