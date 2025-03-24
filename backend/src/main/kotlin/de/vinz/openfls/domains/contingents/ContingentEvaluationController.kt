package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.services.ContingentEvaluationService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contingents/evaluations")
class ContingentEvaluationController(
    private val contingentEvaluationService: ContingentEvaluationService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ContingentEvaluationController::class.java)

    @GetMapping("institution/{institutionId}/{year}")
    fun getByInstitution(@PathVariable institutionId: Long, @PathVariable year: Int): Any {
        // performance
        val startMs = System.currentTimeMillis()

        return try {
            val contingentEvaluation =
                contingentEvaluationService.getContingentEvaluationByYearAndInstitution(year, institutionId)
            return ResponseEntity.ok(contingentEvaluation)
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getByInstitutionId", startMs, logger)
        }
    }
}