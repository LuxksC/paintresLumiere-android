package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.profile.DefaultProfileRepository
import com.luxkasTech.PaintresLumiere.data.profile.ProfileApi
import com.luxkasTech.PaintresLumiere.data.profile.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    abstract fun bindProfileRepository(impl: DefaultProfileRepository): ProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)
    }
}
