package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.domains.permissions.AccessService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/assistance_plans")
class AssistancePlanController(
        private val assistancePlanService: AssistancePlanService,
        private val accessService: AccessService
) {
    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping("")
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: AssistancePlanDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isAffiliated(token, valueDto.institutionId))
                throw IllegalArgumentException("user is not allowed to create assistance plans for this client")

            val dto = assistancePlanService.create(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
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

    @PutMapping("{id}")
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long,
               @Valid @RequestBody valueDto: AssistancePlanDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.canModifyAssistancePlan(token, id))
                throw IllegalArgumentException("user is not allowed to update this assistance plan")

            val dto = assistancePlanService.update(id, valueDto)

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
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

    @DeleteMapping("{id}")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("user is not allowed to delete assistance plans for this client")
            if (!assistancePlanService.existsById(id))
                throw IllegalArgumentException("assistance plan not found")

            val dto = assistancePlanService.getAssistancePlanDtoById(id)
            assistancePlanService.delete(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
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

    @GetMapping
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = assistancePlanService.getAllAssistancePlanDtos()

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = assistancePlanService.getAssistancePlanDtoById(id)

            if (logPerformance) {
                logger.info(String.format("%s getById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client/{id}")
    fun getByClientId(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = assistancePlanService.getAssistancePlanDtosByClientId(id)

            if (logPerformance) {
                logger.info(String.format("%s getByClientId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("sponsor/{id}")
    fun getBySponsorId(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = assistancePlanService.getAssistancePlanDtosBySponsorId(id)

            if (logPerformance) {
                logger.info(String.format("%s getBySponsorId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("institution/{id}")
    fun getByInstitutionId(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = assistancePlanService.getAssistancePlanDtosByInstitutionId(id)

            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("eval/{id}")
    fun getEvalById(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = assistancePlanService.getEvaluationById(id)

            if (logPerformance) {
                logger.info(String.format("%s getEvalById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }
}