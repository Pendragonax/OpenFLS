package de.vinz.openfls.domains.employees

import de.vinz.openfls.dtos.EmployeeDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.AccessService
import de.vinz.openfls.services.EmployeeService
import de.vinz.openfls.services.TokenService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employees")
class EmployeeController(
    private val employeeService: EmployeeService,
    private val accessService: AccessService,
    private val tokenService: TokenService
) {

    private val logger: Logger = LoggerFactory.getLogger(EmployeeController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody valueDto: EmployeeDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = employeeService.create(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}/{role}")
    fun updateRole(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                   @PathVariable id: Long,
                   @PathVariable role: Int): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to change the role")

            // update role
            val dto = employeeService.updateRole(id, role)

            if (logPerformance) {
                logger.info(String.format("%s updateRole took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("reset_password/{id}")
    fun resetPassword(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                      @PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to reset passwords")
            if (!employeeService.existsById(id))
                throw IllegalArgumentException("employee not found")

            val dto = employeeService.resetPassword(id)

            if (logPerformance) {
                logger.info(String.format("%s resetPassword took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            return ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long,
               @Valid @RequestBody valueDto: EmployeeDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.canModifyEmployee(token, valueDto.id))
                throw IllegalArgumentException("no permission to update this employee")
            if (id != valueDto.id)
                throw IllegalArgumentException("path id and dto id are not the same")
            if (!employeeService.existsById(id))
                throw IllegalArgumentException("employee not found")

            val dto = employeeService.update(id, valueDto, token)

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("assistance_plan/favorite")
    fun getAssistancePlanFavorites(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val userId = tokenService.getUserInfo(token)
            val dto = employeeService.getAssistancePlanAsFavorites(userId.first)

            if (logPerformance) {
                logger.info(String.format("%s getAssistancePlanFavorites took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("assistance_plan/favorite/{id}")
    fun addAssistancePlanFavorite(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                  @PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val userId = tokenService.getUserInfo(token)
            employeeService.addAssistancePlanAsFavorite(id, userId.first)

            if (logPerformance) {
                logger.info(String.format("%s addAssistancePlanFavorite took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok()
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("assistance_plan/favorite/{id}")
    fun deleteAssistancePlanFavorite(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                     @PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val userId = tokenService.getUserInfo(token)
            employeeService.deleteAssistancePlanAsFavorite(id, userId.first)

            if (logPerformance) {
                logger.info(String.format("%s deleteAssistancePlanFavorite took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok()
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to delete this employee")
            if (!employeeService.existsById(id))
                throw IllegalArgumentException("employee not found")

            val dto = employeeService.getEmployeeDtoById(id, true)
            employeeService.delete(id)

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
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = employeeService.getAllEmployeeDtos()

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}")
    fun getById(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String, @PathVariable id: Long): Any  {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = employeeService.getEmployeeDtoById(id, accessService.isAdmin(token))

            if (logPerformance) {
                logger.info(String.format("%s getById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }
}