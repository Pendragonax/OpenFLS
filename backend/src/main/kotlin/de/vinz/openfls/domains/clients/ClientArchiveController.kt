package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.archive.ClientArchiveActionRequest
import de.vinz.openfls.domains.clients.archive.ClientArchiveActor
import de.vinz.openfls.domains.clients.archive.ClientArchiveService
import de.vinz.openfls.domains.clients.archive.ClientArchiveStateException
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.exceptions.UserNotAllowedException
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/clients")
class ClientArchiveController(
    private val clientArchiveService: ClientArchiveService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService
) {

    private val logger: Logger = LoggerFactory.getLogger(ClientArchiveController::class.java)

    @PostMapping("{id}/archive")
    fun archive(
        @PathVariable id: Long,
        @Valid @RequestBody request: ClientArchiveActionRequest
    ): Any {
        return try {
            ResponseEntity.ok(
                clientArchiveService.archive(
                    clientId = id,
                    actionDate = request.actionDate!!,
                    reason = request.reason,
                    remark = request.remark,
                    actor = loadActor()
                )
            )
        } catch (ex: UserNotAllowedException) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
        } catch (ex: ClientArchiveStateException) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.CONFLICT)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("{id}/reactivate")
    fun reactivate(
        @PathVariable id: Long,
        @Valid @RequestBody request: ClientArchiveActionRequest
    ): Any {
        return try {
            ResponseEntity.ok(
                clientArchiveService.reactivate(
                    clientId = id,
                    actionDate = request.actionDate!!,
                    reason = request.reason,
                    remark = request.remark,
                    actor = loadActor()
                )
            )
        } catch (ex: UserNotAllowedException) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
        } catch (ex: ClientArchiveStateException) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.CONFLICT)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
        }
    }

    private fun loadActor(): ClientArchiveActor {
        val employeeId = accessService.getId()
        val employee = employeeService.getEmployeeDtoById(employeeId, accessService.isAdmin())
            ?: throw IllegalArgumentException("employee not found")

        return ClientArchiveActor(
            employeeId = employee.id,
            firstname = employee.firstName,
            lastname = employee.lastName,
            isAdmin = accessService.isAdmin(),
            leadingInstitutionIds = accessService.getLeadingInstitutionIds()
        )
    }
}
