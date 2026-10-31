package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.payment.DefaultPaymentRepository
import com.luxkasTech.PaintresLumiere.data.payment.PaymentApi
import com.luxkasTech.PaintresLumiere.data.payment.PaymentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentModule {

    @Binds
    abstract fun bindPaymentRepository(impl: DefaultPaymentRepository): PaymentRepository

    companion object {
        @Provides
        @Singleton
        fun providePaymentApi(retrofit: Retrofit): PaymentApi = retrofit.create(PaymentApi::class.java)
    }
}
