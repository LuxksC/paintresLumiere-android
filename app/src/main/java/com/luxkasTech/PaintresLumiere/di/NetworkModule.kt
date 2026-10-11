package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.BuildConfig
import com.luxkasTech.PaintresLumiere.core.network.AuthInterceptor
import com.luxkasTech.PaintresLumiere.core.network.UnauthorizedInterceptor
import com.luxkasTech.PaintresLumiere.core.network.mock.AssetMockSource
import com.luxkasTech.PaintresLumiere.core.network.mock.MockInterceptor
import com.luxkasTech.PaintresLumiere.core.network.mock.MockRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            redactHeader("Authorization")
                        },
                    )
                }
                // `mock` flavor: answer from assets/mocks instead of the network. Added last so the
                // whole chain above (auth, 401 handling, logging) behaves exactly as in `live`.
                if (BuildConfig.USE_MOCKS) {
                    addInterceptor(MockInterceptor(MockRouter(AssetMockSource(context))))
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
