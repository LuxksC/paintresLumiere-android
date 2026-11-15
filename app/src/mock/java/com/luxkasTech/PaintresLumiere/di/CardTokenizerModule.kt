package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.payment.CardInput
import com.luxkasTech.PaintresLumiere.data.payment.CardTokenizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay

/** `mock` flavor: fake token, so the whole card flow runs without a provider. */
@Module
@InstallIn(SingletonComponent::class)
object CardTokenizerModule {
    @Provides
    fun provideCardTokenizer(): CardTokenizer = object : CardTokenizer {
        override suspend fun tokenize(card: CardInput): String {
            delay(600)
            return "mock-card-token-${card.number.takeLast(4)}"
        }
    }
}
