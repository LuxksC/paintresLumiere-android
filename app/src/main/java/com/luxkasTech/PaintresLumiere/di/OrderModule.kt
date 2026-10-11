package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.order.DefaultOrderRepository
import com.luxkasTech.PaintresLumiere.data.order.OrderApi
import com.luxkasTech.PaintresLumiere.data.order.OrderRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrderModule {

    @Binds
    abstract fun bindOrderRepository(impl: DefaultOrderRepository): OrderRepository

    companion object {
        @Provides
        @Singleton
        fun provideOrderApi(retrofit: Retrofit): OrderApi = retrofit.create(OrderApi::class.java)
    }
}
