package dev.vanderblom.intergamma.sideeffects.scheduling

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Stack

@Component
class BasicSchedulingSideEffectService {

  private val sideEffects = Stack<() -> Unit>()

  fun registerSideEffect(sideEffect: () -> Unit) {
    sideEffects.push(sideEffect)
  }

  @Scheduled(fixedRate = 1_000)
  fun runSideEffects() {
    while (!sideEffects.empty()){
      runSideEffect(sideEffects.pop())
    }
  }

  private fun runSideEffect(sideEffect: () -> Unit) {
    try {
      sideEffect.invoke()
    } catch (e: Exception) {
      e.printStackTrace() // this should be a log statement off course
    }
  }
}

