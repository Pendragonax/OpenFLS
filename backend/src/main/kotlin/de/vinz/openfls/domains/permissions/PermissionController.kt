package de.vinz.openfls.domains.permissions
import de.vinz.openfls.logging.StructuredLog

import de.vinz.openfls.domains.sponsors.SponsorController
import de.vinz.openfls.logback.PerformanceLogbackFilter
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/permissions")
class PermissionController(
        private val permissionService: PermissionService)
{

    private val logger: Logger = LoggerFactory.getLogger(SponsorController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody valueDto: PermissionDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = permissionService.savePermission(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody valueDto: PermissionDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = permissionService.savePermission(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            permissionService.deleteById(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok()
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = permissionService.getAll()

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/combination/{employeeId}/{institutionId}")
    fun getById(@PathVariable employeeId: Long,
                @PathVariable institutionId: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = permissionService.getByEmployeeIdAndInstitutionId(employeeId, institutionId)

            if (logPerformance) {
                logger.info(String.format("%s getById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/employee/{employeeId}")
    fun getByEmployeeId(@PathVariable employeeId: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = permissionService.getByEmployeeId(employeeId)

            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/institution/{institutionId}")
    fun getByInstitutionId(@PathVariable institutionId: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = permissionService.getByInstitutionId(institutionId)

            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            StructuredLog.error(logger, "application.request.failed", ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }
}
