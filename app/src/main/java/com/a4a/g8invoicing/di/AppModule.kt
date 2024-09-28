package com.a4a.g8invoicing.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.a4a.g8invoicing.Database
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSource
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.CreditNoteLocalDataSource
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSource
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSource
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.ProductLocalDataSource
import com.a4a.g8invoicing.data.ProductTaxLocalDataSource
import com.a4a.g8invoicing.data.ProductTaxLocalDataSourceInterface
import com.a4a.g8invoicing.data.auth.AuthApi
import com.a4a.g8invoicing.data.auth.AuthRepository
import com.a4a.g8invoicing.data.auth.AuthRepositoryInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton
import retrofit2.converter.moshi.MoshiConverterFactory


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSqlDriver(app: Application): SqlDriver {
        return AndroidSqliteDriver(
            schema = Database.Schema,
            context = app,
            name = "g8_invoicing.db"
        )
    }

    @Provides
    @Singleton
    fun provideClientDataSource(driver: SqlDriver): ClientOrIssuerLocalDataSourceInterface {
        return ClientOrIssuerLocalDataSource(Database(driver))
    }

    @Provides
    @Singleton
    fun provideProductDataSource(driver: SqlDriver): ProductLocalDataSourceInterface {
        return ProductLocalDataSource(Database(driver))
    }

    @Provides
    @Singleton
    fun provideProductTaxDataSource(driver: SqlDriver): ProductTaxLocalDataSourceInterface {
        return ProductTaxLocalDataSource(Database(driver))
    }

    @Provides
    @Singleton
    fun deliveryNoteDataSource(driver: SqlDriver): DeliveryNoteLocalDataSourceInterface {
        return DeliveryNoteLocalDataSource(Database(driver))
    }

    @Provides
    @Singleton
    fun invoiceDataSource(driver: SqlDriver): InvoiceLocalDataSourceInterface {
        return InvoiceLocalDataSource(Database(driver))
    }

    @Provides
    @Singleton
    fun creditNoteDataSource(driver: SqlDriver): CreditNoteLocalDataSourceInterface {
        return CreditNoteLocalDataSource(Database(driver))
    }

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi {
        return Retrofit.Builder()
            .baseUrl("https://g8-api-4zjqp.ondigitalocean.app/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideSharedPref(app: Application): SharedPreferences {
        return app.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }


    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi, prefs: SharedPreferences): AuthRepositoryInterface {
        return AuthRepository(api, prefs)
    }

}