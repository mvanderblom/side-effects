package dev.vanderblom.intergamma.sideeffects

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.system.measureTimeMillis

@SpringBootTest
class SideeffectsApplicationTests {

	@Test
	fun contextLoads() {
	}

	suspend fun takeSomeTime(){
		delay(1_000)
	}

	@Test
	fun `test coroutines run in parallel`() {
		var counter = 0

		val execTime = measureTimeMillis {
			runBlocking {
				launch {
					takeSomeTime()
					counter++
				}
				launch {
					takeSomeTime()
					counter++
				}
			}
		}

		assertThat(execTime)
			.isCloseTo(1_000, Offset.offset(100))
		assertThat(counter)
			.isEqualTo(2)
	}
}
