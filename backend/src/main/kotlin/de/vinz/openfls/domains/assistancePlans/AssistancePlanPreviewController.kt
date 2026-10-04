package de.vinz.openfls.domains.assistancePlans

import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanPreviewListResult
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanPreviewService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/assistance_plans")
class AssistancePlanPreviewController(
    private val assistancePlanPreviewService: AssistancePlanPreviewService,
    private val performanceLoggingService: PerformanceLoggingService
) {
    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanPreviewController::class.java)

    @GetMapping("client/{id}/preview")
    fun getPreviewsByClientId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val previews = assistancePlanPreviewService.getPreviewsOfCurrentUserByClientId(id) ?: return clientNotFound()
            ResponseEntity.ok(previews)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getPreviewsByClientId", startMs, logger)
        }
    }

    @GetMapping("client/{id}/existing")
    fun getExistingByClientId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val existing = assistancePlanPreviewService.getExistingByClientId(id) ?: return clientNotFound()
            ResponseEntity.ok(existing)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getExistingByClientId", startMs, logger)
        }
    }

    @GetMapping("institution/{id}/preview")
    fun getPreviewsByInstitutionId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = assistancePlanPreviewService.getPreviewsByInstitutionId(id)) {
                is AssistancePlanPreviewListResult.Success -> ResponseEntity.ok(result.previews)
                AssistancePlanPreviewListResult.Forbidden ->
                    ResponseEntity.status(HttpStatus.FORBIDDEN).body("no permission to load the assistance plans of this institution")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getPreviewsByInstitutionId", startMs, logger)
        }
    }

    @GetMapping("sponsor/{id}/preview")
    fun getPreviewsBySponsorId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(assistancePlanPreviewService.getPreviewsBySponsorId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getPreviewsBySponsorId", startMs, logger)
        }
    }

    @GetMapping("favorites/preview")
    fun getFavoritePreviews(): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(assistancePlanPreviewService.getFavoritePreviews())
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getFavoritePreviews", startMs, logger)
        }
    }

    private fun clientNotFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("client not found")
}
