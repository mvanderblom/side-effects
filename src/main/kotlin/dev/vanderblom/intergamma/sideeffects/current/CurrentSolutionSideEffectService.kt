package dev.vanderblom.intergamma.sideeffects.queue

import dev.vanderblom.intergamma.sideeffects.transaction
import org.springframework.stereotype.Component
import java.util.UUID

interface SideEffectEvent1<T> {
    val id: T
}

data class DeclarationSideEffectEvent1(override val id: String, val type: Type) : SideEffectEvent1<String> {
    enum class Type {
        CREATED,
        SOME_IMPORTANT_EVENT
    }
}

@Component
class SideEffectService1(
    private val listener: SideEffectEventListener1
){
    // FIXME This should only be called after the entire "business transaction" has finished, but it's up to the user to do so
    fun publishSideEffects(declaration: Declaration1, someImportantResult: SomeImportantResult) {
        // FIXME Breaks open-closed principle
        // FIXME Not resilient because if one fails, all will fail and will never be retried
        listener.receive(DeclarationSideEffectEvent1(declaration.id, DeclarationSideEffectEvent1.Type.CREATED)) // FIXME this should be be publishing to an actual AWS queue
        listener.receive(DeclarationSideEffectEvent1(declaration.id, DeclarationSideEffectEvent1.Type.SOME_IMPORTANT_EVENT)) // FIXME this should be be publishing to an actual AWS queue
    }

}

@Component
class SideEffectEventListener1(
    private val declarationSideEffectEventHandler: DeclarationSideEffectEventHandler1
) {
    // FIXME this would the be called upon receiving each event asynchronously
    fun receive(event: SideEffectEvent1<out Any>) { // FIXME Should probably be a factory
        when {
            event is DeclarationSideEffectEvent1 -> {
                declarationSideEffectEventHandler.handle(event)
            }
            else -> {
                println("Event type not supported")
            }
        }
    }
}

@Component
class DeclarationSideEffectEventHandler1 {
    fun handle(event: DeclarationSideEffectEvent1){
        when(event.type) { // FIXME Should probably be a factory
            DeclarationSideEffectEvent1.Type.CREATED -> println("handling event $event")
            DeclarationSideEffectEvent1.Type.SOME_IMPORTANT_EVENT -> println("handling event $event")
        }
    }
}

// Usage


@Component
class BusinessService1 (
    private val sideEffectService: SideEffectService1,
    private val someOtherBusinessService: SomeOtherBusinessService1,
    private val declarationRepo: DeclarationRepo1
) {
    fun createDeclaration() {
        val pair = transaction {
            val declaration = declarationRepo.createDeclaration()
            val someImportantResult = someOtherBusinessService.doSomethingImportantWith(declaration)
            Pair(declaration, someImportantResult)
        }
        sideEffectService.publishSideEffects(pair.first, pair.second) // FIXME this would get out of hand quick imho
    }
}

data class SomeImportantResult(val id: String)

@Component
class SomeOtherBusinessService1 {
    fun doSomethingImportantWith(declaration: Declaration1): SomeImportantResult {
        println("doing something important with $declaration")
        return SomeImportantResult("1337")
    }

}

data class Declaration1(val id: String)

@Component
class DeclarationRepo1 {
    fun createDeclaration(): Declaration1 {
        return transaction {
            Declaration1(UUID.randomUUID().toString())
        }
    }
}
