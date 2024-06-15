package de.vinz.openfls.domains.services

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.permissions.PermissionService
import de.vinz.openfls.domains.services.dtos.ServiceDto
import de.vinz.openfls.domains.services.dtos.ServiceFilterDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.ConverterService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/services")
class ServiceController(
        private val serviceService: ServiceService,
        private val employeeService: EmployeeService,
        private val accessService: AccessService,
        private val permissionService: PermissionService,
        private val assistancePlanService: AssistancePlanService,
        private val converter: ConverterService
) {

    private val logger: Logger = LoggerFactory.getLogger(ServiceController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: ServiceDto): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canWriteEntries(token, valueDto.institutionId))
                throw IllegalArgumentException("No permission to write entries to this institution")

            val dto = serviceService.create(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    employeeService.getById(valueDto.employeeId),
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @PutMapping("{id}")
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long,
               @Valid @RequestBody valueDto: ServiceDto): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canWriteEntries(token, valueDto.institutionId))
                throw IllegalArgumentException("No permission to update this service")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")
            if (!accessService.isAdmin(token) &&
                    serviceService.getById(id)?.employee?.id != accessService.getId(token))
                throw IllegalArgumentException("Your not the owner of this service or the admin")
            if (serviceService.getById(id)
                            ?.start
                            ?.toLocalDate()
                            ?.isBefore(LocalDate.now().minusDays(14)) == true) {
                throw IllegalArgumentException("14 days edit period is over")
            }

            val dto = serviceService.update(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @DeleteMapping("{id}")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val service = serviceService.getById(id);

            if (!accessService.isAdmin(token) &&
                    (service?.employee?.id != accessService.getId(token) ||
                            service.start.toLocalDate().isBefore(LocalDate.now().minusDays(14))))
                throw IllegalArgumentException("No permission to delete this service")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")

            val dto = serviceService.getDtoById(id)
            serviceService.delete(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping
    fun getAll(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("No permission to get all services")

            val dtos = serviceService.getAllDtos()

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("{id}")
    fun getById(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                @PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (id <= 0)
                throw IllegalArgumentException("id is <= 0")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")

            val dto = serviceService.getDtoById(id)

            if (dto != null && !accessService.isAdmin(token) &&
                    !accessService.canReadEntries(token, dto.institutionId))
                throw IllegalArgumentException("Your not the allowed to read this entry")


            if (logPerformance) {
                logger.info(String.format("%s getById took %d ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{id}")
    fun getByAssistancePlan(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                            @PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (id <= 0)
                throw IllegalArgumentException("id is <= 0")
            if (!assistancePlanService.existsById(id))
                throw IllegalArgumentException("assistance plan not found")
            if (!accessService.canModifyAssistancePlan(token, id))
                throw IllegalArgumentException("no permission to load the services of this assistance plan")

            val dtos = serviceService.getXLDtosByAssistancePlan(id)

            if (logPerformance) {
                logger.info(String.format("%s getByAssistancePlan took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}/{date}")
    fun getByEmployeeAndDate(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                             @PathVariable id: Long,
                             @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val leadingInstitutionIds = permissionService
                    .getLeadingInstitutionIdsByEmployee(accessService.getId(token))
            val userId = accessService.getId(token)
            val isAdmin = accessService.isAdmin(token)

            val dtos = serviceService.getDtosByEmployeeAndDate(id, date)
                    .filter { isAdmin || it.employeeId == userId || leadingInstitutionIds.contains(it.institutionId) }
                    .sortedBy { it.start }

            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeAndDate took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}/{start}/{end}")
    fun getByEmployeeAndStartAndEnd(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                    @PathVariable id: Long,
                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val leadingInstitutionIds = permissionService
                    .getLeadingInstitutionIdsByEmployee(accessService.getId(token))
            val userId = accessService.getId(token)
            val isAdmin = accessService.isAdmin(token)

            val dtos = serviceService.getDtosByEmployeeAndStartAndEnd(id, start, end)
                    .filter { isAdmin || it.employeeId == userId || leadingInstitutionIds.contains(it.institutionId) }
                    .sortedBy { it.start }

            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeAndStartAndEnd took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client/{id}/{date}")
    fun getByClientAndDate(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                           @PathVariable id: Long,
                           @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val affiliatedInstitutionIds = permissionService
                    .getReadableInstitutionIdsByEmployee(accessService.getId(token))
            val userId = accessService.getId(token)
            val isAdmin = accessService.isAdmin(token)

            val dtos = serviceService.getDtosByClientAndDate(id, date)
                    .filter { isAdmin || it.employeeId == userId || affiliatedInstitutionIds.contains(it.institutionId) }

            if (logPerformance) {
                logger.info(String.format("%s getByClientAndDate took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client/{id}/{start}/{end}")
    fun getByClientAndStartAndEnd(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                  @PathVariable id: Long,
                                  @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                  @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val affiliatedInstitutionIds = permissionService
                    .getReadableInstitutionIdsByEmployee(accessService.getId(token))
            val userId = accessService.getId(token)
            val isAdmin = accessService.isAdmin(token)

            val dtos = serviceService.getDtosByClientAndStartAndEnd(id, start, end)
                    .filter { isAdmin || it.employeeId == userId || affiliatedInstitutionIds.contains(it.institutionId) }

            if (logPerformance) {
                logger.info(String.format("%s getByClientAndStartAndEnd took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}")
    fun getByEmployeeAndFilter(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                               @PathVariable id: Long,
                               @Valid @RequestBody valueDto: ServiceFilterDto): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (valueDto.date == null)
                throw IllegalArgumentException("No date")

            val dtos = serviceService.getDtosByEmployeeAndFilter(id, valueDto)

            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeAndFilter took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("times/{id}/{start}/{end}")
    fun getTimesByEmployee(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                           @PathVariable id: Long,
                           @Valid @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                           @Valid @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (accessService.getId(token) != id &&
                    !accessService.isAdmin(token) &&
                    !accessService.canReadEmployeeStats(token, id))
                throw IllegalArgumentException("No permission to get the times of this employee")

            val serviceDtos = serviceService.getDtosByEmployeeAndStartEndDate(id, start, end)
            val dto = this.converter.convertServicesToServiceTimeDto(serviceDtos).apply {
                periodDays = start.until(end).days + 1
            }

            if (logPerformance) {
                logger.info(String.format("%s getTimesByEmployee took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/employee/{id}")
    fun countByEmployee(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                        @PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(serviceService.countByEmployee(id))
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/client/{id}")
    fun countByClient(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                      @PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(serviceService.countByClient(id))
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/assistance_plan/{id}")
    fun countByAssistancePlan(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                              @PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(serviceService.countByAssistancePlan(id))
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/goal/{id}")
    fun countByGoal(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                    @PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(serviceService.countByGoal(id))
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }
}