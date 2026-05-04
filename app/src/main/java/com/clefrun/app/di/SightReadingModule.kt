package com.clefrun.app.di

import com.clefrun.app.feature.sightreading.DefaultExerciseXmlGenerator
import com.clefrun.app.feature.sightreading.ExerciseXmlGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SightReadingModule {
    @Binds
    abstract fun bindExerciseXmlGenerator(
        generator: DefaultExerciseXmlGenerator,
    ): ExerciseXmlGenerator
}
