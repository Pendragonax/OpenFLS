package de.vinz.openfls

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenflsApplication

fun main(args: Array<String>) {
	runApplication<OpenflsApplication>(*args)
}