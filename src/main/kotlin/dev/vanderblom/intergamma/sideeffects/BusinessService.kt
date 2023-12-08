package dev.vanderblom.intergamma.sideeffects

import org.springframework.stereotype.Component

@Component
class BusinessService {
  fun mainWorkload() {
    println("Running main workload on t: ${Thread.currentThread().id}")
  }
}