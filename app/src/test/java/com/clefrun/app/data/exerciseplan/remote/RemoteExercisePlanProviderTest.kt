package com.clefrun.app.data.exerciseplan.remote

import com.clefrun.app.domain.exerciseplan.AccidentalDensity
import com.clefrun.app.domain.exerciseplan.ExercisePlanConstraints
import com.clefrun.app.domain.exerciseplan.ExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.ExercisePlanRequest
import com.clefrun.app.domain.exerciseplan.ExercisePlanSource
import com.clefrun.app.domain.exerciseplan.FallbackExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.LeftHandTexture
import com.clefrun.app.domain.exerciseplan.LocalExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.MaxLeap
import com.clefrun.app.domain.exerciseplan.RightHandMotion
import com.clefrun.core.Difficulty
import com.clefrun.core.ExerciseFocus
import java.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class RemoteExercisePlanProviderTest {

    @Test
    fun `successful remote response maps to exercise plan`() = runTest {
        withServer { server ->
            server.enqueue(MockResponse().setResponseCode(200).setBody(successResponseJson()))
            val provider = remoteProvider(server)

            val plan = provider.nextSightReadingPlan(
                ExercisePlanRequest(
                    seed = 12L,
                    difficulty = Difficulty.EASY,
                    targetedPracticeText = "I struggle with accidentals"
                )
            )

            assertEquals("remote-sight-reading-12", plan.id)
            assertEquals(12L, plan.seed)
            assertEquals(ExercisePlanSource.REMOTE, plan.source)
            assertEquals(Difficulty.EASY, plan.difficulty)
            assertEquals(ExerciseFocus.ACCIDENTALS, plan.generatorFocus)
            assertEquals("Accidentals", plan.focus)
            assertEquals("Sharps and Flats", plan.coach.title)
            assertEquals("Accidentals", plan.coach.focusLabel)
            assertEquals(
                "Pay close attention to every accidental; they temporarily alter the pitch of the note.",
                plan.coach.body
            )
            assertEquals("Don't forget to cancel accidentals with a natural sign.", plan.coach.watchOut)
            assertEquals(
                ExercisePlanConstraints(
                    accidentalDensity = AccidentalDensity.LOW,
                    rightHandMotion = RightHandMotion.MOSTLY_STEPWISE,
                    leftHandTexture = LeftHandTexture.SIMPLE_BASS,
                    maxLeap = MaxLeap.THIRD
                ),
                plan.constraints
            )
        }
    }

    @Test
    fun `remote request sends difficulty targeted text and supported focuses`() = runTest {
        withServer { server ->
            server.enqueue(MockResponse().setResponseCode(200).setBody(successResponseJson()))
            val provider = remoteProvider(server)

            provider.nextSightReadingPlan(
                ExercisePlanRequest(
                    seed = 3L,
                    difficulty = Difficulty.HARD,
                    targetedPracticeText = "left hand"
                )
            )

            val request = server.takeRequest()
            val body = defaultJson().decodeFromString(
                RemoteExercisePlanRequestDto.serializer(),
                request.body.readUtf8()
            )

            assertEquals("/exercise-plan", request.path)
            assertEquals("POST", request.method)
            assertEquals("HARD", body.difficulty)
            assertEquals("left hand", body.targetedPracticeText)
            assertEquals(ExerciseFocus.entries.map { it.name }, body.supportedFocuses)
        }
    }

    @Test
    fun `non 2xx remote response fails`() = runTest {
        withServer { server ->
            server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
            val provider = remoteProvider(server)

            assertRemoteFailure {
                provider.nextSightReadingPlan(defaultRequest())
            }
        }
    }

    @Test
    fun `invalid json remote response fails`() = runTest {
        withServer { server ->
            server.enqueue(MockResponse().setResponseCode(200).setBody("not-json"))
            val provider = remoteProvider(server)

            assertRemoteFailure {
                provider.nextSightReadingPlan(defaultRequest())
            }
        }
    }

    @Test
    fun `unknown remote focus fails`() = runTest {
        withServer { server ->
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(successResponseJson().replace("ACCIDENTALS", "CHORDS"))
            )
            val provider = remoteProvider(server)

            assertRemoteFailure {
                provider.nextSightReadingPlan(defaultRequest())
            }
        }
    }

    @Test
    fun `blank remote coach content fails`() = runTest {
        withServer { server ->
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(successResponseJson(title = " ", body = " "))
            )
            val provider = remoteProvider(server)

            assertRemoteFailure {
                provider.nextSightReadingPlan(defaultRequest())
            }
        }
    }

    @Test
    fun `fallback provider returns local plan when remote is disabled`() = runTest {
        val provider = FallbackExercisePlanProvider(
            primary = RemoteExercisePlanProvider(
                config = RemoteExercisePlanConfig(
                    enabled = false,
                    baseUrl = "http://10.0.2.2:8080"
                ),
                api = disabledRemoteApi()
            ),
            fallback = LocalExercisePlanProvider()
        )

        val plan = provider.nextSightReadingPlan(
            ExercisePlanRequest(
                seed = 4L,
                difficulty = Difficulty.MEDIUM,
                targetedPracticeText = "accidentals"
            )
        )

        assertEquals(ExercisePlanSource.LOCAL, plan.source)
        assertEquals(ExerciseFocus.ACCIDENTALS, plan.generatorFocus)
    }

    @Test
    fun `fallback provider returns local plan when primary fails`() = runTest {
        val provider = FallbackExercisePlanProvider(
            primary = ExercisePlanProvider { throw IOException("network down") },
            fallback = LocalExercisePlanProvider()
        )

        val plan = provider.nextSightReadingPlan(
            ExercisePlanRequest(
                seed = 5L,
                difficulty = Difficulty.MEDIUM,
                targetedPracticeText = "left hand"
            )
        )

        assertEquals(ExercisePlanSource.LOCAL, plan.source)
        assertEquals(ExerciseFocus.LEFT_HAND_STABILITY, plan.generatorFocus)
    }

    private fun remoteProvider(server: MockWebServer): RemoteExercisePlanProvider {
        val config = RemoteExercisePlanConfig(
            enabled = true,
            baseUrl = server.url("/").toString()
        )
        return RemoteExercisePlanProvider(
            config = config,
            api = defaultRemoteExercisePlanApi(
                config = config,
                json = Json {
                    ignoreUnknownKeys = true
                }
            )
        )
    }

    private fun disabledRemoteApi(): RemoteExercisePlanApi {
        return object : RemoteExercisePlanApi {
            override suspend fun createExercisePlan(
                request: RemoteExercisePlanRequestDto,
            ): RemoteExercisePlanResponseDto {
                error("Remote API should not be called when provider is disabled.")
            }
        }
    }

    private fun defaultRequest(): ExercisePlanRequest {
        return ExercisePlanRequest(
            seed = 1L,
            difficulty = Difficulty.EASY,
            targetedPracticeText = "accidentals"
        )
    }

    private fun successResponseJson(
        title: String = "Sharps and Flats",
        body: String = "Pay close attention to every accidental; they temporarily alter the pitch of the note.",
    ): String {
        return """
            {
              "focus": "ACCIDENTALS",
              "constraints": {
                "accidentalDensity": "LOW",
                "rightHandMotion": "MOSTLY_STEPWISE",
                "leftHandTexture": "SIMPLE_BASS",
                "maxLeap": "THIRD"
              },
              "coach": {
                "title": "$title",
                "body": "$body",
                "watchOut": "Don't forget to cancel accidentals with a natural sign."
              }
            }
        """.trimIndent()
    }

    private suspend fun withServer(block: suspend (MockWebServer) -> Unit) {
        val server = MockWebServer()
        try {
            block(server)
        } finally {
            server.shutdown()
        }
    }

    private suspend fun assertRemoteFailure(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected RemoteExercisePlanException")
        } catch (_: RemoteExercisePlanException) {
        }
    }
}
