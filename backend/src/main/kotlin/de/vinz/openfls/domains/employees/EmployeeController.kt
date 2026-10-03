package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.dto.EmployeeCreateRequest
import de.vinz.openfls.domains.employees.dto.EmployeeCreateResult
import de.vinz.openfls.domains.employees.dto.EmployeeDeleteResult
import de.vinz.openfls.domains.employees.dto.EmployeeFavoriteResult
import de.vinz.openfls.domains.employees.dto.EmployeePasswordResetResult
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateRoleResult
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateRequest
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateResult
import de.vinz.openfls.domains.employees.service.EmployeeDeletionService
import de.vinz.openfls.domains.employees.service.EmployeeFavoriteService
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.services.ExceptionResponseService
import de.vinz.openfls.services.PerformanceLoggingService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employees")
class EmployeeController(
    private val employeeService: EmployeeService,
    private val employeeDeletionService: EmployeeDeletionService,
    private val employeeFavoriteService: EmployeeFavoriteService,
    private val accessService: AccessService,
    private val performanceLoggingService: PerformanceLoggingService
) {

    private val logger: Logger = LoggerFactory.getLogger(EmployeeController::class.java)

    @PostMapping
    fun create(@Valid @RequestBody request: EmployeeCreateRequest): Any {
        val startMs = System.currentTimeMillis()

        return try {
            when (val result = employeeService.create(request)) {
                is EmployeeCreateResult.Success -> ResponseEntity.ok(result.response)
                is EmployeeCreateResult.InvalidUsername -> badRequest(result.reason)
                EmployeeCreateResult.UsernameTaken -> conflict("username already exists")
                EmployeeCreateResult.InvalidRole -> badRequest("role is invalid")
                EmployeeCreateResult.SponsorNotFound -> badRequest("sponsor not found")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("create", startMs, logger)
        }
    }

    @PutMapping("{id}/{role}")
    fun updateRole(@PathVariable id: Long, @PathVariable role: Int): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to change the role")

        return try {
            when (val result = employeeService.updateRole(id, role)) {
                is EmployeeUpdateRoleResult.Success -> ResponseEntity.ok(result.response)
                EmployeeUpdateRoleResult.NotFound -> notFound()
                EmployeeUpdateRoleResult.InvalidRole -> badRequest("role is invalid")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("updateRole", startMs, logger)
        }
    }

    @PutMapping("reset_password/{id}")
    fun resetPassword(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to reset passwords")

        return try {
            when (val result = employeeService.resetPassword(id)) {
                is EmployeePasswordResetResult.Success -> ResponseEntity.ok(result.response)
                EmployeePasswordResetResult.NotFound -> notFound()
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("resetPassword", startMs, logger)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: EmployeeUpdateRequest): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.canModifyEmployee(request.id))
            return forbidden("no permission to update this employee")
        if (id != request.id)
            return badRequest("path id and request id are not the same")

        return try {
            when (val result = employeeService.update(id, request)) {
                is EmployeeUpdateResult.Success -> ResponseEntity.ok(result.response)
                EmployeeUpdateResult.NotFound -> notFound()
                EmployeeUpdateResult.SponsorNotFound -> badRequest("sponsor not found")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("update", startMs, logger)
        }
    }

    @PostMapping("assistance_plan/favorite/{id}")
    fun addAssistancePlanFavorite(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            favoriteResponse(employeeFavoriteService.addAssistancePlanFavorite(accessService.getId(), id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("addAssistancePlanFavorite", startMs, logger)
        }
    }

    @DeleteMapping("assistance_plan/favorite/{id}")
    fun deleteAssistancePlanFavorite(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            favoriteResponse(employeeFavoriteService.deleteAssistancePlanFavorite(accessService.getId(), id))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("deleteAssistancePlanFavorite", startMs, logger)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        if (!accessService.isAdmin())
            return forbidden("no permission to delete this employee")

        return try {
            when (val result = employeeDeletionService.delete(id)) {
                is EmployeeDeleteResult.Success -> ResponseEntity.ok(result.response)
                EmployeeDeleteResult.NotFound -> notFound()
                EmployeeDeleteResult.HasServices -> conflict("employee has service entries")
            }
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("delete", startMs, logger)
        }
    }

    @GetMapping("")
    fun getAll(@RequestParam(defaultValue = "false") includeArchived: Boolean): Any {
        val startMs = System.currentTimeMillis()

        return try {
            ResponseEntity.ok(employeeService.getAllEmployeeDetails(includeArchived && accessService.isAdmin()))
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getAll", startMs, logger)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        val startMs = System.currentTimeMillis()

        return try {
            val employee = employeeService.getEmployeeDetailById(id, accessService.isAdmin())
                ?: return notFound()
            ResponseEntity.ok(employee)
        } catch (ex: Exception) {
            ExceptionResponseService.getExceptionResponseEntity(ex, logger)
        } finally {
            performanceLoggingService.logPerformance("getById", startMs, logger)
        }
    }

    private fun favoriteResponse(result: EmployeeFavoriteResult): ResponseEntity<out Any> {
        return when (result) {
            EmployeeFavoriteResult.Success -> ResponseEntity.ok().build<Void>()
            EmployeeFavoriteResult.EmployeeNotFound -> notFound()
            EmployeeFavoriteResult.AssistancePlanNotFound -> badRequest("assistance plan not found")
            EmployeeFavoriteResult.ClientNotFound -> badRequest("client not found")
        }
    }

    private fun notFound(): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body("employee not found")

    private fun forbidden(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(message)

    private fun badRequest(message: String): ResponseEntity<String> =
        ResponseEntity.badRequest().body(message)

    private fun conflict(message: String): ResponseEntity<String> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(message)
}
