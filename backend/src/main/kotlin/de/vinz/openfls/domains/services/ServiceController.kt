package de.vinz.openfls.domains.services

import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.services.dto.ClientServicesByDateRequest
import de.vinz.openfls.domains.services.dto.ServiceCreateRequest
import de.vinz.openfls.domains.services.dto.ServiceCreateResult
import de.vinz.openfls.domains.services.dto.ServiceDeleteResult
import de.vinz.openfls.domains.services.dto.ServiceGetResult
import de.vinz.openfls.domains.services.dto.ServiceUpdateRequest
import de.vinz.openfls.domains.services.dto.ServiceUpdateResult
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.common.web.ExceptionResponseService
import de.vinz.openfls.common.web.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/services")
class ServiceController(
    private val serviceService: ServiceService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ServiceController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: ServiceCreateRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = serviceService.create(request)) {
                is ServiceCreateResult.Success -> ResponseEntity.ok(result.response)
                ServiceCreateResult.Forbidden -> forbidden("no permission to write entries to this institution")
                ServiceCreateResult.ClientNotFound -> badRequest("client not found")
                ServiceCreateResult.AssistancePlanNotFound -> badRequest("assistance plan not found")
                ServiceCreateResult.HourTypeNotFound -> badRequest("hour type not found")
                ServiceCreateResult.InstitutionNotFound -> badRequest("institution not found")
                ServiceCreateResult.GoalNotFound -> badRequest("goal not found")
                ServiceCreateResult.CategoryNotFound -> badRequest("category not found")
                ServiceCreateResult.ClientArchived -> clientArchived()
                ServiceCreateResult.InvalidTimeRange -> invalidTimeRange()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: ServiceUpdateRequest): Any {
        val startMs = System.currentTimeMillis()

        if (id != request.id)
            return badRequest("path id and request id are not the same")

        return try {
            when (val result = serviceService.update(id, request)) {
                is ServiceUpdateResult.Success -> ResponseEntity.ok(result.response)
                ServiceUpdateResult.NotFound -> notFound("service not found")
                ServiceUpdateResult.Forbidden -> forbidden("no permission to update this service")
                ServiceUpdateResult.ClientNotFound -> badRequest("client not found")
                ServiceUpdateResult.AssistancePlanNotFound -> badRequest("assistance plan not found")
                ServiceUpdateResult.HourTypeNotFound -> badRequest("hour type not found")
                ServiceUpdateResult.InstitutionNotFound -> badRequest("institution not found")
                ServiceUpdateResult.GoalNotFound -> badRequest("goal not found")
                ServiceUpdateResult.CategoryNotFound -> badRequest("category not found")
                ServiceUpdateResult.ClientArchived -> clientArchived()
                ServiceUpdateResult.InvalidTimeRange -> invalidTimeRange()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("update", startMs, logger)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = serviceService.delete(id)) {
                is ServiceDeleteResult.Success -> ResponseEntity.ok(result.response)
                ServiceDeleteResult.NotFound -> notFound("service not found")
                ServiceDeleteResult.Forbidden -> forbidden("no permission to delete this service")
                ServiceDeleteResult.ClientArchived -> clientArchived()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = serviceService.getById(id)) {
                is ServiceGetResult.Success -> ResponseEntity.ok(result.response)
                ServiceGetResult.NotFound -> notFound("service not found")
                ServiceGetResult.Forbidden -> forbidden("no permission to read this service")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    @GetMapping("assistance_plan/{id}")
    fun getServicesByAssistancePlanId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.canModifyAssistancePlan(id))
            return forbidden("no permission to load the services of this assistance plan")

        return try {
            val services = serviceService.getServicesByAssistancePlanId(id)
                ?: return notFound("assistance plan not found")
            ResponseEntity.ok(services)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getServicesByAssistancePlanId", startMs, logger)
        }
    }

    @GetMapping("employee/{id}/outside_assistance_plan_period")
    fun getServicesOutsideAssistancePlanPeriodByEmployeeId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin() && !accessService.canReadEmployee(id) && accessService.getId() != id)
            return forbidden("no permission to load the services outside the assistance plan period of this employee")

        return try {
            ResponseEntity.ok(serviceService.getServicesOutsideAssistancePlanPeriodByEmployeeId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getServicesOutsideAssistancePlanPeriodByEmployeeId", startMs, logger)
        }
    }

    @GetMapping("employee/{id}/{start}/{end}")
    fun getServicesByEmployeeIdAndStartAndEnd(
        @PathVariable id: Long,
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate
    ): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(serviceService.getServicesByEmployeeIdAndStartAndEnd(id, start, end))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getServicesByEmployeeIdAndStartAndEnd", startMs, logger)
        }
    }

    @GetMapping("institution/{id}/outside_assistance_plan_period")
    fun getServicesOutsideAssistancePlanPeriodByInstitutionId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.canReadEntries(id))
            return forbidden("no permission to load the services of this institution")

        return try {
            ResponseEntity.ok(serviceService.getServicesOutsideAssistancePlanPeriodByInstitutionId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getServicesOutsideAssistancePlanPeriodByInstitutionId", startMs, logger)
        }
    }

    @GetMapping("institution/{institutionId}/employee/{employeeId}/client/{clientId}/{start}/{end}")
    fun getServicesByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd(
        @PathVariable institutionId: Long,
        @PathVariable employeeId: Long,
        @PathVariable clientId: Long,
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate
    ): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.canReadEntries(institutionId))
            return forbidden("no permission to load the services of this institution")
        if (employeeId > 0 &&
            accessService.getId() != employeeId &&
            !accessService.isAdmin() &&
            !accessService.canReadEmployee(employeeId))
            return forbidden("no permission to load the services of this employee")

        return try {
            ResponseEntity.ok(
                serviceService.getServicesByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd(
                    institutionId, employeeId, clientId, start, end
                )
            )
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance(
                "getServicesByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd", startMs, logger
            )
        }
    }

    @GetMapping("count/employee/{id}")
    fun countServicesByEmployeeId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(serviceService.countServicesByEmployeeId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("countServicesByEmployeeId", startMs, logger)
        }
    }

    @GetMapping("count/client/{id}")
    fun countServicesByClientId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(serviceService.countServicesByClientId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("countServicesByClientId", startMs, logger)
        }
    }

    @GetMapping("count/assistance_plan/{id}")
    fun countServicesByAssistancePlanId(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(serviceService.countServicesByAssistancePlanId(id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("countServicesByAssistancePlanId", startMs, logger)
        }
    }

    @PostMapping("client-and-date")
    fun getClientServicesByDate(@RequestBody request: ClientServicesByDateRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(serviceService.getClientServicesByDate(request.clientId, request.date))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getClientServicesByDate", startMs, logger)
        }
    }

    private fun notFound(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(message)

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)

    private fun badRequest(message: String): ResponseEntity<String> =
        ResponseEntity.badRequest().body(message)

    private fun clientArchived(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body("client is archived")

    private fun invalidTimeRange(): ResponseEntity<String> =
        badRequest("start must be before end")
}
