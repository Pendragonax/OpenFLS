package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.domains.sponsors.dto.SponsorCreateRequest
import de.vinz.openfls.domains.sponsors.dto.SponsorDeleteResult
import de.vinz.openfls.domains.sponsors.dto.SponsorUpdateRequest
import de.vinz.openfls.domains.sponsors.dto.SponsorUpdateResult
import de.vinz.openfls.domains.sponsors.service.SponsorService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sponsors")
class SponsorController(val sponsorService: SponsorService,
                        val performanceLoggingService: PerformanceLoggingService) {

    private val logger: Logger = LoggerFactory.getLogger(SponsorController::class.java)

    @PostMapping("")
    fun create(@Valid @RequestBody request: SponsorCreateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(sponsorService.create(request))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody request: SponsorUpdateRequest): Any {
        // performance
        val startMs = System.currentTimeMillis()

        if (id != request.id)
            return ResponseEntity.badRequest().body("path id and request id are not the same")

        return try {
            when (val result = sponsorService.update(request)) {
                is SponsorUpdateResult.Success -> ResponseEntity.ok(result.response)
                SponsorUpdateResult.NotFound -> sponsorNotFound()
            }
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

        return try {
            when (val result = sponsorService.delete(id)) {
                is SponsorDeleteResult.Success -> ResponseEntity.ok(result.response)
                SponsorDeleteResult.NotFound -> sponsorNotFound()
            }
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
            val sponsor = sponsorService.getById(id) ?: return sponsorNotFound()
            ResponseEntity.ok(sponsor)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    private fun sponsorNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("sponsor not found")
}
