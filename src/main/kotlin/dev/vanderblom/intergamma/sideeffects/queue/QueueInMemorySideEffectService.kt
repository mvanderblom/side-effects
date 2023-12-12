package dev.vanderblom.intergamma.sideeffects.queue

import dev.vanderblom.intergamma.sideeffects.transaction
import org.springframework.stereotype.Component
import java.util.Stack
import java.util.UUID

interface QueueSideEffectEvent2<T> {
    val id: T
}

data class DeclarationSideEffectEvent2(override val id: String, val type: Type) : QueueSideEffectEvent2<String> {
    enum class Type {
        CREATED,
        SOME_IMPORTANT_EVENT
    }
}

@Component
class SideEffectService2 (
    private val listener: QueueSideEffectEventListener2
){
    // FIXME Events are only stored in-memory, so no resiliency
    private val sideEffectEvents = mutableMapOf<String, Stack<QueueSideEffectEvent2<out Any>>>()

    private fun getStack(businessTransactionId: String) = sideEffectEvents
        .computeIfAbsent(businessTransactionId) { Stack() }

    fun registerSideEffect(businessTransactionId: String, event: QueueSideEffectEvent2<out Any>) {
        getStack(businessTransactionId).push(event)
    }

    // FIXME This should only be called after the entire "business transaction" has finished, but it's up to the user to do so
    fun publishSideEffects(businessTransactionId: String) {
        val stack = getStack(businessTransactionId)
        stack.reverse()
        while (stack.any()) {
            val event = stack.pop()
            listener.receive(event) // FIXME this should be be publishing to an actual AWS queue
        }
    }

    // FIXME This should be called when something in the business transaction fails.
    fun clearSideEffects(businessTransactionId: String) = sideEffectEvents.remove(businessTransactionId)
}

@Component
class QueueSideEffectEventListener2 (
    private val declarationSideEffectEventHandler: DeclarationSideEffectEventHandler2
) {
    // FIXME this would the be called upon receiving each event asynchronously
    fun receive(event: QueueSideEffectEvent2<out Any>) { // FIXME Should probably be a factory
        when {
            event is DeclarationSideEffectEvent2 -> {
                declarationSideEffectEventHandler.handle(event)
            }
            else -> {
                println("Event type not supported")
            }
        }
    }
}

@Component
class DeclarationSideEffectEventHandler2 {
    fun handle(event: DeclarationSideEffectEvent2){
        when(event.type) { // FIXME Should probably be a factory
            DeclarationSideEffectEvent2.Type.CREATED -> println("handling event $event")
            DeclarationSideEffectEvent2.Type.SOME_IMPORTANT_EVENT -> println("handling event $event")
        }
    }
}

// Usage


@Component
class BusinessService2 (
    private val sideEffectService: SideEffectService2,
    private val someOtherBusinessService: SomeOtherBusinessService2,
    private val declarationRepo: DeclarationRepo2
) {
    fun createDeclaration() {
        val businessTransactionId = UUID.randomUUID().toString()
        try {
            transaction {
                val declaration = declarationRepo.createDeclaration(businessTransactionId)
                someOtherBusinessService.doSomethingImportantWith(businessTransactionId, declaration)
            }
            sideEffectService.publishSideEffects(businessTransactionId)
        } catch (exception: Exception) {
            sideEffectService.clearSideEffects(businessTransactionId)

        // FIXME this is not really feasible because
        //  - You'd need some sort of business transaction id that you need to pass down to all services that want to create side effects
        //  - Manual error handing
        //  - Clutters business code
        }
    }
}

@Component
class SomeOtherBusinessService2 (
    private val sideEffectService: SideEffectService2,
) {
    fun doSomethingImportantWith(businessTransactionId: String, declaration: Declaration2) {
        println("doing something important with $declaration")
        sideEffectService.registerSideEffect(
            businessTransactionId,
            DeclarationSideEffectEvent2(
                declaration.id,
                DeclarationSideEffectEvent2.Type.SOME_IMPORTANT_EVENT
            )
        )
    }

}

data class Declaration2(val id: String)

@Component
class DeclarationRepo2 (
    private val sideEffectService: SideEffectService2,
) {
    fun createDeclaration(businessTransactionId: String): Declaration2 {
        val declaration = transaction {
            Declaration2(UUID.randomUUID().toString())
        }
        sideEffectService.registerSideEffect(
            businessTransactionId,
            DeclarationSideEffectEvent2(declaration.id, DeclarationSideEffectEvent2.Type.CREATED)
        )
        return declaration
    }
}
