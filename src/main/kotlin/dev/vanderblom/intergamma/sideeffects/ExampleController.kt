package dev.vanderblom.intergamma.sideeffects

import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class ExampleController (
  private val businessService: BusinessService,
  private val schedulingSideEffectService: SchedulingSideEffectService
) {

  fun someEndpoint() {
    businessService.mainWorkload()
    schedulingSideEffectService.registerSideEffect {
      println("Some Side effect")
    }
    schedulingSideEffectService.registerSideEffect {
      println("Some Other Side effect")
    }
  }

  fun endpointWithBadSideEffect() {
    businessService.mainWorkload()
    schedulingSideEffectService.registerSideEffect {
      println("Some Side effect")
    }
    schedulingSideEffectService.registerSideEffect {
      throw IllegalStateException("I'm so baaaad!")
    }
  }

  fun endpointWithSideEffectsThatTakeTime() {
    businessService.mainWorkload()
    schedulingSideEffectService.registerSideEffect {
      Thread.sleep(500)
      println("Some Side effect")
    }
    schedulingSideEffectService.registerSideEffect {
      Thread.sleep(500)
      println("Some Side effect")
    }
  }

}

