package dev.vanderblom.intergamma.sideeffects.statusfield

import dev.vanderblom.intergamma.sideeffects.transaction
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.TimeUnit

interface StatusFieldSideEffectEvent3<T> {
    val id: T
}

data class DeclarationSideEffectEvent3(override val id: String, val type: Type) : StatusFieldSideEffectEvent3<String> {
    enum class Type {
        CREATED,
        SOME_IMPORTANT_EVENT
    }
}

@Component
class SideEffectService(
    private val listener: StatusFieldSideEffectEventListener3,
    private val declarationRepo: DeclarationRepo
){
    // FIXME Scheduled, so multiple instances problem. Probably needs some form of locking
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    fun publishSideEffects(businessTransactionId: String) {
        val declarations = declarationRepo.getDeclarations()
        declarations.forEach { declaration ->
            if (!declaration.createdEventSent) { // FIXME Breaks open-closed and SRP principles
                val event = DeclarationSideEffectEvent3(declaration.id, DeclarationSideEffectEvent3.Type.CREATED)
                transaction {
                    listener.receive(event) // FIXME this should be be publishing to an actual AWS queue
                    declarationRepo.update(declaration.copy(createdEventSent = true))
                }
            }
            if (!declaration.someImportantEventSent) {
                val event = DeclarationSideEffectEvent3(declaration.id, DeclarationSideEffectEvent3.Type.SOME_IMPORTANT_EVENT)
                transaction {
                    listener.receive(event) // FIXME this should be be publishing to an actual AWS queue
                    declarationRepo.update(declaration.copy(someImportantEventSent = true))
                }
            }
        }
    }
}

@Component
class StatusFieldSideEffectEventListener3(
    private val declarationSideEffectEventHandler: DeclarationSideEffectEventHandler3
) {
    // FIXME this would the be called upon receiving each event asynchronously
    fun receive(event: StatusFieldSideEffectEvent3<out Any>) {
        when {
            event is DeclarationSideEffectEvent3 -> { // FIXME Should probably be a factory
                declarationSideEffectEventHandler.handle(event)
            }
            else -> {
                println("Event type not supported")
            }
        }
    }
}

@Component
class DeclarationSideEffectEventHandler3 {
    fun handle(event: DeclarationSideEffectEvent3){
        when(event.type) { // FIXME Should probably be a factory
            DeclarationSideEffectEvent3.Type.CREATED -> println("handling event $event")
            DeclarationSideEffectEvent3.Type.SOME_IMPORTANT_EVENT -> println("handling event $event")
        }
    }
}


// Usage


@Component
class BusinessService3 (
    private val someOtherBusinessService: SomeOtherBusinessService3,
    private val declarationRepo: DeclarationRepo3
) {
    fun createDeclaration() {
        transaction {
            val declaration = declarationRepo.createDeclaration()
            someOtherBusinessService.doSomethingImportantWith(declaration)
        }
        // FIXME This approach actually cleans up the business code, so that's nice
        // FIXME No need to pass any args, nor return any data. Everything is stored in the database so it is resillient
    }
}

@Component
class SomeOtherBusinessService3 {
    fun doSomethingImportantWith(declaration: Declaration3) {
        println("doing something important with $declaration")
    }

}

// FIXME clutters database/domain object with status fields...
data class Declaration3(val id: String, val createdEventSent: Boolean = false, val someImportantEventSent: Boolean = false)

@Component
class DeclarationRepo3 {
    fun createDeclaration(): Declaration3 {
        val declaration = transaction {
            Declaration3(UUID.randomUUID().toString())
        }
        return declaration
    }

    fun update(declaration: Declaration3) {

    }

    fun getDeclarations(): List<Declaration3> = emptyList()
}

