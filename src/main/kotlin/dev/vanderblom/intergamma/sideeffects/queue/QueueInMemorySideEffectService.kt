package dev.vanderblom.intergamma.sideeffects.queue

import dev.vanderblom.intergamma.sideeffects.transaction
import org.springframework.stereotype.Component
import java.util.UUID

data class DeclarationSideEffectEvent2(val id: String, val type: Type) {
    enum class Type {
        CREATED,
        SOME_IMPORTANT_EVENT
    }
}

@Component
class SideEffectService2 (
    private val listener: QueueSideEffectEventListener2
){
    // FIXME This should only be called after the entire "business transaction" has finished, but it's up to the user to do so
    fun publishSideEffects(declaration: Declaration2, someImportantResult: SomeImportantResult2) {
        listener.receive(
            DeclarationSideEffectEvent2(
                declaration.id,
                DeclarationSideEffectEvent2.Type.CREATED
            )
        ) // FIXME this should be be publishing to an actual AWS queue
        listener.receive(
            DeclarationSideEffectEvent2(
                declaration.id,
                DeclarationSideEffectEvent2.Type.SOME_IMPORTANT_EVENT
            )
        ) // FIXME this should be be publishing to an actual AWS queue
    }

}

@Component
class QueueSideEffectEventListener2 {
    // FIXME this would the be called upon receiving each event asynchronously
    fun receive(event: DeclarationSideEffectEvent2) {
        println("handling event $event")
        // TODO get entity associated with the event and do what needs to be done
        //  - put a message on a queue
        //  - do an http call
        //  - Store something in dynamo
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
        val pair = transaction {
            val declaration = declarationRepo.createDeclaration()
            val someImportantResult = someOtherBusinessService.doSomethingImportantWith(declaration)
            Pair(declaration, someImportantResult) // FIXME This is only needed because we need to pass along all data that we want in the event to outside of the main transaction
        }
        sideEffectService.publishSideEffects(pair.first, pair.second) // FIXME this would get out of hand quick imho. third, fourth, etc... --> Context object could solve this, but that would also bundle all kinds of unrelated stuff.
    }
}

data class SomeImportantResult2(val id: String)

@Component
class SomeOtherBusinessService2 {
    fun doSomethingImportantWith(declaration: Declaration2): SomeImportantResult2 {
        return transaction {
            println("doing something important with $declaration")
            SomeImportantResult2("1337")
        }
    }

}

data class Declaration2(val id: String)

@Component
class DeclarationRepo2 {
    fun createDeclaration(): Declaration2 {
        return transaction {
            Declaration2(UUID.randomUUID().toString())
        }
    }
}
