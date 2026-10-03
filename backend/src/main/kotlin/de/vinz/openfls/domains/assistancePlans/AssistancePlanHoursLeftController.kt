package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanHoursLeftService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/assistance_plans")
class AssistancePlanHoursLeftController(
    private val assistancePlanHoursLeftService: AssistancePlanHoursLeftService,
    private val performanceLoggingService: PerformanceLoggingService
) {
    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanHoursLeftController::class.java)

    @GetMapping("{id}/hours_left")
    fun getHoursLeftById(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val hoursLeft = assistancePlanHoursLeftService.getHoursLeftByAssistancePlanId(LocalDate.now(), id)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("assistance plan not found")
            ResponseEntity.ok(hoursLeft)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getHoursLeftById", startMs, logger)
        }
    }
}
