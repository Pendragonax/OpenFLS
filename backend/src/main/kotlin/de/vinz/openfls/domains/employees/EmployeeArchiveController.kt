package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionRequest
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveActor
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveService
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveStateException
import de.vinz.openfls.domains.employees.archive.dtos.EmployeeArchiveHistoryEntryReadDto
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.exceptions.UserNotAllowedException
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employees")
class EmployeeArchiveController(
    private val employeeArchiveService: EmployeeArchiveService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService
) {
    private val logger: Logger = LoggerFactory.getLogger(EmployeeArchiveController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @GetMapping("{id}/archive/history")
    fun getArchiveHistory(@PathVariable id: Long): ResponseEntity<List<EmployeeArchiveHistoryEntryReadDto>> {
        return ResponseEntity.ok(employeeArchiveService.getArchiveHistory(id))
    }

    @PostMapping("{id}/archive")
    fun archive(
        @PathVariable id: Long,
        @Valid @RequestBody request: EmployeeArchiveActionRequest
    ): Any {
        return try {
            ResponseEntity.ok(
                employeeArchiveService.archive(
                    employeeId = id,
                    actionDate = request.actionDate!!,
                    reason = request.reason,
                    remark = request.remark,
                    actor = loadActor()
                )
            )
        } catch (ex: UserNotAllowedException) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
        } catch (ex: EmployeeArchiveStateException) {
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
        @Valid @RequestBody request: EmployeeArchiveActionRequest
    ): Any {
        return try {
            ResponseEntity.ok(
                employeeArchiveService.reactivate(
                    employeeId = id,
                    actionDate = request.actionDate!!,
                    reason = request.reason,
                    remark = request.remark,
                    actor = loadActor()
                )
            )
        } catch (ex: UserNotAllowedException) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
        } catch (ex: EmployeeArchiveStateException) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.CONFLICT)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
        }
    }

    private fun loadActor(): EmployeeArchiveActor {
        val employeeId = accessService.getId()
        val employee = employeeService.getEmployeeDtoById(employeeId, accessService.isAdmin())
            ?: throw IllegalArgumentException("employee not found")

        return EmployeeArchiveActor(
            employeeId = employee.id,
            firstname = employee.firstName,
            lastname = employee.lastName,
            isAdmin = accessService.isAdmin()
        )
    }
}
