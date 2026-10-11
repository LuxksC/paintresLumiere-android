package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.admin.AdminProductApi
import com.luxkasTech.PaintresLumiere.data.admin.AdminProductRepository
import com.luxkasTech.PaintresLumiere.data.admin.DefaultAdminProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdminModule {

    @Binds
    abstract fun bindAdminProductRepository(impl: DefaultAdminProductRepository): AdminProductRepository

    companion object {
        @Provides
        @Singleton
        fun provideAdminProductApi(retrofit: Retrofit): AdminProductApi = retrofit.create(AdminProductApi::class.java)
    }
}
