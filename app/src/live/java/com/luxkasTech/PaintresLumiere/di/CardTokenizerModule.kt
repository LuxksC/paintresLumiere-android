package com.luxkasTech.PaintresLumiere.di

import com.luxkasTech.PaintresLumiere.data.payment.CardInput
import com.luxkasTech.PaintresLumiere.data.payment.CardTokenizationUnavailableException
import com.luxkasTech.PaintresLumiere.data.payment.CardTokenizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * `live` flavor: no tokenizer yet. The Asaas client-side tokenization (and its public key) has to
 * be chosen with the backend card PL-147; until then card payment fails gracefully and the user is
 * offered another method.
 */
@Module
@InstallIn(SingletonComponent::class)
object CardTokenizerModule {
    @Provides
    fun provideCardTokenizer(): CardTokenizer = object : CardTokenizer {
        override suspend fun tokenize(card: CardInput): String = throw CardTokenizationUnavailableException()
    }
}
