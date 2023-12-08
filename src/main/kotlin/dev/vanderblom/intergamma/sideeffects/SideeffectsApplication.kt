package dev.vanderblom.intergamma.sideeffects

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SideeffectsApplication

fun main(args: Array<String>) {
	runApplication<SideeffectsApplication>(*args)
}
