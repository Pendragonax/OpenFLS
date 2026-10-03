package de.vinz.openfls.domains.clients.dashboard

import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.employees.dto.EmployeeFavoriteResult
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Read endpoints of the client dashboard and the favourite clients that open the
 * home view. Whether a section may be read is decided here and reported back to the
 * frontend, so a missing permission is shown as such instead of as empty data.
 */
@RestController
@RequestMapping("/client_dashboards")
class ClientDashboardController(
    private val clientDashboardService: ClientDashboardService,
    private val clientService: ClientService,
    private val employeeFavoriteService: EmployeeFavoriteService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClientDashboardController::class.java)

    @GetMapping("client/{clientId}")
    fun getDashboard(@PathVariable clientId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val employeeId = accessService.getId()
            val isAdmin = accessService.isAdmin()
            val institutionId = clientService.getById(
                id = clientId,
                includeArchived = true,
                leadingInstitutionIds = emptyList()
            )?.institution?.id ?: throw IllegalArgumentException("client not found")

            val isLeader = accessService.isLeader(institutionId)
            val isAffiliated = accessService.isAffiliated(institutionId)
            val canReadDocumentation = isAdmin || isLeader || isAffiliated ||
                    accessService.getReadRightsInstitutionIds().contains(institutionId)

            val dashboard = clientDashboardService.getDashboard(
                clientId = clientId,
                employeeId = employeeId,
                isAdmin = isAdmin,
                includeArchived = isAdmin || isLeader,
                canReadDocumentation = canReadDocumentation,
                canWriteEntries = accessService.canWriteEntries(institutionId),
                canModifyClient = accessService.canModifyClient(clientId),
                readableInstitutionIds = accessService.getReadRightsInstitutionIds()
            ) ?: return ResponseEntity("Klient:in nicht gefunden", HttpStatus.NOT_FOUND)

            ResponseEntity.ok(dashboard)
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getDashboard", startMs, logger)
        }
    }

    @GetMapping("favorites")
    fun getFavorites(): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(
                clientDashboardService.getFavorites(
                    employeeId = accessService.getId(),
                    isAdmin = accessService.isAdmin(),
                    leadingInstitutionIds = accessService.getLeadingInstitutionIds()
                )
            )
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getFavorites", startMs, logger)
        }
    }

    @PostMapping("favorites/client/{clientId}")
    fun addFavorite(@PathVariable clientId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            favoriteResponse(employeeFavoriteService.addClientFavorite(accessService.getId(), clientId))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("addFavorite", startMs, logger)
        }
    }

    @DeleteMapping("favorites/client/{clientId}")
    fun deleteFavorite(@PathVariable clientId: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            favoriteResponse(employeeFavoriteService.deleteClientFavorite(accessService.getId(), clientId))
        } catch (ex: IllegalAccessException) {
            ExceptionResponseService.getPermissionDeniedResponseEntity(ex, logger)
        } catch (ex: IllegalArgumentException) {
            ExceptionResponseService.getIllegalArgumentExceptionResponseEntity(ex, logger)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("deleteFavorite", startMs, logger)
        }
    }

    private fun favoriteResponse(result: EmployeeFavoriteResult): ResponseEntity<out Any> {
        return when (result) {
            EmployeeFavoriteResult.Success -> ResponseEntity.ok().build<Void>()
            EmployeeFavoriteResult.EmployeeNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("employee not found")
            EmployeeFavoriteResult.ClientNotFound -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("client not found")
            EmployeeFavoriteResult.AssistancePlanNotFound -> ResponseEntity.badRequest().body("assistance plan not found")
        }
    }
}
