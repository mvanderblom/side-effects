package dev.vanderblom.intergamma.sideeffects.events

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class SideEffectEventListener {
    var successfulSideEffects = 0
    var brokenSideEffects = 0

    @EventListener
    fun handleEvent(event: SideEffectEvent) {
        println("received SideEffectEvent t: ${Thread.currentThread().id}")
        successfulSideEffects++
    }

    @EventListener
    fun handleEvent(event: BadSideEffectEvent) {
        println("received BadSideEffectEvent t: ${Thread.currentThread().id}")
        brokenSideEffects++
        throw IllegalStateException("I'm so baaaad!")
    }

    @EventListener
    fun handleEvent(event: ExpensiveSideEffectEvent) {
        println("received ExpensiveSideEffectEvent t: ${Thread.currentThread().id}")
        Thread.sleep(500)
        successfulSideEffects++
    }
}

class SideEffectEvent
class BadSideEffectEvent
class ExpensiveSideEffectEvent