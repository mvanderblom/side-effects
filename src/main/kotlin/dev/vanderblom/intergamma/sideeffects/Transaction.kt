package dev.vanderblom.intergamma.sideeffects

fun <T> transaction(block: () -> T): T {
    // mockup for transaction
    return block.invoke()
}