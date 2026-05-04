package com.clefrun.app.data.exerciseplan.remote

import kotlinx.serialization.Serializable

@Serializable
data class RemoteExercisePlanRequestDto(
    val difficulty: String,
    val targetedPracticeText: String?,
    val supportedFocuses: List<String>,
)

@Serializable
data class RemoteExercisePlanResponseDto(
    val focus: RemoteExerciseFocusDto,
    val constraints: RemoteExercisePlanConstraintsDto,
    val coach: RemoteCoachDto,
)

@Serializable
data class RemoteExercisePlanConstraintsDto(
    val accidentalDensity: RemoteAccidentalDensityDto,
    val rightHandMotion: RemoteRightHandMotionDto,
    val leftHandTexture: RemoteLeftHandTextureDto,
    val maxLeap: RemoteMaxLeapDto,
)

@Serializable
data class RemoteCoachDto(
    val title: String,
    val body: String,
    val watchOut: String? = null,
)

@Serializable
enum class RemoteExerciseFocusDto {
    READ_AHEAD,
    LEFT_HAND_STABILITY,
    ACCIDENTALS,
    SMALL_LEAPS
}

@Serializable
enum class RemoteAccidentalDensityDto {
    NONE,
    LOW,
    MEDIUM
}

@Serializable
enum class RemoteRightHandMotionDto {
    MOSTLY_STEPWISE,
    STEPWISE_WITH_SMALL_LEAPS
}

@Serializable
enum class RemoteLeftHandTextureDto {
    SIMPLE_BASS,
    STEADY_BASS
}

@Serializable
enum class RemoteMaxLeapDto {
    SECOND,
    THIRD,
    FOURTH
}
