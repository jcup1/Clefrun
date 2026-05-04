package com.clefrun.app.di

import com.clefrun.app.feature.scales.DefaultTechnicalPracticeXmlGenerator
import com.clefrun.app.feature.scales.TechnicalPracticeXmlGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TechnicalPracticeModule {
    @Binds
    abstract fun bindTechnicalPracticeXmlGenerator(
        generator: DefaultTechnicalPracticeXmlGenerator,
    ): TechnicalPracticeXmlGenerator
}
