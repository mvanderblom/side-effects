package dev.vanderblom.intergamma.sideeffects.scheduling

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.system.measureTimeMillis

@SpringBootTest
class SchedulingExampleControllerTest {

    @Autowired
    private lateinit var controller: SchedulingExampleController

    @Autowired
    private lateinit var testableSchedulingSideEffectService: TestableSchedulingSideEffectService

    @BeforeEach
    fun setUp() {
        testableSchedulingSideEffectService.successfulSideEffects = 0
        testableSchedulingSideEffectService.brokenSideEffects = 0
        testableSchedulingSideEffectService.latestExecTime = 0L
    }

    @Test
    fun `workload gets executed outside of main thread`() {
        val mainThreadExecTime = measureTimeMillis {
            controller.someEndpoint()
        }

        assertThat(mainThreadExecTime)
            .isLessThan(10)

        Thread.sleep(2_000)
        assertSuccessfulSideEffects(2)
        assertBrokenSideEffects(0)
    }

    @Test
    fun `exceptions in side effects do not impact main thread`() {
        val mainThreadExecTime = measureTimeMillis {
            controller.endpointWithBadSideEffect()
        }

        assertThat(mainThreadExecTime)
            .isLessThan(10)

        Thread.sleep(2_000)

        assertSuccessfulSideEffects(1)
        assertBrokenSideEffects(1)
    }

    @Test
    fun `sideEffects are run concurrently`() {
        val mainThreadExecTime = measureTimeMillis {
            controller.endpointWithSideEffectsThatTakeTime()
        }

        assertThat(mainThreadExecTime)
            .isLessThan(10)

        Thread.sleep(2_000)

        assertSuccessfulSideEffects(2)
        assertThat(testableSchedulingSideEffectService.latestExecTime)
            .isCloseTo(500, Offset.offset(100))
    }

    private fun assertSuccessfulSideEffects(expected: Int) {
        assertThat(testableSchedulingSideEffectService.successfulSideEffects)
            .withFailMessage("expected $expected side effect(s) to be successful, but found ${testableSchedulingSideEffectService.successfulSideEffects}")
            .isEqualTo(expected)
    }

    private fun assertBrokenSideEffects(expected: Int) {
        assertThat(testableSchedulingSideEffectService.brokenSideEffects)
            .withFailMessage("expected $expected side effect(s) to be broken, but found ${testableSchedulingSideEffectService.brokenSideEffects}")
            .isEqualTo(expected)
    }

}