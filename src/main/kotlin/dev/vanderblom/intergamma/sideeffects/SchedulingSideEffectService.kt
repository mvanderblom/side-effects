package dev.vanderblom.intergamma.sideeffects

import kotlinx.coroutines.Dispatchers
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Stack

@Component
class SchedulingSideEffectService {

  private val sideEffects = Stack<() -> Unit>()

  var successfulSideEffects = 0;
  var brokenSideEffects = 0

  fun registerSideEffect( block: () -> Unit) {
    sideEffects.push(block)
  }

  @Scheduled(fixedRate = 1_000)
  fun runSideEffects() {
    while (!sideEffects.empty()){
      val sideEffect = sideEffects.pop()
      try {
        sideEffect.invoke()
        successfulSideEffects++
      } catch (e: Exception) {
        brokenSideEffects++
        throw e
      }
    }
  }
}

