package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.domains.sponsors.exceptions.InvalidSponsorDtoException
import de.vinz.openfls.logback.PerformanceLogbackFilter
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sponsors")
class SponsorController(val sponsorService: SponsorService) {

    private val logger: Logger = LoggerFactory.getLogger(SponsorController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping("")
    fun create(@Valid @RequestBody valueDto: SponsorDto): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(sponsorService.create(valueDto))
        } catch (ex: Exception) {
            getExceptionResponseEntity(ex)
        } finally {
            logPerformance("create", startMs)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: SponsorDto): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (id != valueDto.id)
            throw InvalidSponsorDtoException("path id and dto id are not the same")
        if (!sponsorService.existsById(id))
            throw InvalidSponsorDtoException("sponsor not found")

        return try {
            ResponseEntity.ok(sponsorService.update(valueDto))
        } catch (ex: Exception) {
            getExceptionResponseEntity(ex)
        } finally {
            logPerformance("update", startMs)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (!sponsorService.existsById(id))
            throw InvalidSponsorDtoException("sponsor not found")

        return try {
            val dto = sponsorService.getDtoById(id)
            sponsorService.delete(id)

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            getExceptionResponseEntity(ex)
        } finally {
            logPerformance("delete", startMs)
        }
    }

    @GetMapping
    fun getAll(): Any {
        // performance
        val startMs = System.currentTimeMillis()
        return try {
            ResponseEntity.ok(sponsorService.getAll())
        } catch (ex: Exception) {
            getExceptionResponseEntity(ex)
        } finally {
            logPerformance("getAll", startMs)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(sponsorService.getDtoById(id))
        } catch (ex: Exception) {
            getExceptionResponseEntity(ex)
        } finally {
            logPerformance("getById", startMs)
        }
    }

    private fun getExceptionResponseEntity(ex: Exception): ResponseEntity<String> {
        logger.error(ex.message, ex)

        return ResponseEntity(
                "Es trat ein unbekannter Fehler auf. Bitte wenden sie sich an ihren Administrator",
                HttpStatus.BAD_REQUEST
        )
    }

    private fun logPerformance(method: String, startMs: Long) {
        if (logPerformance) {
            val elapsedMs = System.currentTimeMillis() - startMs
            logger.info("${PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING} $method took $elapsedMs ms")
        }
    }
}