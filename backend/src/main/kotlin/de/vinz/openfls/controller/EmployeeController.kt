package de.vinz.openfls.controller

import de.vinz.openfls.dtos.EmployeeDto
import de.vinz.openfls.dtos.PermissionDto
import de.vinz.openfls.dtos.UnprofessionalDto
import de.vinz.openfls.entities.Employee
import de.vinz.openfls.entities.EmployeeAccess
import de.vinz.openfls.entities.Permission
import de.vinz.openfls.entities.Unprofessional
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.*
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import kotlin.Exception

@RestController
@RequestMapping("/employees")
class EmployeeController(
    private val employeeService: EmployeeService,
    private val modelMapper: ModelMapper,
    private val passwordEncoder: PasswordEncoder,
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

            // convert employee
            val entity = modelMapper.map(valueDto, Employee::class.java)
            // convert access
            entity.access = modelMapper.map(valueDto.access, EmployeeAccess::class.java)?.apply {
                password = if (username.isNotEmpty()) passwordEncoder.encode(username).toString() else ""
            }

            entity.permissions = convertToPermissions(valueDto.permissions, -1)
            entity.unprofessionals = convertToUnprofessionals(valueDto.unprofessionals, -1)

            val savedEntity = employeeService.create(entity)

            // set id to dto
            valueDto.id = savedEntity.id!!
            valueDto.access?.id = savedEntity.id!!

            valueDto.permissions = valueDto.permissions
                ?.filter { savedEntity.permissions
                    ?.any { permission -> permission.id.institutionId == it.institutionId } ?: false }
                ?.map { it.apply { employeeId = savedEntity.id!! } }
                ?.toTypedArray()
            valueDto.unprofessionals = valueDto.unprofessionals
                ?.map { it.apply { employeeId = savedEntity.id!! } }
                ?.toTypedArray()

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(valueDto)
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
            val dto = modelMapper.map(
                employeeService.updateRole(id, role).apply {
                    access?.password = "" },
                EmployeeDto::class.java)

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

            val dto = modelMapper.map(employeeService.resetPassword(id), EmployeeDto::class.java)

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

            // convert employee
            val entity = modelMapper.map(valueDto, Employee::class.java)

            if (accessService.isAdmin(token)) {
                entity.permissions = convertToPermissions(valueDto.permissions, id)
                entity.unprofessionals = convertToUnprofessionals(valueDto.unprofessionals, id)
            } else {
                entity.permissions = mutableSetOf()
                entity.unprofessionals = mutableSetOf()
            }

            // update employee
            val savedEntity = employeeService.update(entity)

            // permissions
            valueDto.permissions = savedEntity.permissions
                ?.map { modelMapper.map(it, PermissionDto::class.java) }
                ?.toTypedArray()
            // unprofessionals
            valueDto.unprofessionals = savedEntity.unprofessionals
                ?.map { modelMapper.map(it, UnprofessionalDto::class.java) }
                ?.toTypedArray()

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(valueDto)
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

            val dto = modelMapper.map(employeeService.getById(id, true), EmployeeDto::class.java)

            employeeService.delete(id).toString()

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

            val dtos = employeeService
                .getAll()
                .sortedBy { it.lastname.lowercase() }
                .map { employee -> modelMapper.map(employee, EmployeeDto::class.java).apply {
                    access?.password = ""
                } }

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

            val dto = modelMapper.map(
                employeeService.getById(
                    id,
                    accessService.isAdmin(token)),
                EmployeeDto::class.java)

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

    private fun convertToPermissions(permissionDtos: Array<PermissionDto>?, employeeId: Long): MutableSet<Permission> {
        return permissionDtos
            ?.map {
                modelMapper
                    .map(it, Permission::class.java)
                    .apply { it.employeeId = employeeId } }
            ?.toMutableSet() ?: mutableSetOf()
    }

    private fun convertToUnprofessionals(dtos: Array<UnprofessionalDto>?, employeeId: Long): MutableSet<Unprofessional> {
        return dtos
            ?.map {
                modelMapper
                    .map(it, Unprofessional::class.java)
                    .apply { it.employeeId = employeeId } }
            ?.toMutableSet() ?: mutableSetOf()
    }
}