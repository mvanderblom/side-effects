package dev.vanderblom.intergamma.sideeffects.scheduling

import dev.vanderblom.intergamma.sideeffects.BusinessService
import org.springframework.stereotype.Component

@Component
class SchedulingExampleController (
  private val businessService: BusinessService,
  private val testableSchedulingSideEffectService: TestableSchedulingSideEffectService
) {

  fun someEndpoint() {
    businessService.mainWorkload()
    testableSchedulingSideEffectService.registerSideEffect {
      println("Some Side effect")
    }
    testableSchedulingSideEffectService.registerSideEffect {
      println("Some Other Side effect")
    }
  }

  fun endpointWithBadSideEffect() {
    businessService.mainWorkload()
    testableSchedulingSideEffectService.registerSideEffect {
      println("Some Side effect")
    }
    testableSchedulingSideEffectService.registerSideEffect {
      throw IllegalStateException("I'm so baaaad!")
    }
  }

  fun endpointWithSideEffectsThatTakeTime() {
    businessService.mainWorkload()
    testableSchedulingSideEffectService.registerSideEffect {
      Thread.sleep(500)
      println("Some Side effect")
    }
    testableSchedulingSideEffectService.registerSideEffect {
      Thread.sleep(500)
      println("Some Side effect")
    }
  }

}

