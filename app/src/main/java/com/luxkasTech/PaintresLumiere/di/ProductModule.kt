package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.product.DefaultProductRepository
import com.luxkasTech.PaintresLumiere.data.product.ProductApi
import com.luxkasTech.PaintresLumiere.data.product.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProductModule {

    @Binds
    abstract fun bindProductRepository(impl: DefaultProductRepository): ProductRepository

    companion object {
        @Provides
        @Singleton
        fun provideProductApi(retrofit: Retrofit): ProductApi = retrofit.create(ProductApi::class.java)
    }
}
