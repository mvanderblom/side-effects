package dev.vanderblom.intergamma.sideeffects

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Stack
import kotlin.system.measureTimeMillis

@Component
class SchedulingSideEffectService {

  private val sideEffects = Stack<() -> Unit>()

  var successfulSideEffects = 0
  var brokenSideEffects = 0
  var latestExecTime = 0L

  fun registerSideEffect(block: () -> Unit) {
    sideEffects.push(block)
  }

  @Scheduled(fixedRate = 1_000)
  fun runSideEffects() {
    val execTime = measureTimeMillis {
      runBlocking(Dispatchers.IO) {
        while (!sideEffects.empty()){
          val sideEffect = sideEffects.pop()
          launch {
            runSideEffect(sideEffect)
          }
        }
      }
    }
    if (execTime > 10) { // Only for testing purposes
      latestExecTime = execTime
    }
  }

  private suspend fun runSideEffect(sideEffect: suspend () -> Unit) {
    try {
      println("Invoking side-effect from t: ${Thread.currentThread().id}")
      sideEffect.invoke()
      successfulSideEffects++
      println("Done invoking side-effect from t: ${Thread.currentThread().id}")
    } catch (e: Exception) {
      brokenSideEffects++
      e.printStackTrace() // this should be a log statement off course
      // rethrowing e would stop all side effects from being executed.
    }
  }
}

