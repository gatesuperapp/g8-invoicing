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
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSource
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductTaxLocalDataSource
import com.a4a.g8invoicing.data.ProductTaxLocalDataSourceInterface
import com.a4a.g8invoicing.ui.screens.AccountViewModel
import com.a4a.g8invoicing.ui.viewmodels.AlertDialogViewModel
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

    single { DatabaseDriverFactory(androidContext()) }

    single<SqlDriver> { get<DatabaseDriverFactory>().createDriver() }

    single { Database(get()) }

    single<ClientOrIssuerLocalDataSourceInterface> { ClientOrIssuerLocalDataSource(get()) }
    single<ProductLocalDataSourceInterface> { ProductLocalDataSource(get()) }
    single<ProductTaxLocalDataSourceInterface> { ProductTaxLocalDataSource(get()) }
    single<DeliveryNoteLocalDataSourceInterface> { DeliveryNoteLocalDataSource(get()) }
    single<InvoiceLocalDataSourceInterface> { InvoiceLocalDataSource(get()) }
    single<CreditNoteLocalDataSourceInterface> { CreditNoteLocalDataSource(get()) }
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
    viewModel { AccountViewModel() }
}
