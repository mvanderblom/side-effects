package dev.vanderblom.intergamma.sideeffects.statusfield

import dev.vanderblom.intergamma.sideeffects.transaction
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.TimeUnit

@Component
class SideEffectService4(
    private val listener: StatusFieldSideEffectEventListener4,
    private val outboxEventRepo: OutboxEventRepo
){
    // FIXME Scheduled, so multiple instances problem. Probably needs some form of locking
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    fun publishSideEffects(businessTransactionId: String) {
        val events = outboxEventRepo.getEvents()
        events.forEach { event ->
            transaction { // FIXME no more open-closed and SRP violations
                listener.receive(event) // FIXME this should be be publishing to an actual AWS queue
                outboxEventRepo.delete(event)
            }
        }
    }
}

@Component
class StatusFieldSideEffectEventListener4 {
    // FIXME this would the be called upon receiving each event asynchronously
    fun receive(event: OutboxEvent) {
        when(event.type) {
            OutboxEvent.Type.DECLARATION_CREATED -> println("handling event $event")
            OutboxEvent.Type.SOME_IMPORTANT_EVENT_OCCURED -> println("handling event $event")
        }
    }
}


// Usage


@Component
class BusinessService4 (
    private val someOtherBusinessService: SomeOtherBusinessService4,
    private val declarationRepo: DeclarationRepo4
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
class SomeOtherBusinessService4 (
    private val outboxEventRepo: OutboxEventRepo
) {
    fun doSomethingImportantWith(declaration: Declaration4) {
        transaction {
            println("doing something important with $declaration")
            // FIXME Events can be created everywhere. They will get stored if the transaction completes
            outboxEventRepo.createEvent(OutboxEvent("1337", OutboxEvent.Type.SOME_IMPORTANT_EVENT_OCCURED))
        }
    }

}

data class Declaration4(val id: String)

@Component
class DeclarationRepo4 (
    private val outboxEventRepo: OutboxEventRepo
) {
    fun createDeclaration(): Declaration4 {
        return transaction {
            Declaration4(UUID.randomUUID().toString())
                .also {
                    // FIXME Events can be added everywhere. They will get stored if the transaction completes
                    outboxEventRepo.createEvent(OutboxEvent(it.id, OutboxEvent.Type.DECLARATION_CREATED))
                }
        }

    }
}

data class OutboxEvent(val id: String, val type: Type) {
    enum class Type {
        DECLARATION_CREATED,
        SOME_IMPORTANT_EVENT_OCCURED
    }
}

@Component
class OutboxEventRepo {
    fun createEvent(event: OutboxEvent) {}
    fun getEvents(): List<OutboxEvent> = emptyList()
    fun delete(event: OutboxEvent) { }
}

