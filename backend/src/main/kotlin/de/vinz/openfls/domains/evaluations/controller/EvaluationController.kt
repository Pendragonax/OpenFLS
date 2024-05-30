package de.vinz.openfls.domains.evaluations.controller

import de.vinz.openfls.domains.evaluations.dtos.EvaluationRequestDto
import de.vinz.openfls.services.AccessService
import de.vinz.openfls.services.EmployeeService
import de.vinz.openfls.domains.evaluations.services.EvaluationService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import jakarta.validation.Valid

@Controller
@RequestMapping("/evaluations")
class EvaluationController(
        private val evaluationService: EvaluationService,
        private val employeeService: EmployeeService,
        private val accessService: AccessService
) {

    private val logger: Logger = LoggerFactory.getLogger(EvaluationController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: EvaluationRequestDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            // find user
            val user = employeeService.getById(accessService.getId(token), true)
                    ?: throw IllegalArgumentException("User not found")
            val dto = evaluationService.create(valueDto, user)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: EvaluationRequestDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            // find user
            val user = employeeService.getById(accessService.getId(token), true)
                    ?: throw IllegalArgumentException("User not found")
            val dto = evaluationService.update(valueDto, user)

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()
            val dto = evaluationService.delete(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()
            val dto = evaluationService.getAll()

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}")
    fun getById(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                              @PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()
            val dto = evaluationService.getById(id)

            if (logPerformance) {
                logger.info(String.format("%s getById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{assistancePlanId}")
    fun getByAssistancePlanId(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                              @PathVariable assistancePlanId: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()
            val dto = evaluationService.getByAssistancePlanId(assistancePlanId)

            if (logPerformance) {
                logger.info(String.format("%s getByAssistancePlanId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{assistancePlanId}/{year}")
    fun getByAssistancePlanIdAndYear(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                     @PathVariable assistancePlanId: Long,
                                     @PathVariable year: Int): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()
            val dto = evaluationService.getByAssistancePlanIdAndYear(assistancePlanId, year)

            if (logPerformance) {
                logger.info(String.format("%s getByAssistancePlanIdAndYear took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }
}