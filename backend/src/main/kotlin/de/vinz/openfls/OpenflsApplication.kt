package de.vinz.openfls

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableScheduling
class OpenflsApplication

fun main(args: Array<String>) {
	runApplication<OpenflsApplication>(*args)
}
