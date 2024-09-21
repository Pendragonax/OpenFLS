package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanHourService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/assistance_plan_hours")
class AssistancePlanHourController(
        private val assistancePlanHourService: AssistancePlanHourService
) {
    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false


    @PostMapping
    fun create(@Valid @RequestBody valueDto: AssistancePlanHourDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = assistancePlanHourService.save(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s save took %s ms",
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


    @PutMapping
    fun update(@Valid @RequestBody valueDto: AssistancePlanHourDto): Any {
        return create(valueDto)
    }


    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = assistancePlanHourService.getById(id)
            assistancePlanHourService.delete(id)

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
}