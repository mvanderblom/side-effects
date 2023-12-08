package dev.vanderblom.intergamma.sideeffects.events

import org.assertj.core.api.Assertions
import org.assertj.core.data.Offset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.system.measureTimeMillis

@SpringBootTest
class EventsExampleControllerTest {
    @Autowired
    private lateinit var controller: EventsExampleController
    @Autowired
    private lateinit var sideEffectEventListener: SideEffectEventListener

    @BeforeEach
    fun setUp() {
        sideEffectEventListener.successfulSideEffects = 0
    }

    @Test
    fun `workload gets executed outside of main thread`() {
        val mainThreadExecTime = measureTimeMillis {
            controller.someEndpoint()
        }

        Assertions.assertThat(mainThreadExecTime)
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

        Assertions.assertThat(mainThreadExecTime)
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

        Assertions.assertThat(mainThreadExecTime)
            .isLessThan(10)

        Thread.sleep(600)

        assertSuccessfulSideEffects(2)
    }

    private fun assertSuccessfulSideEffects(expected: Int) {
        Assertions.assertThat(sideEffectEventListener.successfulSideEffects)
            .withFailMessage("expected $expected side effect(s) to be successful, but found ${sideEffectEventListener.successfulSideEffects}")
            .isEqualTo(expected)
    }

    private fun assertBrokenSideEffects(expected: Int) {
        Assertions.assertThat(sideEffectEventListener.brokenSideEffects)
            .withFailMessage("expected $expected side effect(s) to be broken, but found ${sideEffectEventListener.brokenSideEffects}")
            .isEqualTo(expected)
    }

}