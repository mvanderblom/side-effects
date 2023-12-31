package dev.vanderblom.intergamma.sideeffects.scheduling

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Stack

@Component
class CoroutineSchedulingSideEffectService {

  private val sideEffects = Stack<() -> Unit>()

  fun registerSideEffect(sideEffect: () -> Unit) {
    sideEffects.push(sideEffect)
  }

  @Scheduled(fixedRate = 1_000)
  fun runSideEffects() {
    runBlocking(Dispatchers.IO) {
      while (!sideEffects.empty()){
        // This needs to be outside of launch because otherwise it will launch more coroutines
        // than there are side effects on the stack causing an EmptyStackException which leads
        // to all coroutines being cancelled.
        val sideEffect = sideEffects.pop()
        launch {
          runSideEffect(sideEffect)
        }
      }
    }
  }

  private suspend fun runSideEffect(sideEffect: suspend () -> Unit) {
    try {
      sideEffect.invoke()
    } catch (e: Exception) {
      e.printStackTrace() // this should be a log statement off course
    }
  }
}
