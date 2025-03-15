package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.domains.sponsors.exceptions.InvalidSponsorDtoException
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sponsors")
class SponsorController(val sponsorService: SponsorService,
                        val performanceLoggingService: PerformanceLoggingService) {

    private val logger: Logger = LoggerFactory.getLogger(SponsorController::class.java)

    @PostMapping("")
    fun create(@Valid @RequestBody valueDto: SponsorDto): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(sponsorService.create(valueDto))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
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
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("update", startMs, logger)
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
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping
    fun getAll(): Any {
        // performance
        val startMs = System.currentTimeMillis()
        return try {
            ResponseEntity.ok(sponsorService.getAll())
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(sponsorService.getDtoById(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }
}