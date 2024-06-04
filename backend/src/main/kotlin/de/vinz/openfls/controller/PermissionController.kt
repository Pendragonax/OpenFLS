package de.vinz.openfls.controller

import de.vinz.openfls.dtos.PermissionDto
import de.vinz.openfls.entities.Permission
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.repositories.PermissionRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.repositories.InstitutionRepository
import de.vinz.openfls.services.HelperService
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import jakarta.validation.Valid

@RestController
@RequestMapping("/permissions")
class PermissionController(
        private val repository: PermissionRepository,
        private val employeeRepository: EmployeeRepository,
        private val institutionRepository: InstitutionRepository,
        private val helperService: HelperService,
        private val modelMapper: ModelMapper)
{

    private val logger: Logger = LoggerFactory.getLogger(SponsorController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody valueDto: PermissionDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val permission = repository.save(convertToEntity(valueDto))

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(permission)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody valueDto: PermissionDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val permission = repository.save(convertToEntity(valueDto))

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(permission)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            repository.deleteById(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
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

    @GetMapping("")
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entities = repository.findAll()
            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(entities)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/combination/{employeeId}/{institutionId}")
    fun getById(@PathVariable employeeId: Long,
                @PathVariable institutionId: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entity = repository.findByIds(employeeId, institutionId)
            if (logPerformance) {
                logger.info(String.format("%s getById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(entity)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/employee/{employeeId}")
    fun getByEmployeeId(@PathVariable employeeId: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entity = repository.findByEmployeeId(employeeId)
            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(entity)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/institution/{institutionId}")
    fun getByInstitutionId(@PathVariable institutionId: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entity = repository.findByInstitutionId(institutionId)
            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(entity)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    private fun convertToEntity(permissionDto: PermissionDto): Permission {
        val permission: Permission = modelMapper.map(permissionDto, Permission::class.java)

        permission.employee = employeeRepository.findById(permissionDto.employeeId).get()
        permission.institution = institutionRepository.findById(permissionDto.institutionId).get()

        return permission
    }
}