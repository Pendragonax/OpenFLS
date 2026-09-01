package de.vinz.openfls.domains.clients.dashboard

import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
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
    private val employeeService: EmployeeService,
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
            val institutionId = clientService.getDtoById(
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
            if (!clientService.existsById(clientId))
                throw IllegalArgumentException("client not found")

            employeeService.addClientAsFavorite(clientId, accessService.getId())
            ResponseEntity.ok().build<Void>()
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
            employeeService.deleteClientAsFavorite(clientId, accessService.getId())
            ResponseEntity.ok().build<Void>()
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
}
