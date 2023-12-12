package dev.vanderblom.intergamma.sideeffects.queue

import dev.vanderblom.intergamma.sideeffects.transaction
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SideEffectService1 {
    // FIXME This should only be called after the entire "business transaction" has finished, but it's up to the user to do so
    fun publishSideEffects(declaration: Declaration1, someImportantResult: SomeImportantResult1) {
        // FIXME Not resilient because if one fails, all will fail and will never be retried
        runBlocking {
            launch {
                // put a message on a queue
            }
            launch {
                // do an http call
            }
            launch {
                // Store something in dynamo
            }
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
            Pair(declaration, someImportantResult) // FIXME This is only needed because we need to pass along all data that we want in the event to outside of the main transaction
        }
        sideEffectService.publishSideEffects(pair.first, pair.second) // FIXME this would get out of hand quick imho. third, fourth, etc... --> Context object could solve this, but that would also bundle all kinds of unrelated stuff.
    }
}

data class SomeImportantResult1(val id: String)

@Component
class SomeOtherBusinessService1 {
    fun doSomethingImportantWith(declaration: Declaration1): SomeImportantResult1 {
        return transaction {
            println("doing something important with $declaration")
            SomeImportantResult1("1337")
        }
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
